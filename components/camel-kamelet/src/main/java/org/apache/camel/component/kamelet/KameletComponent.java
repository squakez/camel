/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.kamelet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.ServiceStatus;
import org.apache.camel.VetoCamelContextStartException;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.RouteTemplateLoaderListener;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;
import org.apache.camel.support.LifecycleStrategySupport;
import org.apache.camel.support.RouteTemplateHelper;
import org.apache.camel.support.service.ServiceHelper;
import org.apache.camel.util.StopWatch;
import org.apache.camel.util.URISupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.component.kamelet.Kamelet.BRIDGE_ERROR_HANDLER;
import static org.apache.camel.component.kamelet.Kamelet.NO_ERROR_HANDLER;
import static org.apache.camel.component.kamelet.Kamelet.PARAM_LOCATION;
import static org.apache.camel.component.kamelet.Kamelet.PARAM_ROUTE_ID;
import static org.apache.camel.component.kamelet.Kamelet.PARAM_TEMPLATE_ID;
import static org.apache.camel.component.kamelet.Kamelet.PARAM_UUID;

/**
 * Materialize route templates
 */
@Component(Kamelet.SCHEME)
public class KameletComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(KameletComponent.class);

    private final ReentrantLock lock = new ReentrantLock();

    private final LifecycleHandler lifecycleHandler = new LifecycleHandler();

    // active consumers
    private final Map<String, KameletConsumer> consumers = new HashMap<>();
    private final Lock consumersLock = new ReentrantLock();
    private final Condition consumersCondition = consumersLock.newCondition();
    // active kamelet EIPs
    private final Map<String, Processor> kameletEips = new ConcurrentHashMap<>();

    @Metadata(label = "advanced", autowired = true)
    private RouteTemplateLoaderListener routeTemplateLoaderListener;

    // counter that is used for producers to keep track if any consumer was added/removed since they last checked
    // this is used for optimization to avoid each producer to get consumer for each message processed
    // (locking via synchronized, and then lookup in the map as the cost)
    // consumers and producers are only added/removed during startup/shutdown or if routes is manually controlled
    private volatile int stateCounter;

    @Metadata(label = "producer", defaultValue = "true")
    private boolean block = true;
    @Metadata(label = "producer", defaultValue = "30000")
    private long timeout = 30000L;
    @Metadata(label = "advanced")
    private boolean noErrorHandler;

    @Metadata
    private Map<String, Properties> templateProperties;
    @Metadata
    private Map<String, Properties> routeProperties;
    @Metadata(defaultValue = Kamelet.DEFAULT_LOCATION)
    private String location = Kamelet.DEFAULT_LOCATION;

    public KameletComponent() {
    }

    public void addKameletEip(String key, Processor callback) {
        kameletEips.put(key, callback);
    }

    public Processor removeKameletEip(String key) {
        return kameletEips.remove(key);
    }

    public Processor getKameletEip(String key) {
        return kameletEips.get(key);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        final String templateId = Kamelet.extractTemplateId(getCamelContext(), remaining, parameters);
        final String uuid = Kamelet.extractUuid();
        final String routeId = Kamelet.extractRouteId(getCamelContext(), remaining, parameters, uuid);
        final String loc = Kamelet.extractLocation(getCamelContext(), parameters);

        parameters.remove(PARAM_TEMPLATE_ID);
        parameters.remove(PARAM_ROUTE_ID);
        parameters.remove(PARAM_LOCATION);
        parameters.remove(PARAM_UUID);

        // manually need to resolve raw parameters as input to the kamelet because
        // resolveRawParameterValues is false
        // this ensures that parameters such as passwords are used as-is and not encoded
        // but this requires to use RAW() syntax in the kamelet template.
        URISupport.resolveRawParameterValues(parameters);

        final KameletEndpoint endpoint;

        if (Kamelet.SOURCE_ID.equals(remaining) || Kamelet.SINK_ID.equals(remaining)) {
            //
            // if remaining is either `source` or `sink' then it is a virtual
            // endpoint that is used inside the kamelet definition to mark it
            // as in/out endpoint.
            //
            // The following snippet defines a template which will act as a
            // consumer for this Kamelet:
            //
            //     from("kamelet:source")
            //         .to("log:info")
            //
            // The following snippet defines a template which will act as a
            // producer for this Kamelet:
            //
            //     from("telegram:bots")
            //         .to("kamelet:sink")
            //
            // Note that at the moment, there's no enforcement around `source`
            // and `sink' to be defined on the right side (producer or consumer)
            //
            endpoint = new KameletEndpoint(uri, this, templateId, routeId);

            // forward component properties
            endpoint.setNoErrorHandler(noErrorHandler);
            endpoint.setBlock(block);
            endpoint.setTimeout(timeout);
            // endpoint specific location
            endpoint.setLocation(loc);

            // set endpoint specific properties
            setProperties(endpoint, parameters);
        } else {
            endpoint = new KameletEndpoint(uri, this, templateId, routeId) {
                @Override
                protected void doInit() throws Exception {
                    super.doInit();
                    //
                    // since this is the real kamelet, then we need to hand it
                    // over to the tracker.
                    //
                    String routeId = getCamelContext().getCamelContextExtension().getCreateRoute();
                    String processorId = getCamelContext().getCamelContextExtension().getCreateProcessor();
                    lifecycleHandler.track(this, routeId, processorId);
                }
            };

            // forward component properties
            endpoint.setNoErrorHandler(noErrorHandler);
            endpoint.setBlock(block);
            endpoint.setTimeout(timeout);
            // endpoint specific location
            endpoint.setLocation(loc);

            // set and remove endpoint specific properties
            setProperties(endpoint, parameters);

            Map<String, Object> kameletProperties = new HashMap<>();

            //
            // Load properties from the component configuration. Template and route specific properties
            // can be set through properties, as an example:
            //
            //     camel.component.kamelet.template-properties[templateId].key = val
            //     camel.component.kamelet.route-properties[routeId].key = val
            //
            if (templateProperties != null) {
                Properties props = templateProperties.get(templateId);
                if (props != null) {
                    props.stringPropertyNames().forEach(name -> kameletProperties.put(name, props.get(name)));
                }
            }
            if (routeProperties != null) {
                Properties props = routeProperties.get(routeId);
                if (props != null) {
                    props.stringPropertyNames().forEach(name -> kameletProperties.put(name, props.get(name)));
                }
            }

            //
            // We can't mix configuration styles so if properties are not configured through the component,
            // then fallback to the old - deprecated - style.
            //
            if (kameletProperties.isEmpty()) {
                //
                // The properties for the kamelets are determined by global properties
                // and local endpoint parameters,
                //
                // Global parameters are loaded in the following order:
                //
                //   camel.kamelet." + templateId
                //   camel.kamelet." + templateId + "." routeId
                //
                Kamelet.extractKameletProperties(getCamelContext(), kameletProperties, templateId, routeId);
            }

            //
            // Look for OS environment variables that match the Kamelet properties
            // Environment variables are loaded in the following order:
            //
            //   CAMEL_KAMELET_" + templateId
            //   CAMEL_KAMELET_" + templateId + "_" routeId
            //
            Kamelet.extractKameletEnvironmentVariables(kameletProperties, templateId, routeId);

            //
            // Uri params have the highest precedence
            //
            kameletProperties.putAll(parameters);

            //
            // And finally we have some specific properties that cannot be changed by the user.
            //
            kameletProperties.put(PARAM_TEMPLATE_ID, templateId);
            kameletProperties.put(PARAM_ROUTE_ID, routeId);
            kameletProperties.put(PARAM_UUID, uuid);
            kameletProperties.put(NO_ERROR_HANDLER, endpoint.isNoErrorHandler());
            kameletProperties.put(BRIDGE_ERROR_HANDLER, endpoint.isBridgeErrorHandler());

            // set kamelet specific properties
            endpoint.setKameletProperties(kameletProperties);

            //
            // Add a custom converter to convert a RouteTemplateDefinition to a RouteDefinition
            // and make sure consumer URIs are unique.
            //
            ((ModelCamelContext) getCamelContext()).addRouteTemplateDefinitionConverter(
                    templateId,
                    Kamelet::templateToRoute);
        }

        return endpoint;
    }

    @Override
    protected boolean resolveRawParameterValues() {
        return false;
    }

    public boolean isNoErrorHandler() {
        return noErrorHandler;
    }

    /**
     * Whether kamelets should use error handling or not. By default, the Kamelet uses the same error handler as from
     * the calling route. This means that if the calling route has error handling that performs retries, or routing to a
     * dead letter channel, then the kamelet route will use this also.
     * <p>
     * This can be turned off by setting this option to true. If off then the kamelet route is not using error handling,
     * and any exception thrown will for source kamelets be logged by the consumer, and the sink/action kamelets will
     * fail processing.
     */
    public void setNoErrorHandler(boolean noErrorHandler) {
        this.noErrorHandler = noErrorHandler;
    }

    public boolean isBlock() {
        return block;
    }

    /**
     * If sending a message to a kamelet endpoint which has no active consumer, then we can tell the producer to block
     * and wait for the consumer to become active.
     */
    public void setBlock(boolean block) {
        this.block = block;
    }

    public long getTimeout() {
        return timeout;
    }

    /**
     * The timeout value to use if block is enabled.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Map<String, Properties> getTemplateProperties() {
        return templateProperties;
    }

    /**
     * Set template local parameters.
     */
    public void setTemplateProperties(Map<String, Properties> templateProperties) {
        this.templateProperties = templateProperties;
    }

    public Map<String, Properties> getRouteProperties() {
        return routeProperties;
    }

    /**
     * Set route local parameters.
     */
    public void setRouteProperties(Map<String, Properties> routeProperties) {
        this.routeProperties = routeProperties;
    }

    public String getLocation() {
        return location;
    }

    /**
     * The location(s) of the Kamelets on the file system. Multiple locations can be set separated by comma.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    public RouteTemplateLoaderListener getRouteTemplateLoaderListener() {
        return routeTemplateLoaderListener;
    }

    /**
     * To plugin a custom listener for when the Kamelet component is loading Kamelets from external resources.
     */
    public void setRouteTemplateLoaderListener(RouteTemplateLoaderListener routeTemplateLoaderListener) {
        this.routeTemplateLoaderListener = routeTemplateLoaderListener;
    }

    int getStateCounter() {
        return stateCounter;
    }

    public void addConsumer(String key, KameletConsumer consumer) {
        consumersLock.lock();
        try {
            if (consumers.putIfAbsent(key, consumer) != null) {
                throw new IllegalArgumentException(
                        "Cannot add a 2nd consumer to the same endpoint: " + key
                                                   + ". KameletEndpoint only allows one consumer.");
            }
            // state changed so inc counter
            stateCounter++;
            consumersCondition.signalAll();
        } finally {
            consumersLock.unlock();
        }
    }

    public void removeConsumer(String key, KameletConsumer consumer) {
        consumersLock.lock();
        try {
            consumers.remove(key, consumer);
            // state changed so inc counter
            stateCounter++;
            consumersCondition.signalAll();
        } finally {
            consumersLock.unlock();
        }
    }

    protected KameletConsumer getConsumer(String key, boolean block, long timeout) throws InterruptedException {
        consumersLock.lock();
        try {
            KameletConsumer answer = consumers.get(key);
            if (answer == null && block) {
                StopWatch watch = new StopWatch();
                for (;;) {
                    answer = consumers.get(key);
                    if (answer != null) {
                        break;
                    }
                    long rem = timeout - watch.taken();
                    if (rem <= 0) {
                        break;
                    }
                    consumersCondition.await(rem, TimeUnit.MILLISECONDS);
                }
            }
            return answer;
        } finally {
            consumersLock.unlock();
        }
    }

    @Override
    protected void doInit() throws Exception {
        getCamelContext().addLifecycleStrategy(lifecycleHandler);

        if (getCamelContext().isRunAllowed()) {
            lifecycleHandler.setInitialized(true);
        }

        super.doInit();
    }

    @Override
    protected void doShutdown() throws Exception {
        getCamelContext().getLifecycleStrategies().remove(lifecycleHandler);

        ServiceHelper.stopAndShutdownService(consumers);
        consumers.clear();
        kameletEips.clear();
        super.doShutdown();
    }

    /*
     * This LifecycleHandler is used to keep track of created kamelet endpoints during startup as
     * we need to defer create routes from templates until camel context has finished loading
     * all routes and whatnot.
     *
     * Once the camel context is initialized all the endpoint tracked by this LifecycleHandler will
     * be used to create routes from templates.
     */
    private class LifecycleHandler extends LifecycleStrategySupport {

        record Tuple(KameletEndpoint endpoint, String parentRouteId, String parentProcessorId) {
        }

        private final List<Tuple> endpoints;
        private final AtomicBoolean initialized;

        public LifecycleHandler() {
            this.endpoints = new ArrayList<>();
            this.initialized = new AtomicBoolean();
        }

        public void createRouteForEndpoint(KameletEndpoint endpoint, String parentRouteId, String parentProcessorId)
                throws Exception {
            // creating dynamic routes from kamelets should not happen concurrently so we use locking
            lock.lock();
            try {
                doCreateRouteForEndpoint(endpoint, parentRouteId, parentProcessorId);
            } finally {
                lock.unlock();
            }
        }

        protected void doCreateRouteForEndpoint(KameletEndpoint endpoint, String parentRouteId, String parentProcessorId)
                throws Exception {

            final ModelCamelContext context = (ModelCamelContext) getCamelContext();
            final String templateId = endpoint.getTemplateId();
            final String routeId = endpoint.getRouteId();
            final String loc = endpoint.getLocation() != null ? endpoint.getLocation() : getLocation();
            final String uuid = (String) endpoint.getKameletProperties().get(PARAM_UUID);

            if (context.getRouteTemplateDefinition(templateId) == null && loc != null) {
                LOG.debug("Loading route template={} from {}", templateId, loc);
                RouteTemplateHelper.loadRouteTemplateFromLocation(getCamelContext(), routeTemplateLoaderListener, templateId,
                        loc);
            }

            LOG.debug("Creating route from template={} and id={}", templateId, routeId);
            try {
                String id = context.addRouteFromKamelet(routeId, templateId, uuid, parentRouteId, parentProcessorId,
                        endpoint.getKameletProperties());
                RouteDefinition def = context.getRouteDefinition(id);

                // start the route if not already started
                ServiceStatus status = context.getRouteController().getRouteStatus(id);
                boolean started = status != null && status.isStarted();
                if (!started) {
                    context.startRouteDefinitions(Collections.singletonList(def));
                }

                LOG.debug("Route with id={} created from template={}", id, templateId);
            } catch (Exception e) {
                throw new FailedToCreateKameletException(templateId, loc, e);
            }
        }

        @Override
        public void onContextInitialized(CamelContext context) throws VetoCamelContextStartException {
            if (this.initialized.compareAndSet(false, true)) {
                for (Tuple tuple : endpoints) {
                    try {
                        createRouteForEndpoint(tuple.endpoint, tuple.parentRouteId, tuple.parentProcessorId);
                    } catch (Exception e) {
                        throw new VetoCamelContextStartException(
                                "Failure creating route from template: " + tuple.endpoint.getTemplateId(), e, context);
                    }
                }

                endpoints.clear();
            }
        }

        public void setInitialized(boolean initialized) {
            this.initialized.set(initialized);
        }

        public void track(KameletEndpoint endpoint, String parentRouteId, String parentProcessorId) {
            if (this.initialized.get()) {
                try {
                    createRouteForEndpoint(endpoint, parentRouteId, parentProcessorId);
                } catch (Exception e) {
                    throw RuntimeCamelException.wrapRuntimeException(e);
                }
            } else {
                LOG.debug("Tracking route template={} and id={}", endpoint.getTemplateId(), endpoint.getRouteId());
                this.endpoints.add(new Tuple(endpoint, parentRouteId, parentProcessorId));
            }
        }
    }

}

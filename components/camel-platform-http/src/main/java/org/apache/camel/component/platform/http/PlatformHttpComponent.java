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
package org.apache.camel.component.platform.http;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.Processor;
import org.apache.camel.component.platform.http.spi.PlatformHttpEngine;
import org.apache.camel.spi.FactoryFinder;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.RestApiConsumerFactory;
import org.apache.camel.spi.RestConfiguration;
import org.apache.camel.spi.RestConsumerFactory;
import org.apache.camel.spi.RestOpenApiConsumerFactory;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.CamelContextHelper;
import org.apache.camel.support.HeaderFilterStrategyComponent;
import org.apache.camel.support.RestComponentHelper;
import org.apache.camel.support.service.ServiceHelper;
import org.apache.camel.util.FileUtil;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exposes HTTP endpoints leveraging the given platform's (SpringBoot, WildFly, Quarkus, ...) HTTP server.
 */
@Component("platform-http")
public class PlatformHttpComponent extends HeaderFilterStrategyComponent
        implements RestConsumerFactory, RestApiConsumerFactory, RestOpenApiConsumerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(PlatformHttpComponent.class);

    @Metadata(label = "advanced", description = "An HTTP Server engine implementation to serve the requests")
    private volatile PlatformHttpEngine engine;
    @Metadata(label = "advanced,consumer", defaultValue = "false",
              description = "When Camel is complete processing the message, and the HTTP server is writing response. This option controls whether Camel"
                            + " should catch any failure during writing response and store this on the Exchange, which allows onCompletion/UnitOfWork to"
                            + " regard the Exchange as failed and have access to the caused exception from the HTTP server.")
    private boolean handleWriteResponseError;
    @Metadata(label = "advanced,consumer",
              description = "The period in milliseconds after which the request should be timed out.")
    private long requestTimeout;

    private final Set<HttpEndpointModel> httpEndpoints = new TreeSet<>();
    private final Set<HttpEndpointModel> httpManagementEndpoints = new TreeSet<>();
    private final List<PlatformHttpListener> listeners = new ArrayList<>();
    private volatile boolean localEngine;

    public PlatformHttpComponent() {
        this(null);
    }

    public PlatformHttpComponent(CamelContext context) {
        super(context);
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        PlatformHttpEndpoint endpoint = new PlatformHttpEndpoint(uri, remaining, this);
        endpoint.setPlatformHttpEngine(engine);
        endpoint.setHandleWriteResponseError(handleWriteResponseError);
        endpoint.setRequestTimeout(requestTimeout);
        setEndpointHeaderFilterStrategy(endpoint);
        setProperties(endpoint, parameters);
        return endpoint;
    }

    @Override
    public Consumer createApiConsumer(
            CamelContext camelContext, Processor processor, String contextPath,
            RestConfiguration configuration, Map<String, Object> parameters)
            throws Exception {

        // reuse the createConsumer method we already have. The api need to use GET and match on uri prefix
        return doCreateConsumer(camelContext, processor, "GET", contextPath, null, null, "application/json,text/yaml",
                configuration,
                parameters, true, true);
    }

    @Override
    public Consumer createConsumer(
            CamelContext camelContext, Processor processor, String verb, String basePath,
            String uriTemplate,
            String consumes, String produces, RestConfiguration configuration, Map<String, Object> parameters)
            throws Exception {
        return doCreateConsumer(camelContext, processor, verb, basePath, uriTemplate, consumes, produces, configuration,
                parameters, false, true);
    }

    @Override
    public Consumer createConsumer(
            CamelContext camelContext, Processor processor, String contextPath, RestConfiguration configuration,
            Map<String, Object> parameters)
            throws Exception {
        return doCreateConsumer(camelContext, processor, null, contextPath, null, null, null, configuration,
                parameters, true, false);
    }

    /**
     * Adds a known http endpoint managed by this component.
     */
    public void addHttpEndpoint(String uri, String verbs, String consumes, String produces, Consumer consumer) {
        this.addHttpEndpoint(this.httpEndpoints, uri, verbs, consumes, produces, consumer);
    }

    /**
     * Adds a known http management endpoint managed by this component.
     */
    public void addHttpManagementEndpoint(String uri, String verbs, String consumes, String produces, Consumer consumer) {
        this.addHttpEndpoint(this.httpManagementEndpoints, uri, verbs, consumes, produces, consumer);
    }

    private void addHttpEndpoint(
            Set<HttpEndpointModel> endpoints, String uri, String verbs, String consumes, String produces, Consumer consumer) {
        HttpEndpointModel model = new HttpEndpointModel(uri, verbs, consumes, produces, consumer);
        endpoints.add(model);
        for (PlatformHttpListener listener : listeners) {
            try {
                listener.registerHttpEndpoint(model);
            } catch (Exception e) {
                LOG.warn("Error adding listener due to {}. This exception is ignored", e.getMessage(), e);
            }
        }
    }

    /**
     * Removes a known http endpoint managed by this component.
     */
    public void removeHttpEndpoint(String uri) {
        this.removeHttpEndpoint(this.httpEndpoints, uri);
    }

    /**
     * Removes a known http endpoint managed by this component.
     */
    public void removeHttpManagementEndpoint(String uri) {
        this.removeHttpEndpoint(this.httpManagementEndpoints, uri);
    }

    private void removeHttpEndpoint(Set<HttpEndpointModel> endpoints, String uri) {
        List<HttpEndpointModel> toRemove = new ArrayList<>();
        endpoints.stream().filter(e -> e.getUri().equals(uri)).forEach(model -> {
            toRemove.add(model);
            for (PlatformHttpListener listener : listeners) {
                try {
                    listener.unregisterHttpEndpoint(model);
                } catch (Exception e) {
                    LOG.warn("Error removing listener due to {}. This exception is ignored", e.getMessage(), e);
                }
            }
        });
        toRemove.forEach(endpoints::remove);
    }

    /**
     * Adds a {@link PlatformHttpListener} listener.
     */
    public void addPlatformHttpListener(PlatformHttpListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Removes an existing {@link PlatformHttpListener} listener.
     */
    public void removePlatformHttpListener(PlatformHttpListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Lists the known http endpoints managed by this component. The endpoints are without host:port/[context-path]
     */
    public Set<HttpEndpointModel> getHttpEndpoints() {
        return Collections.unmodifiableSet(httpEndpoints);
    }

    /**
     * Lists the known http management endpoints managed by this component. The endpoints are without
     * host:port/[context-path]
     */
    public Set<HttpEndpointModel> getHttpManagementEndpoints() {
        return Collections.unmodifiableSet(httpManagementEndpoints);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        ServiceHelper.startService(getOrCreateEngine());
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();

        // Stop the platform-http engine only if it has been created through factory finder
        if (localEngine) {
            ServiceHelper.stopService(engine);
        }
    }

    public PlatformHttpEngine getEngine() {
        return engine;
    }

    /**
     * Sets the {@link PlatformHttpEngine} to use.
     */
    public void setEngine(PlatformHttpEngine engine) {
        this.engine = engine;
    }

    public boolean isHandleWriteResponseError() {
        return handleWriteResponseError;
    }

    public void setHandleWriteResponseError(boolean handleWriteResponseError) {
        this.handleWriteResponseError = handleWriteResponseError;
    }

    public long getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(long requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    private Consumer doCreateConsumer(
            CamelContext camelContext, Processor processor, String verb, String basePath,
            String uriTemplate,
            String consumes, String produces, RestConfiguration configuration, Map<String, Object> parameters,
            boolean api, boolean register)
            throws Exception {

        String path = basePath;
        if (uriTemplate != null) {
            // make sure to avoid double slashes
            if (uriTemplate.startsWith("/")) {
                path = path + uriTemplate;
            } else {
                path = path + "/" + uriTemplate;
            }
        }
        path = FileUtil.stripLeadingSeparator(path);

        // if no explicit port/host configured, then use port from rest configuration
        RestConfiguration config = configuration;
        if (config == null) {
            config = CamelContextHelper.getRestConfiguration(getCamelContext(),
                    PlatformHttpConstants.PLATFORM_HTTP_COMPONENT_NAME);
        }

        // prefix path with context-path if configured in rest-dsl configuration
        String contextPath = config.getContextPath();
        if (ObjectHelper.isNotEmpty(contextPath)) {
            contextPath = FileUtil.stripTrailingSeparator(contextPath);
            contextPath = FileUtil.stripLeadingSeparator(contextPath);
            if (ObjectHelper.isNotEmpty(contextPath)) {
                path = contextPath + "/" + path;
            }
        }

        Map<String, Object> map
                = RestComponentHelper.initRestEndpointProperties(PlatformHttpConstants.PLATFORM_HTTP_COMPONENT_NAME, config);

        boolean cors = config.isEnableCORS();

        if (api) {
            map.put("matchOnUriPrefix", "true");
        }
        if (verb != null) {
            RestComponentHelper.addHttpRestrictParam(map, verb, cors);
        }

        String url = RestComponentHelper.createRestConsumerUrl("platform-http", path, map);

        PlatformHttpEndpoint endpoint = (PlatformHttpEndpoint) camelContext.getEndpoint(url, parameters);
        endpoint.setConsumes(consumes);
        endpoint.setProduces(produces);

        // configure consumer properties
        DefaultPlatformHttpConsumer consumer = endpoint.createConsumer(processor);
        consumer.setRegister(register);
        if (config.getConsumerProperties() != null && !config.getConsumerProperties().isEmpty()) {
            setProperties(camelContext, consumer, config.getConsumerProperties());
        }

        return consumer;
    }

    PlatformHttpEngine getOrCreateEngine() {
        if (engine == null) {
            lock.lock();
            try {
                if (engine == null) {
                    LOG.debug("Lookup platform http engine from registry");

                    engine = getCamelContext().getRegistry()
                            .lookupByNameAndType(PlatformHttpConstants.PLATFORM_HTTP_ENGINE_NAME, PlatformHttpEngine.class);

                    if (engine == null) {
                        LOG.debug("Lookup platform http engine from factory");

                        engine = getCamelContext()
                                .getCamelContextExtension()
                                .getFactoryFinder(FactoryFinder.DEFAULT_PATH)
                                .newInstance(PlatformHttpConstants.PLATFORM_HTTP_ENGINE_FACTORY, PlatformHttpEngine.class)
                                .orElseThrow(() -> new IllegalStateException(
                                        "PlatformHttpEngine is neither set on this endpoint neither found in Camel Registry or FactoryFinder."));

                        localEngine = true;
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        CamelContextAware.trySetCamelContext(engine, getCamelContext());
        ServiceHelper.initService(engine);

        return engine;
    }

    @Override
    protected void doInit() throws Exception {
        super.doInit();

        try {
            RestConfiguration config = CamelContextHelper.getRestConfiguration(getCamelContext(), "platform-http");

            // configure additional options on configuration
            if (config.getComponentProperties() != null && !config.getComponentProperties().isEmpty()) {
                setProperties(this, config.getComponentProperties());
            }
        } catch (IllegalArgumentException e) {
            // if there's a mismatch between the component and the rest-configuration,
            // then getRestConfiguration throws IllegalArgumentException which can be
            // safely ignored as it means there's no special conf for this component.
        }
    }

}

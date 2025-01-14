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
package org.apache.camel.tracing;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.Route;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.StaticService;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.spi.CamelLogger;
import org.apache.camel.spi.CamelTracingService;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.LogListener;
import org.apache.camel.spi.RoutePolicy;
import org.apache.camel.spi.RoutePolicyFactory;
import org.apache.camel.support.EndpointHelper;
import org.apache.camel.support.EventNotifierSupport;
import org.apache.camel.support.RoutePolicySupport;
import org.apache.camel.support.service.ServiceHelper;
import org.apache.camel.support.service.ServiceSupport;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Tracer extends ServiceSupport implements CamelTracingService, RoutePolicyFactory, StaticService {

    private static final Logger LOG = LoggerFactory.getLogger(Tracer.class);

    private final TracingLogListener logListener = new TracingLogListener();
    private final TracingEventNotifier eventNotifier = new TracingEventNotifier();
    private String excludePatterns;
    private InterceptStrategy tracingStrategy;

    private final SpanStorageManager spanStorageManager = new SpanStorageManagerExchange();
    private final SpanDecoratorManager spanDecoratorManager = new SpanDecoratorManagerImpl();
    private SpanLifecycleManager spanLifecycleManager;
    private SpanContextPropagationManager spanPropagationManager;

    private CamelContext camelContext;

    protected abstract void initTracer();

    protected abstract void initContextPropagators();

    /**
     * Returns the currently used tracing strategy which is responsible for tracking invoked EIP or beans.
     *
     * @return The currently used tracing strategy
     */
    public InterceptStrategy getTracingStrategy() {
        return tracingStrategy;
    }

    /**
     * Specifies the instance responsible for tracking invoked EIP and beans with Tracing.
     *
     * @param tracingStrategy The instance which tracks invoked EIP and beans
     */
    public void setTracingStrategy(InterceptStrategy tracingStrategy) {
        this.tracingStrategy = tracingStrategy;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    @Override
    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @ManagedAttribute
    public String getExcludePatterns() {
        return excludePatterns;
    }

    public void setExcludePatterns(String excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    @Override
    public RoutePolicy createRoutePolicy(CamelContext camelContext, String routeId, NamedNode route) {
        init(camelContext);
        return new TracingRoutePolicy();
    }

    /**
     * Registers this {@link Tracer} on the {@link CamelContext} if not already registered.
     */
    public void init(CamelContext camelContext) {
        if (!camelContext.hasService(this)) {
            try {
                // start this service eager so we init before Camel is starting up
                camelContext.addService(this, true, true);
            } catch (Exception e) {
                throw RuntimeCamelException.wrapRuntimeCamelException(e);
            }
        }
    }

    @Override
    protected void doInit() {
        ObjectHelper.notNull(camelContext, "CamelContext", this);

        camelContext.getManagementStrategy().addEventNotifier(eventNotifier);
        if (!camelContext.getRoutePolicyFactories().contains(this)) {
            camelContext.addRoutePolicyFactory(this);
        }
        camelContext.getCamelContextExtension().addLogListener(logListener);

        if (tracingStrategy != null) {
            camelContext.getCamelContextExtension().addInterceptStrategy(tracingStrategy);
        }
        initTracer();
        initContextPropagators();
        ServiceHelper.startService(eventNotifier);
    }

    @Override
    protected void doShutdown() {
        // stop event notifier
        camelContext.getManagementStrategy().removeEventNotifier(eventNotifier);
        ServiceHelper.stopService(eventNotifier);

        // remove route policy
        camelContext.getRoutePolicyFactories().remove(this);
    }

    public boolean exclude(Endpoint endpoint, CamelContext context) {
        String url = endpoint.getEndpointUri();
        if (url != null && excludePatterns != null) {
            for (String pattern : excludePatterns.split(",")) {
                pattern = pattern.trim();
                if (EndpointHelper.matchEndpoint(context, url, pattern)) {
                    return true;
                }
            }
        }
        return false;
    }

    private final class TracingEventNotifier extends EventNotifierSupport {

        public TracingEventNotifier() {
            // ignore these
            setIgnoreCamelContextEvents(true);
            setIgnoreCamelContextInitEvents(true);
            setIgnoreRouteEvents(true);
        }

        @Override
        public void notify(CamelEvent event) throws Exception {
            try {
                if (event instanceof CamelEvent.ExchangeCreatedEvent ece) {
                    beginSpan(ece.getExchange());
                } else if (event instanceof CamelEvent.ExchangeCompletedEvent ece) {
                    endSpan(ece.getExchange());
                } else if (event instanceof CamelEvent.ExchangeSendingEvent ese) {
                    if (exclude(ese.getEndpoint(), ese.getExchange().getContext())) {
                        LOG.debug("Tracing: endpoint {} is explicitly excluded, skipping.", ese.getEndpoint());
                    } else {
                        beginSpan(ese.getExchange(), ese.getEndpoint());
                    }
                } else if (event instanceof CamelEvent.ExchangeSentEvent ese) {
                    if (exclude(ese.getEndpoint(), ese.getExchange().getContext())) {
                        LOG.debug("Tracing: endpoint {} is explicitly excluded, skipping.", ese.getEndpoint());
                    } else {
                        endSpan(ese.getExchange(), ese.getEndpoint());
                    }
                }
            } catch (Exception t) {
                // This exception is ignored
                LOG.warn("Tracing: Failed to capture tracing data. This exception is ignored.", t);
            }
        }
    }

    private final class TracingRoutePolicy extends RoutePolicySupport {
        @Override
        public void onExchangeBegin(Route route, Exchange exchange) {
            try {
                if (exclude(route.getEndpoint(), exchange.getContext())) {
                    LOG.debug("Tracing: endpoint {} is explicitly excluded, skipping.", route.getEndpoint());
                } else {
                    beginSpan(exchange, route.getEndpoint());
                }
            } catch (Exception t) {
                LOG.warn("Tracing: Failed to capture tracing data. This exception is ignored.", t);
            }
        }

        @Override
        public void onExchangeDone(Route route, Exchange exchange) {
            try {
                if (exclude(route.getEndpoint(), exchange.getContext())) {
                    LOG.debug("Tracing: endpoint {} is explicitly excluded, skipping.", route.getEndpoint());
                } else {
                    endSpan(exchange, route.getEndpoint());
                }
            } catch (Exception t) {
                LOG.warn("Tracing: Failed to capture tracing data. This exception is ignored.", t);
            }
        }
    }

    private final class TracingLogListener implements LogListener {
        @Override
        public String onLog(Exchange exchange, CamelLogger camelLogger, String message) {
            try {
                Span span = spanStorageManager.peek(exchange);
                if (span != null) {
                    Map<String, String> fields = new HashMap<>();
                    fields.put("message", message);
                    span.log(fields);
                }
            } catch (Exception t) {
                // This exception is ignored
                LOG.warn("Tracing: Failed to capture tracing data. This exception is ignored.", t);
            }
            return message;
        }
    }

    private void beginSpan(Exchange exchange, Endpoint endpoint) throws Exception {
        SpanDecorator spanDecorator = spanDecoratorManager.get(endpoint);
        Span parentSpan = spanStorageManager.peek(exchange);
        if (parentSpan == null) {
            // attempt to extract any potential upstream trace in
            // a distributed environment
            spanPropagationManager.extract(spanDecorator.getExtractor(exchange));
        }
        String spanName = spanDecorator.getOperationName(exchange, endpoint);
        Span span = spanLifecycleManager.create(spanName);
        spanDecorator.beforeTracingEvent(span, exchange, endpoint);
        spanLifecycleManager.activate(span);
        spanStorageManager.push(exchange, span);
        spanPropagationManager.inject(span, spanDecorator.getInjector(exchange));
        LOG.debug("Started span: {}", span);
    }

    private void beginSpan(Exchange exchange) throws Exception {
        Span parentSpan = spanStorageManager.peek(exchange);
        if (parentSpan != null) {
            // there is some inconsistency
            LOG.warn("Tracing parent should be null but found instead: {}", parentSpan);
        }
        Span span = spanLifecycleManager.create(exchange.getExchangeId());
        spanLifecycleManager.activate(span);
        spanStorageManager.push(exchange, span);
        spanPropagationManager.inject(span);
        LOG.debug("Started span: {}", span);
    }

    private void endSpan(Exchange exchange, Endpoint endpoint) throws Exception {
        Span span = spanStorageManager.pull(exchange);
        if (span == null) {
            LOG.warn("Could not find managed span for exchange: {}", endpoint);
            return;
        }
        SpanDecorator spanDecorator = spanDecoratorManager.get(endpoint);
        spanDecorator.afterTracingEvent(span, exchange);
        spanLifecycleManager.deactivate(span);
        spanLifecycleManager.close(span);
        LOG.debug("Stopped span: {}", span);
    }

    private void endSpan(Exchange exchange) throws Exception {
        Span span = spanStorageManager.pull(exchange);
        spanLifecycleManager.deactivate(span);
        spanLifecycleManager.close(span);
        LOG.debug("Stopped span: {}", span);
    }

}

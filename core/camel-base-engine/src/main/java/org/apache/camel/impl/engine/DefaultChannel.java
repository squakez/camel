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
package org.apache.camel.impl.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.camel.AsyncProcessor;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Channel;
import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.NamedRoute;
import org.apache.camel.Processor;
import org.apache.camel.Route;
import org.apache.camel.impl.debugger.BacklogTracer;
import org.apache.camel.impl.debugger.DefaultBacklogDebugger;
import org.apache.camel.spi.BacklogDebugger;
import org.apache.camel.spi.Debugger;
import org.apache.camel.spi.ErrorHandlerRedeliveryCustomizer;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.camel.spi.InterceptableProcessor;
import org.apache.camel.spi.ManagementInterceptStrategy;
import org.apache.camel.spi.MessageHistoryFactory;
import org.apache.camel.spi.Tracer;
import org.apache.camel.spi.WrapAwareProcessor;
import org.apache.camel.support.OrderedComparator;
import org.apache.camel.support.PatternHelper;
import org.apache.camel.support.PluginHelper;
import org.apache.camel.support.service.ServiceHelper;
import org.apache.camel.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultChannel is the default {@link Channel}.
 * <p/>
 * The current implementation is just a composite containing the interceptors and error handler that beforehand was
 * added to the route graph directly. <br/>
 * With this {@link Channel} we can in the future implement better strategies for routing the {@link Exchange} in the
 * route graph, as we have a {@link Channel} between each and every node in the graph.
 */
public class DefaultChannel extends CamelInternalProcessor implements Channel {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultChannel.class);

    private Processor errorHandler;
    // the next processor (non wrapped)
    private Processor nextProcessor;
    // the real output to invoke that has been wrapped
    private Processor output;
    private ManagementInterceptStrategy.InstrumentationProcessor<?> instrumentationProcessor;
    private Route route;

    public DefaultChannel(CamelContext camelContext) {
        super(camelContext);
    }

    @Override
    public Processor getOutput() {
        // the errorHandler is already decorated with interceptors
        // so it contain the entire chain of processors, so we can safely use it directly as output
        // if no error handler provided we use the output
        // the error handlers, interceptors, etc. woven in at design time
        return errorHandler != null ? errorHandler : output;
    }

    @Override
    public boolean hasNext() {
        return nextProcessor != null;
    }

    @Override
    public List<Processor> next() {
        if (!hasNext()) {
            return null;
        }
        List<Processor> answer = new ArrayList<>(1);
        answer.add(nextProcessor);
        return answer;
    }

    public void setOutput(Processor output) {
        this.output = output;
    }

    @Override
    public Processor getNextProcessor() {
        return nextProcessor;
    }

    @Override
    public void setErrorHandler(Processor errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public Processor getErrorHandler() {
        return errorHandler;
    }

    @Override
    public Route getRoute() {
        return route;
    }

    @Override
    protected void doStart() throws Exception {
        // do not call super as we want to be in control here of the lifecycle

        // the output has now been created, so assign the output as the processor
        setProcessor(getOutput());
        ServiceHelper.startService(errorHandler, output);
    }

    @Override
    protected void doStop() throws Exception {
        // do not call super as we want to be in control here of the lifecycle

        // only stop services if not context scoped (as context scoped is reused by others)
        ServiceHelper.stopService(output, errorHandler);
    }

    @Override
    protected void doShutdown() throws Exception {
        // do not call super as we want to be in control here of the lifecycle

        ServiceHelper.stopAndShutdownServices(output, errorHandler);
    }

    @Override
    public void initChannel(
            Route route,
            NamedNode definition,
            NamedNode childDefinition,
            List<InterceptStrategy> interceptors,
            Processor nextProcessor,
            NamedRoute routeDefinition,
            boolean first)
            throws Exception {
        this.route = route;
        this.nextProcessor = nextProcessor;

        // init CamelContextAware as early as possible on nextProcessor
        CamelContextAware.trySetCamelContext(nextProcessor, camelContext);

        // the definition to wrap should be the fine grained,
        // so if a child is set then use it, if not then its the original output used
        NamedNode targetOutputDef = childDefinition != null ? childDefinition : definition;
        LOG.trace("Initialize channel for target: {}", targetOutputDef);

        // setup instrumentation processor for management (jmx)
        // this is later used in postInitChannel as we need to setup the error handler later as well
        ManagementInterceptStrategy managed = route.getManagementInterceptStrategy();
        if (managed != null) {
            instrumentationProcessor = managed.createProcessor(targetOutputDef, nextProcessor);
        }

        // then wrap the output with the tracer and debugger (debugger first,
        // as we do not want regular tracer to trace the debugger)
        if (camelContext.isDebugStandby() || route.isDebugging()) {
            final Debugger customDebugger = camelContext.getDebugger();
            final MessageHistoryFactory messageHistoryFactory = camelContext.getMessageHistoryFactory();
            if (customDebugger != null) {
                // use custom debugger
                addAdvice(new MessageHistoryAdvice(messageHistoryFactory, targetOutputDef));
                addAdvice(new DebuggerAdvice(customDebugger, nextProcessor, targetOutputDef));
            }
            BacklogDebugger debugger = getBacklogDebugger(camelContext, customDebugger == null);
            if (debugger != null) {
                // use backlog debugger
                if (!camelContext.hasService(debugger)) {
                    camelContext.addService(debugger);
                }
                // skip debugging inside rest-dsl (just a tiny facade) or kamelets / route-templates
                boolean skip = routeDefinition != null
                        && (routeDefinition.isCreatedFromRest() || routeDefinition.isCreatedFromTemplate());
                if (!skip && routeDefinition != null) {
                    backlogDebuggerSetupInitialBreakpoints(definition, routeDefinition, first, debugger, targetOutputDef);
                    if (first && debugger.isSingleStepIncludeStartEnd()) {
                        // debugger captures message history, and we need to capture history of incoming
                        addAdvice(
                                new MessageHistoryAdvice(camelContext.getMessageHistoryFactory(), routeDefinition.getInput()));
                        // add breakpoint on route input instead of first node
                        addAdvice(new BacklogDebuggerAdvice(debugger, nextProcessor, routeDefinition.getInput()));
                    }
                    addAdvice(new MessageHistoryAdvice(messageHistoryFactory, targetOutputDef));
                    addAdvice(new BacklogDebuggerAdvice(debugger, nextProcessor, targetOutputDef));
                }
            }
        }

        if (camelContext.isBacklogTracingStandby() || route.isBacklogTracing()) {
            // add jmx backlog tracer
            BacklogTracer backlogTracer = getOrCreateBacklogTracer(camelContext);
            addAdvice(new BacklogTracerAdvice(camelContext, backlogTracer, targetOutputDef, routeDefinition, first));
        }
        if (route.isTracing() || camelContext.isTracingStandby()) {
            // add logger tracer
            Tracer tracer = camelContext.getTracer();
            addAdvice(new TracingAdvice(camelContext, tracer, targetOutputDef, routeDefinition, first));
        }

        // debugger will automatically include message history
        boolean debugging = camelContext.isDebugStandby() || route.isDebugging();
        if (!debugging && route.isMessageHistory()) {
            final MessageHistoryFactory messageHistoryFactory = camelContext.getMessageHistoryFactory();
            // add message history advice (when not debugging)
            addAdvice(new MessageHistoryAdvice(messageHistoryFactory, targetOutputDef));
        }
        // add advice that keeps track of which node is processing
        addAdvice(new NodeHistoryAdvice(targetOutputDef));

        // sort interceptors according to ordered
        interceptors.sort(OrderedComparator.get());
        // reverse list so the first will be wrapped last, as it would then be first being invoked
        Collections.reverse(interceptors);
        // wrap the output with the configured interceptors
        Processor target = nextProcessor;
        boolean skip = target instanceof InterceptableProcessor && !((InterceptableProcessor) target).canIntercept();
        if (!skip) {
            for (InterceptStrategy strategy : interceptors) {
                Processor next = target == nextProcessor ? null : nextProcessor;
                // use the fine grained definition (eg the child if available). Its always possible to get back to the parent
                Processor wrapped
                        = strategy.wrapProcessorInInterceptors(route.getCamelContext(), targetOutputDef, target, next);
                if (!(wrapped instanceof AsyncProcessor)) {
                    LOG.warn("Interceptor: {} at: {} does not return an AsyncProcessor instance."
                             + " This causes the asynchronous routing engine to not work as optimal as possible."
                             + " See more details at the InterceptStrategy javadoc."
                             + " Camel will use a bridge to adapt the interceptor to the asynchronous routing engine,"
                             + " but its not the most optimal solution. Please consider changing your interceptor to comply.",
                            strategy, definition);
                }
                if (!(wrapped instanceof WrapAwareProcessor)) {
                    // wrap the target so it becomes a service and we can manage its lifecycle
                    wrapped = PluginHelper.getInternalProcessorFactory(camelContext)
                            .createWrapProcessor(wrapped, target);
                }
                target = wrapped;
            }
        }

        if (route.isStreamCaching()) {
            addAdvice(new StreamCachingAdvice(camelContext.getStreamCachingStrategy()));
        }

        if (route.getDelayer() != null && route.getDelayer() > 0) {
            addAdvice(new DelayerAdvice(route.getDelayer()));
        }

        // sets the delegate to our wrapped output
        output = target;
    }

    @Override
    public void postInitChannel() throws Exception {
        // if jmx was enabled for the processor then either add as advice or wrap and change the processor
        // on the error handler. See more details in the class javadoc of InstrumentationProcessor
        if (instrumentationProcessor != null) {
            boolean redeliveryPossible = false;
            if (errorHandler instanceof ErrorHandlerRedeliveryCustomizer erh) {
                redeliveryPossible = erh.determineIfRedeliveryIsEnabled();
                if (redeliveryPossible) {
                    // okay we can redeliver then we need to change the output in the error handler
                    // to use us which we then wrap the call so we can capture before/after for redeliveries as well
                    Processor currentOutput = erh.getOutput();
                    instrumentationProcessor.setProcessor(currentOutput);
                    erh.changeOutput(instrumentationProcessor);
                }
            }
            if (!redeliveryPossible) {
                // optimise to use advice as we cannot redeliver
                addAdvice(CamelInternalProcessor.wrap(instrumentationProcessor));
            }
        }
    }

    private static BacklogTracer getOrCreateBacklogTracer(CamelContext camelContext) {
        BacklogTracer tracer = null;
        if (camelContext.getRegistry() != null) {
            // lookup in registry
            Map<String, BacklogTracer> map = camelContext.getRegistry().findByTypeWithName(BacklogTracer.class);
            if (map.size() == 1) {
                tracer = map.values().iterator().next();
            }
        }
        if (tracer == null) {
            tracer = camelContext.getCamelContextExtension().getContextPlugin(BacklogTracer.class);
        }
        if (tracer == null) {
            tracer = BacklogTracer.createTracer(camelContext);
            tracer.setEnabled(camelContext.isBacklogTracing() != null && camelContext.isBacklogTracing());
            tracer.setStandby(camelContext.isBacklogTracingStandby());
            tracer.setTraceTemplates(camelContext.isBacklogTracingTemplates());
            tracer.setTraceRests(camelContext.isBacklogTracingRests());
            camelContext.getCamelContextExtension().addContextPlugin(BacklogTracer.class, tracer);
        }
        return tracer;
    }

    /**
     * @param  camelContext   the camel context from which the {@link BacklogDebugger} should be found.
     * @param  createIfAbsent indicates whether a {@link BacklogDebugger} should be created if it cannot be found
     * @return                the instance of {@link BacklogDebugger} that could be found in the context or that was
     *                        created if {@code createIfAbsent} is set to {@code true}, {@code null} otherwise.
     */
    private static BacklogDebugger getBacklogDebugger(CamelContext camelContext, boolean createIfAbsent) {
        BacklogDebugger debugger = null;
        if (camelContext.getRegistry() != null) {
            // lookup in registry
            Map<String, BacklogDebugger> map = camelContext.getRegistry().findByTypeWithName(BacklogDebugger.class);
            if (map.size() == 1) {
                debugger = map.values().iterator().next();
            }
        }
        if (debugger == null) {
            debugger = camelContext.hasService(BacklogDebugger.class);
        }
        if (debugger == null && createIfAbsent) {
            // fallback to use the default backlog debugger
            debugger = DefaultBacklogDebugger.createDebugger(camelContext);
        }
        return debugger;
    }

    private void backlogDebuggerSetupInitialBreakpoints(
            NamedNode definition, NamedRoute routeDefinition, boolean first,
            BacklogDebugger debugger, NamedNode targetOutputDef) {
        // setup initial breakpoints
        if (debugger.getInitialBreakpoints() != null) {
            boolean match = false;
            String id = definition.getId();
            for (String pattern : debugger.getInitialBreakpoints().split(",")) {
                pattern = pattern.trim();
                match |= BacklogDebugger.BREAKPOINT_ALL_ROUTES.equals(pattern) && first;
                if (!match) {
                    match = PatternHelper.matchPattern(id, pattern);
                }
                // eip kind so you can match with (setBody*)
                if (!match) {
                    match = PatternHelper.matchPattern(definition.getShortName(), pattern);
                }
                // location and line number
                if (!match && pattern.contains(":")) {
                    // try with line number and location
                    String pnum = StringHelper.afterLast(pattern, ":");
                    if (pnum != null) {
                        String ploc = StringHelper.beforeLast(pattern, ":");
                        String loc = definition.getLocation();
                        // strip schema
                        if (loc != null && loc.contains(":")) {
                            loc = StringHelper.after(loc, ":");
                        }
                        String num = String.valueOf(definition.getLineNumber());
                        if (PatternHelper.matchPattern(loc, ploc) && pnum.equals(num)) {
                            match = true;
                        }
                    }
                }
                // line number only
                if (!match) {
                    Integer pnum = camelContext.getTypeConverter().tryConvertTo(Integer.class, pattern);
                    if (pnum != null) {
                        int num = definition.getLineNumber();
                        if (num == pnum) {
                            match = true;
                        }
                    }
                }
            }
            if (match) {
                if (first && debugger.isSingleStepIncludeStartEnd()) {
                    // we want route to be breakpoint (use input)
                    id = routeDefinition.getInput().getId();
                    debugger.addBreakpoint(id);
                    LOG.debug("BacklogDebugger added breakpoint: {}", id);
                } else {
                    // first output should also be breakpoint
                    id = targetOutputDef.getId();
                    debugger.addBreakpoint(id);
                    LOG.debug("BacklogDebugger added breakpoint: {}", id);
                }
            }
        }
    }

    @Override
    public String toString() {
        // just output the next processor as all the interceptors and error handler is just too verbose
        return "Channel[" + nextProcessor + "]";
    }

}

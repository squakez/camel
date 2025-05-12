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
package org.apache.camel.micrometer.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.apache.camel.api.management.ManagedResource;
import org.apache.camel.spi.Configurer;
import org.apache.camel.spi.annotations.JdkService;
import org.apache.camel.support.CamelContextHelper;
import org.apache.camel.telemetry.Span;
import org.apache.camel.telemetry.SpanContextPropagationExtractor;
import org.apache.camel.telemetry.SpanContextPropagationInjector;
import org.apache.camel.telemetry.SpanLifecycleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JdkService("micrometer-observability-tracer")
@Configurer
@ManagedResource(description = "MicrometerObservabilityTracer")
public class MicrometerObservabilityTracer extends org.apache.camel.telemetry.Tracer {

    private static final Logger LOG = LoggerFactory.getLogger(MicrometerObservabilityTracer.class);

    private Tracer tracer;
    private ObservationRegistry observationRegistry;

    @Override
    protected void initTracer() {
        if (tracer == null) {
            tracer = CamelContextHelper.findSingleByType(getCamelContext(), Tracer.class);
        }
        if (observationRegistry == null) {
            observationRegistry = CamelContextHelper.findSingleByType(getCamelContext(), ObservationRegistry.class);
        }
        if (observationRegistry == null) {
            // No Observation Registry is available, so setup Noop
            observationRegistry = ObservationRegistry.NOOP;
        }
        if (tracer == null) {
            tracer = Tracer.NOOP;
        }

        System.out.println("*********** Using registry " + observationRegistry);
        System.out.println("*********** Using tracer " + tracer);

        this.setSpanLifecycleManager(new MicrometerObservabilitySpanLifecycleManager(observationRegistry));
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        LOG.info("Micrometer Observability enabled");
    }

    private class MicrometerObservabilitySpanLifecycleManager implements SpanLifecycleManager {

        private ObservationRegistry observationRegistry;

        private MicrometerObservabilitySpanLifecycleManager(ObservationRegistry obsRegistry) {
            this.observationRegistry = obsRegistry;
        }

        @Override
        public Span create(String spanName, Span parent, SpanContextPropagationExtractor extractor) {
            Observation.Context context = new Observation.Context();
            Observation observation = Observation.createNotStarted(spanName, () -> context, observationRegistry);
            Tracer.SpanInScope scope = null;
            if (parent != null) {
                MicrometerObservabilitySpanAdapter microObsParentSpan = (MicrometerObservabilitySpanAdapter) parent;
                observation.parentObservation(microObsParentSpan.getObservation());
            }
            return new MicrometerObservabilitySpanAdapter(observation);
        }

        @Override
        public void activate(Span span) {
            MicrometerObservabilitySpanAdapter microObsSpan = (MicrometerObservabilitySpanAdapter) span;
            microObsSpan.activate();
        }

        @Override
        public void close(Span span) {
            MicrometerObservabilitySpanAdapter microObsSpan = (MicrometerObservabilitySpanAdapter) span;
            microObsSpan.close();
        }

        @Override
        public void deactivate(Span span) {
            MicrometerObservabilitySpanAdapter microObsSpan = (MicrometerObservabilitySpanAdapter) span;
            microObsSpan.deactivate();
        }

        @Override
        public void inject(Span span, SpanContextPropagationInjector injector) {
        }

    }

}

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

import java.io.IOException;

import io.micrometer.observation.tck.TestObservationRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.telemetry.Op;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class MicrometerObservabilityTracerTest extends MicrometerObservabilityTracerTestSupport {

    TestObservationRegistry registry = TestObservationRegistry.create();

    @Override
    protected CamelContext createCamelContext() throws Exception {
        MicrometerObservabilityTracer tst = new MicrometerObservabilityTracer();
        CamelContext context = super.createCamelContext();
        context.getRegistry().bind("MicrometerObservabilityRegistry", registry);
        CamelContextAware.trySetCamelContext(tst, context);
        tst.init(context);
        return context;
    }

    @Test
    void testRouteSingleRequest() throws IOException {
        Exchange result = template.request("direct:start", null);
        // Make sure the trace is propagated downstream
        //assertNotNull(result.getIn().getHeader("traceparent"));
        checkTraces();
    }

    private void checkTraces() {
        Assertions.assertThat(registry)
                .hasNumberOfObservationsEqualTo(3)
                .doesNotHaveAnyRemainingCurrentObservation();

        // Validate span completion
        Assertions.assertThat(registry)
                .hasAnObservation(t -> {
                    t.hasNameEqualTo("start");
                    t.hasHighCardinalityKeyValue("op", Op.EVENT_SENT.toString());
                });
        Assertions.assertThat(registry)
                .hasAnObservation(t -> {
                    t.hasNameEqualTo("start");
                    t.hasHighCardinalityKeyValue("op", Op.EVENT_RECEIVED.toString());
                });
        Assertions.assertThat(registry)
                .hasObservationWithNameEqualTo("log")
                .that()
                .hasBeenStarted()
                .hasBeenStopped();
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                        .routeId("start")
                        .log("A message")
                        .to("log:info");
            }
        };
    }

}

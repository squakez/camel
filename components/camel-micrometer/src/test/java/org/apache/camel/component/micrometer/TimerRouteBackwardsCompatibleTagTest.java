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
package org.apache.camel.component.micrometer;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.camel.BindToRegistry;
import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringTest;
import org.apache.camel.test.spring.junit5.CamelSpringTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import static org.apache.camel.component.micrometer.MicrometerConstants.HEADER_METRIC_NAME;
import static org.apache.camel.component.micrometer.MicrometerConstants.HEADER_TIMER_ACTION;
import static org.apache.camel.component.micrometer.MicrometerConstants.METRICS_REGISTRY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@CamelSpringTest
public class TimerRouteBackwardsCompatibleTagTest extends CamelSpringTestSupport {

    private static final long DELAY = 20L;

    @EndpointInject("mock:out")
    private MockEndpoint endpoint;

    @Produce("direct:in-1")
    private ProducerTemplate producer1;

    @Produce("direct:in-2")
    private ProducerTemplate producer2;

    @Produce("direct:in-3")
    private ProducerTemplate producer3;

    @BindToRegistry(METRICS_REGISTRY_NAME)
    private MeterRegistry registry = new SimpleMeterRegistry();

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:in-1")
                        .setHeader(HEADER_METRIC_NAME, constant("B"))
                        .to("micrometer:timer:A?action=start")
                        .delay(DELAY)
                        .setHeader(HEADER_METRIC_NAME, constant("B"))
                        .to("micrometer:timer:A?action=stop")
                        .to("mock:out");

                from("direct:in-2")
                        .setHeader(HEADER_TIMER_ACTION, constant(MicrometerTimerAction.start))
                        .to("micrometer:timer:A")
                        .delay(DELAY)
                        .setHeader(HEADER_TIMER_ACTION, constant(MicrometerTimerAction.stop))
                        .to("micrometer:timer:A")
                        .to("mock:out");

                from("direct:in-3")
                        .to("micrometer:timer:C?action=start")
                        .delay(DELAY)
                        // backwards compatible tags= parameter
                        .to("micrometer:timer:C?action=stop&tags=a=${body}")
                        .to("mock:out");
            }
        };
    }

    @Override
    protected AbstractApplicationContext createApplicationContext() {
        return new AnnotationConfigApplicationContext();
    }

    @Override
    public void doPostTearDown() {
        endpoint.reset();
    }

    @Test
    public void testOverrideMetricsName() throws Exception {
        Object body = new Object();
        endpoint.expectedBodiesReceived(body);
        producer1.sendBody(body);
        Timer timer = registry.find("B").timer();
        assertEquals(1L, timer.count());
        assertTrue(timer.max(TimeUnit.MILLISECONDS) > 0.0D);
        endpoint.assertIsSatisfied();
    }

    @Test
    public void testOverrideNoAction() throws Exception {
        Object body = new Object();
        endpoint.expectedBodiesReceived(body);
        producer2.sendBody(body);
        Timer timer = registry.find("A").timer();
        assertEquals(1L, timer.count());
        assertTrue(timer.max(TimeUnit.MILLISECONDS) > 0.0D);
        endpoint.assertIsSatisfied();
    }

    @Test
    public void testNormal() throws Exception {
        int count = 10;
        String body = "Hello";
        endpoint.expectedMessageCount(count);
        for (int i = 0; i < count; i++) {
            producer3.sendBody(body);
        }
        Timer timer = registry.find("C").timer();
        assertEquals(count, timer.count());
        assertTrue(timer.max(TimeUnit.MILLISECONDS) > DELAY);
        assertTrue(timer.mean(TimeUnit.MILLISECONDS) > DELAY);
        assertTrue(timer.totalTime(TimeUnit.MILLISECONDS) > DELAY * count);
        assertEquals(body, timer.getId().getTag("a"));
        endpoint.assertIsSatisfied();
    }
}

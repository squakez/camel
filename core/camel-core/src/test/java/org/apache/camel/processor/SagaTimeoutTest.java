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
package org.apache.camel.processor;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.SagaCompletionMode;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.saga.InMemorySagaService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SagaTimeoutTest extends ContextTestSupport {

    @Test
    public void testTimeoutCalledCorrectly() throws Exception {
        MockEndpoint compensate = getMockEndpoint("mock:compensate");
        compensate.expectedMessageCount(1);
        compensate.expectedHeaderReceived("id", "myid");

        MockEndpoint end = getMockEndpoint("mock:end");
        end.expectedMessageCount(1);

        template.sendBody("direct:saga", "Hello");

        end.assertIsSatisfied();
        compensate.assertIsSatisfied();
    }

    @Test
    public void testTimeoutHasNoEffectIfCompleted() throws Exception {
        MockEndpoint compensate = getMockEndpoint("mock:compensate");
        compensate.expectedMessageCount(1);
        compensate.setResultWaitTime(500);

        MockEndpoint complete = getMockEndpoint("mock:complete");
        complete.expectedMessageCount(1);
        complete.expectedHeaderReceived("id", "myid");

        MockEndpoint end = getMockEndpoint("mock:end");
        end.expectedMessageCount(1);

        template.sendBody("direct:saga-auto", "Hello");

        end.assertIsSatisfied();
        complete.assertIsSatisfied();
        compensate.assertIsNotSatisfied();
    }

    @Test
    public void testTimeoutMultiParticipants() throws Exception {
        MockEndpoint compensate = getMockEndpoint("mock:compensate");
        compensate.expectedMessageCount(1);

        MockEndpoint complete = getMockEndpoint("mock:complete");
        complete.expectedMessageCount(0);

        MockEndpoint end = getMockEndpoint("mock:end");
        end.expectedMessageCount(1);

        CamelExecutionException ex = assertThrows(CamelExecutionException.class,
                () -> {
                    template.sendBody("direct:saga-multi-participants", "Hello");
                });

        String msg1 = "Cannot begin: status is COMPENSATING";
        String msg2 = "Cannot begin: status is COMPENSATED";
        String msg = ex.getCause().getMessage();
        assertTrue(msg.equals(msg1) || msg.equals(msg2));

        end.assertIsSatisfied();
        complete.assertIsSatisfied();
        compensate.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                context.addService(new InMemorySagaService());

                from("direct:saga").saga().timeout(100, TimeUnit.MILLISECONDS).option("id", constant("myid"))
                        .completionMode(SagaCompletionMode.MANUAL)
                        .compensation("mock:compensate").to("mock:end");

                from("direct:saga-auto").saga().timeout(350, TimeUnit.MILLISECONDS).option("id", constant("myid"))
                        .compensation("mock:compensate").completion("mock:complete")
                        .to("mock:end");

                from("direct:saga-multi-participants")
                        .process(exchange -> {
                            exchange.getMessage().setHeader("id", UUID.randomUUID().toString());
                        })
                        .saga()
                        .propagation(SagaPropagation.REQUIRES_NEW)
                        .to("direct:service1")
                        .to("direct:service2");

                from("direct:service1")
                        .saga().option("id", header("id"))
                        .propagation(SagaPropagation.MANDATORY).timeout(100, TimeUnit.MILLISECONDS)
                        .compensation("mock:compensate").completion("mock:complete")
                        .delay(300L)
                        .to("mock:end");

                from("direct:service2")
                        .saga().option("id", header("id"))
                        .propagation(SagaPropagation.MANDATORY).timeout(500, TimeUnit.MILLISECONDS)
                        .compensation("mock:compensate").completion("mock:complete")
                        .to("mock:end");
            }
        };
    }

}

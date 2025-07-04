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
package org.apache.camel.component.aws2.kinesis;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.health.HealthCheck;
import org.apache.camel.health.HealthCheckHelper;
import org.apache.camel.health.HealthCheckRegistry;
import org.apache.camel.health.HealthCheckRepository;
import org.apache.camel.impl.health.DefaultHealthCheckRegistry;
import org.apache.camel.test.infra.aws.common.services.AWSService;
import org.apache.camel.test.infra.aws2.clients.AWSSDKClientUtils;
import org.apache.camel.test.infra.aws2.services.AWSServiceFactory;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.awaitility.Awaitility.await;

@DisabledIfSystemProperty(named = "ci.env.name", matches = ".*", disabledReason = "Flaky on GitHub Actions")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Kinesis2ConsumerHealthCustomClientIT extends CamelTestSupport {

    @RegisterExtension
    public static AWSService service = AWSServiceFactory.createSingletonS3Service();

    CamelContext context;

    @Override
    protected CamelContext createCamelContext() throws Exception {
        context = super.createCamelContext();
        context.getPropertiesComponent().setLocation("ref:prop");
        Kinesis2Component component = new Kinesis2Component(context);
        component.getConfiguration().setAmazonKinesisClient(AWSSDKClientUtils.newKinesisClient());
        component.init();
        context.addComponent("aws2-kinesis", component);

        HealthCheckRegistry registry = new DefaultHealthCheckRegistry();
        registry.setCamelContext(context);
        Object hc = registry.resolveById("context");
        registry.register(hc);
        hc = registry.resolveById("routes");
        registry.register(hc);
        hc = registry.resolveById("consumers");
        registry.register(hc);
        HealthCheckRepository hcr = (HealthCheckRepository) registry.resolveById("producers");
        hcr.setEnabled(true);
        registry.register(hcr);
        context.getCamelContextExtension().addContextPlugin(HealthCheckRegistry.class, registry);

        return context;
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {

            @Override
            public void configure() {
                from("aws2-kinesis://stream").startupOrder(2).log("${body}").routeId("test-health-it");
            }
        };
    }

    @Test
    @Order(1)
    public void testLiveness() {
        Collection<HealthCheck.Result> res = HealthCheckHelper.invokeLiveness(context);
        boolean up = res.stream().allMatch(r -> r.getState().equals(HealthCheck.State.UP));
        Assertions.assertTrue(up, "liveness check");
    }

    @Test
    @Order(2)
    public void testReadinessWhenDown() {
        // health-check readiness should be down
        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            Collection<HealthCheck.Result> res2 = HealthCheckHelper.invokeReadiness(context);
            boolean down = res2.stream().allMatch(r -> r.getState().equals(HealthCheck.State.DOWN));

            Assertions.assertTrue(down, "liveness check");
        });

    }
}

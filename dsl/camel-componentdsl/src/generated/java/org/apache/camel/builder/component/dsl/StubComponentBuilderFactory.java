/* Generated by camel build tools - do NOT edit this file! */
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
package org.apache.camel.builder.component.dsl;

import javax.annotation.processing.Generated;
import org.apache.camel.Component;
import org.apache.camel.builder.component.AbstractComponentBuilder;
import org.apache.camel.builder.component.ComponentBuilder;
import org.apache.camel.component.stub.StubComponent;

/**
 * Stub out any physical endpoints while in development or testing.
 * 
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface StubComponentBuilderFactory {

    /**
     * Stub (camel-stub)
     * Stub out any physical endpoints while in development or testing.
     * 
     * Category: core,testing
     * Since: 2.10
     * Maven coordinates: org.apache.camel:camel-stub
     * 
     * @return the dsl builder
     */
    static StubComponentBuilder stub() {
        return new StubComponentBuilderImpl();
    }

    /**
     * Builder for the Stub component.
     */
    interface StubComponentBuilder extends ComponentBuilder<StubComponent> {
    
        
        /**
         * Allows for bridging the consumer to the Camel routing Error Handler,
         * which mean any exceptions (if possible) occurred while the Camel
         * consumer is trying to pickup incoming messages, or the likes, will
         * now be processed as a message and handled by the routing Error
         * Handler. Important: This is only possible if the 3rd party component
         * allows Camel to be alerted if an exception was thrown. Some
         * components handle this internally only, and therefore
         * bridgeErrorHandler is not possible. In other situations we may
         * improve the Camel component to hook into the 3rd party component and
         * make this possible for future releases. By default the consumer will
         * use the org.apache.camel.spi.ExceptionHandler to deal with
         * exceptions, that will be logged at WARN or ERROR level and ignored.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: consumer
         * 
         * @param bridgeErrorHandler the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder bridgeErrorHandler(boolean bridgeErrorHandler) {
            doSetProperty("bridgeErrorHandler", bridgeErrorHandler);
            return this;
        }
    
        
        /**
         * Sets the default number of concurrent threads processing exchanges.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 1
         * Group: consumer
         * 
         * @param concurrentConsumers the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder concurrentConsumers(int concurrentConsumers) {
            doSetProperty("concurrentConsumers", concurrentConsumers);
            return this;
        }
    
        
        /**
         * The timeout (in milliseconds) used when polling. When a timeout
         * occurs, the consumer can check whether it is allowed to continue
         * running. Setting a lower value allows the consumer to react more
         * quickly upon shutdown.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 1000
         * Group: consumer (advanced)
         * 
         * @param defaultPollTimeout the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder defaultPollTimeout(int defaultPollTimeout) {
            doSetProperty("defaultPollTimeout", defaultPollTimeout);
            return this;
        }
    
        
        /**
         * Whether the producer should be started lazy (on the first message).
         * By starting lazy you can use this to allow CamelContext and routes to
         * startup in situations where a producer may otherwise fail during
         * starting and cause the route to fail being started. By deferring this
         * startup to be lazy then the startup failure can be handled during
         * routing messages via Camel's routing error handlers. Beware that when
         * the first message is processed then creating and starting the
         * producer may take a little time and prolong the total processing time
         * of the processing.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param lazyStartProducer the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder lazyStartProducer(boolean lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
            return this;
        }
    
        
        /**
         * Whether a thread that sends messages to a full SEDA queue will block
         * until the queue's capacity is no longer exhausted. By default, an
         * exception will be thrown stating that the queue is full. By enabling
         * this option, the calling thread will instead block and wait until the
         * message can be accepted.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer (advanced)
         * 
         * @param defaultBlockWhenFull the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder defaultBlockWhenFull(boolean defaultBlockWhenFull) {
            doSetProperty("defaultBlockWhenFull", defaultBlockWhenFull);
            return this;
        }
    
        
        /**
         * Whether a thread that sends messages to a full SEDA queue will be
         * discarded. By default, an exception will be thrown stating that the
         * queue is full. By enabling this option, the calling thread will give
         * up sending and continue, meaning that the message was not sent to the
         * SEDA queue.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer (advanced)
         * 
         * @param defaultDiscardWhenFull the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder defaultDiscardWhenFull(boolean defaultDiscardWhenFull) {
            doSetProperty("defaultDiscardWhenFull", defaultDiscardWhenFull);
            return this;
        }
    
        /**
         * Whether a thread that sends messages to a full SEDA queue will block
         * until the queue's capacity is no longer exhausted. By default, an
         * exception will be thrown stating that the queue is full. By enabling
         * this option, where a configured timeout can be added to the block
         * case. Using the offer(timeout) method of the underlining java queue.
         * 
         * The option is a: &lt;code&gt;long&lt;/code&gt; type.
         * 
         * Group: producer (advanced)
         * 
         * @param defaultOfferTimeout the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder defaultOfferTimeout(long defaultOfferTimeout) {
            doSetProperty("defaultOfferTimeout", defaultOfferTimeout);
            return this;
        }
    
        
        /**
         * Whether autowiring is enabled. This is used for automatic autowiring
         * options (the option must be marked as autowired) by looking up in the
         * registry to find if there is a single instance of matching type,
         * which then gets configured on the component. This can be used for
         * automatic configuring JDBC data sources, JMS connection factories,
         * AWS Clients, etc.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: advanced
         * 
         * @param autowiredEnabled the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder autowiredEnabled(boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
    
        /**
         * Sets the default queue factory.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.seda.BlockingQueueFactory&amp;lt;org.apache.camel.Exchange&amp;gt;&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param defaultQueueFactory the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder defaultQueueFactory(org.apache.camel.component.seda.BlockingQueueFactory<org.apache.camel.Exchange> defaultQueueFactory) {
            doSetProperty("defaultQueueFactory", defaultQueueFactory);
            return this;
        }
    
        
        /**
         * Sets the default maximum capacity of the SEDA queue (i.e., the number
         * of messages it can hold).
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Default: 1000
         * Group: advanced
         * 
         * @param queueSize the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder queueSize(int queueSize) {
            doSetProperty("queueSize", queueSize);
            return this;
        }
    
        
        /**
         * If shadow is enabled then the stub component will register a shadow
         * endpoint with the actual uri that refers to the stub endpoint,
         * meaning you can lookup the endpoint via both stub:kafka:cheese and
         * kafka:cheese.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: advanced
         * 
         * @param shadow the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder shadow(boolean shadow) {
            doSetProperty("shadow", shadow);
            return this;
        }
    
        /**
         * If shadow is enabled then this pattern can be used to filter which
         * components to match. Multiple patterns can be separated by comma.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param shadowPattern the value to set
         * @return the dsl builder
         */
        default StubComponentBuilder shadowPattern(java.lang.String shadowPattern) {
            doSetProperty("shadowPattern", shadowPattern);
            return this;
        }
    }

    class StubComponentBuilderImpl
            extends AbstractComponentBuilder<StubComponent>
            implements StubComponentBuilder {
        @Override
        protected StubComponent buildConcreteComponent() {
            return new StubComponent();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "bridgeErrorHandler": ((StubComponent) component).setBridgeErrorHandler((boolean) value); return true;
            case "concurrentConsumers": ((StubComponent) component).setConcurrentConsumers((int) value); return true;
            case "defaultPollTimeout": ((StubComponent) component).setDefaultPollTimeout((int) value); return true;
            case "lazyStartProducer": ((StubComponent) component).setLazyStartProducer((boolean) value); return true;
            case "defaultBlockWhenFull": ((StubComponent) component).setDefaultBlockWhenFull((boolean) value); return true;
            case "defaultDiscardWhenFull": ((StubComponent) component).setDefaultDiscardWhenFull((boolean) value); return true;
            case "defaultOfferTimeout": ((StubComponent) component).setDefaultOfferTimeout((long) value); return true;
            case "autowiredEnabled": ((StubComponent) component).setAutowiredEnabled((boolean) value); return true;
            case "defaultQueueFactory": ((StubComponent) component).setDefaultQueueFactory((org.apache.camel.component.seda.BlockingQueueFactory) value); return true;
            case "queueSize": ((StubComponent) component).setQueueSize((int) value); return true;
            case "shadow": ((StubComponent) component).setShadow((boolean) value); return true;
            case "shadowPattern": ((StubComponent) component).setShadowPattern((java.lang.String) value); return true;
            default: return false;
            }
        }
    }
}
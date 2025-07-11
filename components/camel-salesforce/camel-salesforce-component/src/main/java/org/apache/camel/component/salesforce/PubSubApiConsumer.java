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
package org.apache.camel.component.salesforce;

import java.util.Map;

import com.salesforce.eventbus.protobuf.ReplayPreset;
import org.apache.camel.AsyncCallback;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.salesforce.api.SalesforceException;
import org.apache.camel.component.salesforce.internal.client.PubSubApiClient;
import org.apache.camel.support.DefaultConsumer;
import org.apache.camel.support.service.ServiceHelper;

import static org.apache.camel.component.salesforce.SalesforceConstants.HEADER_SALESFORCE_PUBSUB_EVENT_ID;
import static org.apache.camel.component.salesforce.SalesforceConstants.HEADER_SALESFORCE_PUBSUB_REPLAY_ID;
import static org.apache.camel.component.salesforce.SalesforceConstants.HEADER_SALESFORCE_PUBSUB_RPC_ID;

public class PubSubApiConsumer extends DefaultConsumer {

    private final String topic;
    private final ReplayPreset initialReplayPreset;
    private String initialReplayId;
    private boolean fallbackToLatestReplayId;
    private final SalesforceEndpoint endpoint;

    private final int batchSize;
    private final PubSubDeserializeType deserializeType;
    private Class<?> pojoClass;
    private PubSubApiClient pubSubClient;
    private Map<String, Class<?>> eventClassMap;

    private boolean usePlainTextConnection = false;

    public PubSubApiConsumer(SalesforceEndpoint endpoint, Processor processor) throws ClassNotFoundException {
        super(endpoint, processor);
        this.endpoint = endpoint;
        this.topic = endpoint.getTopicName();
        this.initialReplayPreset = endpoint.getConfiguration().getReplayPreset();
        this.initialReplayId = endpoint.getPubSubReplayId();
        this.fallbackToLatestReplayId = endpoint.isFallbackToLatestReplayId();
        if (initialReplayPreset == ReplayPreset.CUSTOM && initialReplayId == null) {
            throw new IllegalArgumentException("pubSubReplayId option is required if ReplayPreset is CUSTOM.");
        }
        this.batchSize = endpoint.getConfiguration().getPubSubBatchSize();
        this.deserializeType = endpoint.getConfiguration().getPubSubDeserializeType();
        String pojoClassName = endpoint.getConfiguration().getPubSubPojoClass();
        if (pojoClassName != null) {
            this.pojoClass = endpoint.getCamelContext().getClassResolver().resolveMandatoryClass(pojoClassName);
        }
    }

    public void processEvent(Object recordObj, String eventId, String replayId, String rpcId) {
        final Exchange exchange = createExchange(true);
        final Message in = exchange.getIn();
        in.setBody(recordObj);
        in.setHeader(HEADER_SALESFORCE_PUBSUB_EVENT_ID, eventId);
        in.setHeader(HEADER_SALESFORCE_PUBSUB_REPLAY_ID, replayId);
        in.setHeader(HEADER_SALESFORCE_PUBSUB_RPC_ID, rpcId);
        AsyncCallback cb = defaultConsumerCallback(exchange, true);
        getAsyncProcessor().process(exchange, cb);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        if (endpoint.getComponent().getLoginConfig().isLazyLogin()) {
            throw new SalesforceException("Lazy login is not supported by salesforce consumers.", null);
        }

        this.eventClassMap = endpoint.getComponent().getEventClassMap();
        this.pubSubClient = new PubSubApiClient(
                endpoint.getComponent().getSession(), endpoint.getComponent().getLoginConfig(),
                endpoint.getComponent().getPubSubHost(), endpoint.getComponent().getPubSubPort(),
                endpoint.getConfiguration().getBackoffIncrement(), endpoint.getConfiguration().getMaxBackoff(),
                endpoint.getComponent().isPubsubAllowUseSystemProxy());
        this.pubSubClient.setUsePlainTextConnection(this.usePlainTextConnection);

        ServiceHelper.startService(pubSubClient);
        pubSubClient.subscribe(this, initialReplayPreset, initialReplayId, fallbackToLatestReplayId);
    }

    @Override
    protected void doStop() throws Exception {
        ServiceHelper.stopService(pubSubClient);
        super.doStop();
    }

    public String getTopic() {
        return topic;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public PubSubDeserializeType getDeserializeType() {
        return deserializeType;
    }

    public Map<String, Class<?>> getEventClassMap() {
        return eventClassMap;
    }

    public Class<?> getPojoClass() {
        return pojoClass;
    }

    // ability to use Plain Text (http) for test contexts
    public void setUsePlainTextConnection(boolean usePlainTextConnection) {
        this.usePlainTextConnection = usePlainTextConnection;
    }

    /**
     * This updates the initial replay id. This will only take effect after the route is restarted, and should generally
     * only be done while the route is stopped.
     *
     * @param initialReplayId
     */
    public void updateInitialReplayId(String initialReplayId) {
        this.initialReplayId = initialReplayId;
    }
}

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
package org.apache.camel.component.kafka.producer.support;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DelegatingCallback implements Callback {

    private static final Logger LOG = LoggerFactory.getLogger(DelegatingCallback.class);
    private final Callback callback1;
    private final Callback callback2;

    public DelegatingCallback(Callback callback1, Callback callback2) {
        this.callback1 = callback1;
        this.callback2 = callback2;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        try {
            callback1.onCompletion(metadata, exception);
        } catch (Exception e) {
            // ensure every callback is invoked
            LOG.warn("Error invoking 1st onCompletion. This exception is ignored.", e);
        }
        try {
            callback2.onCompletion(metadata, exception);
        } catch (Exception e) {
            // ensure every callback is invoked
            LOG.warn("Error invoking 2nd onCompletion. This exception is ignored.", e);
        }
    }
}

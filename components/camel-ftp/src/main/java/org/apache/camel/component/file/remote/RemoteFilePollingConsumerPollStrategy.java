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
package org.apache.camel.component.file.remote;

import org.apache.camel.Consumer;
import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultPollingConsumerPollStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Remote file consumer polling strategy that attempts to help recovering from lost connections.
 */
public class RemoteFilePollingConsumerPollStrategy extends DefaultPollingConsumerPollStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteFilePollingConsumerPollStrategy.class);

    @Override
    public boolean rollback(Consumer consumer, Endpoint endpoint, int retryCounter, Exception e) throws Exception {
        if (consumer instanceof RemoteFileConsumer<?> rfc) {

            // only try to recover if we are allowed to run
            if (rfc.isRunAllowed()) {
                // disconnect from the server to force it to re login at next
                // poll to recover
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Trying to recover by force disconnecting from remote server and re-connecting at next poll: {}",
                            rfc.remoteServer());
                }
                try {
                    rfc.forceDisconnect();
                } catch (Exception t) {
                    // ignore the exception
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Error occurred during force disconnecting from: {}. This exception will be ignored.",
                                rfc.remoteServer(), t);
                    }
                }
            }
        }

        return super.rollback(consumer, endpoint, retryCounter, e);
    }

}

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
package org.apache.camel.component.dhis2;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.component.dhis2.internal.Dhis2ApiCollection;
import org.apache.camel.component.dhis2.internal.Dhis2ApiName;
import org.apache.camel.spi.Metadata;
import org.apache.camel.support.component.AbstractApiComponent;
import org.hisp.dhis.integration.sdk.Dhis2ClientBuilder;
import org.hisp.dhis.integration.sdk.api.Dhis2Client;

import static org.apache.camel.util.ObjectHelper.isNotEmpty;

@org.apache.camel.spi.annotations.Component("dhis2")
public class Dhis2Component extends AbstractApiComponent<Dhis2ApiName, Dhis2Configuration, Dhis2ApiCollection> {
    @Metadata(label = "advanced")
    Dhis2Configuration configuration;

    private Dhis2Client dhis2Client;

    public Dhis2Component() {
        super(Dhis2ApiName.class, Dhis2ApiCollection.getCollection());
    }

    public Dhis2Component(CamelContext context) {
        super(context, Dhis2ApiName.class, Dhis2ApiCollection.getCollection());
    }

    @Override
    protected Dhis2ApiName getApiName(String apiNameStr)
            throws IllegalArgumentException {
        return getCamelContext().getTypeConverter().convertTo(Dhis2ApiName.class, apiNameStr);
    }

    @Override
    protected Endpoint createEndpoint(
            String uri, String methodName, Dhis2ApiName apiName,
            Dhis2Configuration endpointConfiguration) {
        endpointConfiguration.setApiName(apiName);
        endpointConfiguration.setMethodName(methodName);
        return new Dhis2Endpoint(uri, this, apiName, methodName, endpointConfiguration);
    }

    /**
     * To use the shared configuration
     */
    @Override
    public void setConfiguration(Dhis2Configuration configuration) {
        super.setConfiguration(configuration);
    }

    public Dhis2Client getClient(Dhis2Configuration endpointConfiguration) {
        if (endpointConfiguration.equals(this.configuration)) {
            lock.lock();
            try {
                if (this.dhis2Client == null) {
                    this.dhis2Client = Dhis2ClientBuilder.newClient(endpointConfiguration.getBaseApiUrl(),
                            endpointConfiguration.getUsername(), endpointConfiguration.getPassword()).build();
                }
            } finally {
                lock.unlock();
            }

            return this.dhis2Client;
        } else {
            if (endpointConfiguration.getClient() != null) {
                if (endpointConfiguration.getBaseApiUrl() != null || endpointConfiguration.getPersonalAccessToken() != null
                        || endpointConfiguration.getUsername() != null || endpointConfiguration.getPassword() != null) {
                    throw new RuntimeCamelException(
                            "Bad DHIS2 endpoint configuration: client option is mutually exclusive to baseApiUrl, username, password, and personalAccessToken. Either set (1) `client`, or (2) `baseApiUrl` and `username` and `password`, or (3) `baseApiUrl` and `personalAccessToken`");
                }

                return endpointConfiguration.getClient();
            } else {
                if (isNotEmpty(endpointConfiguration.getPersonalAccessToken())
                        && (isNotEmpty(endpointConfiguration.getUsername())
                                || isNotEmpty(endpointConfiguration.getPassword()))) {
                    throw new RuntimeCamelException(
                            "Bad DHIS2 authentication configuration: Personal access token authentication and basic authentication are mutually exclusive. Either set `personalAccessToken` or both `username` and `password`");
                }

                if (isNotEmpty(endpointConfiguration.getPersonalAccessToken())) {
                    return Dhis2ClientBuilder.newClient(endpointConfiguration.getBaseApiUrl(),
                            endpointConfiguration.getPersonalAccessToken()).build();
                } else {
                    return Dhis2ClientBuilder.newClient(endpointConfiguration.getBaseApiUrl(),
                            endpointConfiguration.getUsername(), endpointConfiguration.getPassword()).build();
                }
            }
        }
    }
}

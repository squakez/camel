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

import java.util.Map;

import io.micrometer.observation.Observation;
import io.micrometer.observation.Observation.Scope;
import org.apache.camel.telemetry.Span;

public class MicrometerObservabilitySpanAdapter implements Span {

    private static final String DEFAULT_EVENT_NAME = "log";

    private final Observation observation;
    private Scope scope;

    public MicrometerObservabilitySpanAdapter(Observation observation) {
        this.observation = observation;
    }

    @Override
    public void log(Map<String, String> fields) {
        String event = fields.get("event");
        if ("error".equalsIgnoreCase(event)) {
            setError(true);
        } else {
            this.observation.event(() -> DEFAULT_EVENT_NAME);
        }
    }

    @Override
    public void setComponent(String component) {
        this.observation.lowCardinalityKeyValue("component", component);
    }

    @Override
    public void setError(boolean isError) {
        this.observation.lowCardinalityKeyValue("error", String.valueOf(isError));
    }

    @Override
    public void setTag(String key, String value) {
        this.observation.highCardinalityKeyValue(key, value);
    }

    protected Observation getObservation() {
        return this.observation;
    }

    protected void activate() {
        this.observation.start();
        this.scope = this.observation.openScope();
    }

    protected void close() {
        scope.close();
    }

    protected void deactivate() {
        this.observation.stop();
    }

}

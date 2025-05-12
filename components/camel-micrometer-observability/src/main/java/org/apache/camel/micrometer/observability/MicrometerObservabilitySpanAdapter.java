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
import org.apache.camel.telemetry.Span;

public class MicrometerObservabilitySpanAdapter implements Span {

    private final Observation observation;

    public MicrometerObservabilitySpanAdapter(Observation observation) {
        this.observation = observation;
    }

    @Override
    public void log(Map<String, String> fields) {
        throw new UnsupportedOperationException("Unimplemented method 'log'");
    }

    @Override
    public void setComponent(String component) {
        throw new UnsupportedOperationException("Unimplemented method 'setComponent'");
    }

    @Override
    public void setError(boolean isError) {
        throw new UnsupportedOperationException("Unimplemented method 'setError'");
    }

    @Override
    public void setTag(String key, String value) {
        throw new UnsupportedOperationException("Unimplemented method 'setTag'");
    }

    protected Observation getObservation() {
        return this.observation;
    }

    protected void activate() {
        this.observation.start();
    }

    protected void close() {
        // Should we close the scope?
    }

    protected void deactivate() {
        this.observation.stop();
    }

}

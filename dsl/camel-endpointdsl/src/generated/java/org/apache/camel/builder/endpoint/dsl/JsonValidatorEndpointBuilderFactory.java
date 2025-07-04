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
package org.apache.camel.builder.endpoint.dsl;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.stream.*;
import javax.annotation.processing.Generated;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.AbstractEndpointBuilder;

/**
 * Validate JSON payloads using NetworkNT JSON Schema.
 * 
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.EndpointDslMojo")
public interface JsonValidatorEndpointBuilderFactory {

    /**
     * Builder for endpoint for the JSON Schema Validator component.
     */
    public interface JsonValidatorEndpointBuilder
            extends
                EndpointProducerBuilder {
        default AdvancedJsonValidatorEndpointBuilder advanced() {
            return (AdvancedJsonValidatorEndpointBuilder) this;
        }

        /**
         * Sets whether the context map should allow access to all details. By
         * default only the message body and headers can be accessed. This
         * option can be enabled for full access to the current Exchange and
         * CamelContext. Doing so impose a potential security risk as this opens
         * access to the full power of CamelContext API.
         * 
         * The option is a: <code>boolean</code> type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param allowContextMapAll the value to set
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder allowContextMapAll(boolean allowContextMapAll) {
            doSetProperty("allowContextMapAll", allowContextMapAll);
            return this;
        }
        /**
         * Sets whether the context map should allow access to all details. By
         * default only the message body and headers can be accessed. This
         * option can be enabled for full access to the current Exchange and
         * CamelContext. Doing so impose a potential security risk as this opens
         * access to the full power of CamelContext API.
         * 
         * The option will be converted to a <code>boolean</code> type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param allowContextMapAll the value to set
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder allowContextMapAll(String allowContextMapAll) {
            doSetProperty("allowContextMapAll", allowContextMapAll);
            return this;
        }
        /**
         * Sets whether to use resource content cache or not.
         * 
         * The option is a: <code>boolean</code> type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param contentCache the value to set
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder contentCache(boolean contentCache) {
            doSetProperty("contentCache", contentCache);
            return this;
        }
        /**
         * Sets whether to use resource content cache or not.
         * 
         * The option will be converted to a <code>boolean</code> type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param contentCache the value to set
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder contentCache(String contentCache) {
            doSetProperty("contentCache", contentCache);
            return this;
        }
        /**
         * Whether to fail if no body exists.
         * 
         * The option is a: <code>boolean</code> type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param failOnNullBody the value to set
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder failOnNullBody(boolean failOnNullBody) {
            doSetProperty("failOnNullBody", failOnNullBody);
            return this;
        }
        /**
         * Whether to fail if no body exists.
         * 
         * The option will be converted to a <code>boolean</code> type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param failOnNullBody the value to set
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder failOnNullBody(String failOnNullBody) {
            doSetProperty("failOnNullBody", failOnNullBody);
            return this;
        }
        /**
         * Whether to fail if no header exists when validating against a header.
         * 
         * The option is a: <code>boolean</code> type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param failOnNullHeader the value to set
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder failOnNullHeader(boolean failOnNullHeader) {
            doSetProperty("failOnNullHeader", failOnNullHeader);
            return this;
        }
        /**
         * Whether to fail if no header exists when validating against a header.
         * 
         * The option will be converted to a <code>boolean</code> type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param failOnNullHeader the value to set
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder failOnNullHeader(String failOnNullHeader) {
            doSetProperty("failOnNullHeader", failOnNullHeader);
            return this;
        }
        /**
         * To validate against a header instead of the message body.
         * 
         * The option is a: <code>java.lang.String</code> type.
         * 
         * Group: producer
         * 
         * @param headerName the value to set
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder headerName(String headerName) {
            doSetProperty("headerName", headerName);
            return this;
        }
    }

    /**
     * Advanced builder for endpoint for the JSON Schema Validator component.
     */
    public interface AdvancedJsonValidatorEndpointBuilder
            extends
                EndpointProducerBuilder {
        default JsonValidatorEndpointBuilder basic() {
            return (JsonValidatorEndpointBuilder) this;
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
         * The option is a: <code>boolean</code> type.
         * 
         * Default: false
         * Group: producer (advanced)
         * 
         * @param lazyStartProducer the value to set
         * @return the dsl builder
         */
        default AdvancedJsonValidatorEndpointBuilder lazyStartProducer(boolean lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
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
         * The option will be converted to a <code>boolean</code> type.
         * 
         * Default: false
         * Group: producer (advanced)
         * 
         * @param lazyStartProducer the value to set
         * @return the dsl builder
         */
        default AdvancedJsonValidatorEndpointBuilder lazyStartProducer(String lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
            return this;
        }
        /**
         * Comma-separated list of Jackson DeserializationFeature enum values
         * which will be disabled for parsing exchange body.
         * 
         * The option is a: <code>java.lang.String</code> type.
         * 
         * Group: advanced
         * 
         * @param disabledDeserializationFeatures the value to set
         * @return the dsl builder
         */
        default AdvancedJsonValidatorEndpointBuilder disabledDeserializationFeatures(String disabledDeserializationFeatures) {
            doSetProperty("disabledDeserializationFeatures", disabledDeserializationFeatures);
            return this;
        }
        /**
         * Comma-separated list of Jackson DeserializationFeature enum values
         * which will be enabled for parsing exchange body.
         * 
         * The option is a: <code>java.lang.String</code> type.
         * 
         * Group: advanced
         * 
         * @param enabledDeserializationFeatures the value to set
         * @return the dsl builder
         */
        default AdvancedJsonValidatorEndpointBuilder enabledDeserializationFeatures(String enabledDeserializationFeatures) {
            doSetProperty("enabledDeserializationFeatures", enabledDeserializationFeatures);
            return this;
        }
        /**
         * To use a custom ValidatorErrorHandler. The default error handler
         * captures the errors and throws an exception.
         * 
         * The option is a:
         * <code>org.apache.camel.component.jsonvalidator.JsonValidatorErrorHandler</code> type.
         * 
         * Group: advanced
         * 
         * @param errorHandler the value to set
         * @return the dsl builder
         */
        default AdvancedJsonValidatorEndpointBuilder errorHandler(org.apache.camel.component.jsonvalidator.JsonValidatorErrorHandler errorHandler) {
            doSetProperty("errorHandler", errorHandler);
            return this;
        }
        /**
         * To use a custom ValidatorErrorHandler. The default error handler
         * captures the errors and throws an exception.
         * 
         * The option will be converted to a
         * <code>org.apache.camel.component.jsonvalidator.JsonValidatorErrorHandler</code> type.
         * 
         * Group: advanced
         * 
         * @param errorHandler the value to set
         * @return the dsl builder
         */
        default AdvancedJsonValidatorEndpointBuilder errorHandler(String errorHandler) {
            doSetProperty("errorHandler", errorHandler);
            return this;
        }
        /**
         * The used Jackson object mapper.
         * 
         * The option is a:
         * <code>com.fasterxml.jackson.databind.ObjectMapper</code> type.
         * 
         * Group: advanced
         * 
         * @param objectMapper the value to set
         * @return the dsl builder
         */
        default AdvancedJsonValidatorEndpointBuilder objectMapper(com.fasterxml.jackson.databind.ObjectMapper objectMapper) {
            doSetProperty("objectMapper", objectMapper);
            return this;
        }
        /**
         * The used Jackson object mapper.
         * 
         * The option will be converted to a
         * <code>com.fasterxml.jackson.databind.ObjectMapper</code> type.
         * 
         * Group: advanced
         * 
         * @param objectMapper the value to set
         * @return the dsl builder
         */
        default AdvancedJsonValidatorEndpointBuilder objectMapper(String objectMapper) {
            doSetProperty("objectMapper", objectMapper);
            return this;
        }
        /**
         * To use a custom schema loader allowing for adding custom format
         * validation. The default implementation will create a schema loader
         * that tries to determine the schema version from the $schema property
         * of the specified schema.
         * 
         * The option is a:
         * <code>org.apache.camel.component.jsonvalidator.JsonUriSchemaLoader</code> type.
         * 
         * Group: advanced
         * 
         * @param uriSchemaLoader the value to set
         * @return the dsl builder
         */
        default AdvancedJsonValidatorEndpointBuilder uriSchemaLoader(org.apache.camel.component.jsonvalidator.JsonUriSchemaLoader uriSchemaLoader) {
            doSetProperty("uriSchemaLoader", uriSchemaLoader);
            return this;
        }
        /**
         * To use a custom schema loader allowing for adding custom format
         * validation. The default implementation will create a schema loader
         * that tries to determine the schema version from the $schema property
         * of the specified schema.
         * 
         * The option will be converted to a
         * <code>org.apache.camel.component.jsonvalidator.JsonUriSchemaLoader</code> type.
         * 
         * Group: advanced
         * 
         * @param uriSchemaLoader the value to set
         * @return the dsl builder
         */
        default AdvancedJsonValidatorEndpointBuilder uriSchemaLoader(String uriSchemaLoader) {
            doSetProperty("uriSchemaLoader", uriSchemaLoader);
            return this;
        }
    }

    public interface JsonValidatorBuilders {
        /**
         * JSON Schema Validator (camel-json-validator)
         * Validate JSON payloads using NetworkNT JSON Schema.
         * 
         * Category: validation
         * Since: 2.20
         * Maven coordinates: org.apache.camel:camel-json-validator
         * 
         * Syntax: <code>json-validator:resourceUri</code>
         * 
         * Path parameter: resourceUri (required)
         * Path to the resource. You can prefix with: classpath, file, http,
         * ref, or bean. classpath, file and http loads the resource using these
         * protocols (classpath is default). ref will lookup the resource in the
         * registry. bean will call a method on a bean to be used as the
         * resource. For bean you can specify the method name after dot, eg
         * bean:myBean.myMethod.
         * This option can also be loaded from an existing file, by prefixing
         * with file: or classpath: followed by the location of the file.
         * 
         * @param path resourceUri
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder jsonValidator(String path) {
            return JsonValidatorEndpointBuilderFactory.endpointBuilder("json-validator", path);
        }
        /**
         * JSON Schema Validator (camel-json-validator)
         * Validate JSON payloads using NetworkNT JSON Schema.
         * 
         * Category: validation
         * Since: 2.20
         * Maven coordinates: org.apache.camel:camel-json-validator
         * 
         * Syntax: <code>json-validator:resourceUri</code>
         * 
         * Path parameter: resourceUri (required)
         * Path to the resource. You can prefix with: classpath, file, http,
         * ref, or bean. classpath, file and http loads the resource using these
         * protocols (classpath is default). ref will lookup the resource in the
         * registry. bean will call a method on a bean to be used as the
         * resource. For bean you can specify the method name after dot, eg
         * bean:myBean.myMethod.
         * This option can also be loaded from an existing file, by prefixing
         * with file: or classpath: followed by the location of the file.
         * 
         * @param componentName to use a custom component name for the endpoint
         * instead of the default name
         * @param path resourceUri
         * @return the dsl builder
         */
        default JsonValidatorEndpointBuilder jsonValidator(String componentName, String path) {
            return JsonValidatorEndpointBuilderFactory.endpointBuilder(componentName, path);
        }

    }
    static JsonValidatorEndpointBuilder endpointBuilder(String componentName, String path) {
        class JsonValidatorEndpointBuilderImpl extends AbstractEndpointBuilder implements JsonValidatorEndpointBuilder, AdvancedJsonValidatorEndpointBuilder {
            public JsonValidatorEndpointBuilderImpl(String path) {
                super(componentName, path);
            }
        }
        return new JsonValidatorEndpointBuilderImpl(path);
    }
}
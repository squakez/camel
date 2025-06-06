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
import org.apache.camel.component.servicenow.ServiceNowComponent;

/**
 * Interact with ServiceNow via its REST API.
 * 
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface ServicenowComponentBuilderFactory {

    /**
     * ServiceNow (camel-servicenow)
     * Interact with ServiceNow via its REST API.
     * 
     * Category: api,cloud,management
     * Since: 2.18
     * Maven coordinates: org.apache.camel:camel-servicenow
     * 
     * @return the dsl builder
     */
    static ServicenowComponentBuilder servicenow() {
        return new ServicenowComponentBuilderImpl();
    }

    /**
     * Builder for the ServiceNow component.
     */
    interface ServicenowComponentBuilder extends ComponentBuilder<ServiceNowComponent> {
    
        /**
         * Component configuration.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.servicenow.ServiceNowConfiguration&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param configuration the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder configuration(org.apache.camel.component.servicenow.ServiceNowConfiguration configuration) {
            doSetProperty("configuration", configuration);
            return this;
        }
    
        
        /**
         * Set this parameter to true to return only scorecards where the
         * indicator Display field is selected. Set this parameter to all to
         * return scorecards with any Display field value. This parameter is
         * true by default.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param display the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder display(java.lang.String display) {
            doSetProperty("display", display);
            return this;
        }
    
        
        /**
         * Return the display value (true), actual value (false), or both (all)
         * for reference fields (default: false).
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: false
         * Group: producer
         * 
         * @param displayValue the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder displayValue(java.lang.String displayValue) {
            doSetProperty("displayValue", displayValue);
            return this;
        }
    
        /**
         * True to exclude Table API links for reference fields (default:
         * false).
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param excludeReferenceLink the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder excludeReferenceLink(java.lang.Boolean excludeReferenceLink) {
            doSetProperty("excludeReferenceLink", excludeReferenceLink);
            return this;
        }
    
        /**
         * Set this parameter to true to return only scorecards that are
         * favorites of the querying user.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param favorites the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder favorites(java.lang.Boolean favorites) {
            doSetProperty("favorites", favorites);
            return this;
        }
    
        /**
         * Set this parameter to true to always return all available aggregates
         * for an indicator, including when an aggregate has already been
         * applied. If a value is not specified, this parameter defaults to
         * false and returns no aggregates.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param includeAggregates the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder includeAggregates(java.lang.Boolean includeAggregates) {
            doSetProperty("includeAggregates", includeAggregates);
            return this;
        }
    
        /**
         * Set this parameter to true to return all available aggregates for an
         * indicator when no aggregate has been applied. If a value is not
         * specified, this parameter defaults to false and returns no
         * aggregates.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param includeAvailableAggregates the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder includeAvailableAggregates(java.lang.Boolean includeAvailableAggregates) {
            doSetProperty("includeAvailableAggregates", includeAvailableAggregates);
            return this;
        }
    
        /**
         * Set this parameter to true to return all available breakdowns for an
         * indicator. If a value is not specified, this parameter defaults to
         * false and returns no breakdowns.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param includeAvailableBreakdowns the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder includeAvailableBreakdowns(java.lang.Boolean includeAvailableBreakdowns) {
            doSetProperty("includeAvailableBreakdowns", includeAvailableBreakdowns);
            return this;
        }
    
        /**
         * Set this parameter to true to return all notes associated with the
         * score. The note element contains the note text as well as the author
         * and timestamp when the note was added.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param includeScoreNotes the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder includeScoreNotes(java.lang.Boolean includeScoreNotes) {
            doSetProperty("includeScoreNotes", includeScoreNotes);
            return this;
        }
    
        /**
         * Set this parameter to true to return all scores for a scorecard. If a
         * value is not specified, this parameter defaults to false and returns
         * only the most recent score value.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param includeScores the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder includeScores(java.lang.Boolean includeScores) {
            doSetProperty("includeScores", includeScores);
            return this;
        }
    
        /**
         * True to set raw value of input fields (default: false).
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param inputDisplayValue the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder inputDisplayValue(java.lang.Boolean inputDisplayValue) {
            doSetProperty("inputDisplayValue", inputDisplayValue);
            return this;
        }
    
        /**
         * Set this parameter to true to return only scorecards for key
         * indicators.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param key the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder key(java.lang.Boolean key) {
            doSetProperty("key", key);
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
        default ServicenowComponentBuilder lazyStartProducer(boolean lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
            return this;
        }
    
        /**
         * Defines both request and response models. This is a multi-value
         * option with prefix: model.
         * 
         * The option is a: &lt;code&gt;java.util.Map&amp;lt;java.lang.String,
         * java.lang.Class&amp;lt;java.lang.Object&amp;gt;&amp;gt;&lt;/code&gt;
         * type.
         * 
         * Group: producer
         * 
         * @param models the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder models(java.util.Map<java.lang.String, java.lang.Class<java.lang.Object>> models) {
            doSetProperty("models", models);
            return this;
        }
    
        
        /**
         * Enter the maximum number of scorecards each query can return. By
         * default this value is 10, and the maximum is 100.
         * 
         * The option is a: &lt;code&gt;java.lang.Integer&lt;/code&gt; type.
         * 
         * Default: 10
         * Group: producer
         * 
         * @param perPage the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder perPage(java.lang.Integer perPage) {
            doSetProperty("perPage", perPage);
            return this;
        }
    
        
        /**
         * The ServiceNow release to target, default to Helsinki See
         * https://docs.servicenow.com.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.servicenow.ServiceNowRelease&lt;/code&gt; type.
         * 
         * Default: HELSINKI
         * Group: producer
         * 
         * @param release the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder release(org.apache.camel.component.servicenow.ServiceNowRelease release) {
            doSetProperty("release", release);
            return this;
        }
    
        /**
         * Defines the request model. This is a multi-value option with prefix:
         * request-model.
         * 
         * The option is a: &lt;code&gt;java.util.Map&amp;lt;java.lang.String,
         * java.lang.Class&amp;lt;java.lang.Object&amp;gt;&amp;gt;&lt;/code&gt;
         * type.
         * 
         * Group: producer
         * 
         * @param requestModels the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder requestModels(java.util.Map<java.lang.String, java.lang.Class<java.lang.Object>> requestModels) {
            doSetProperty("requestModels", requestModels);
            return this;
        }
    
        /**
         * The default resource, can be overridden by header
         * CamelServiceNowResource.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param resource the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder resource(java.lang.String resource) {
            doSetProperty("resource", resource);
            return this;
        }
    
        /**
         * Defines the response model. This is a multi-value option with prefix:
         * response-model.
         * 
         * The option is a: &lt;code&gt;java.util.Map&amp;lt;java.lang.String,
         * java.lang.Class&amp;lt;java.lang.Object&amp;gt;&amp;gt;&lt;/code&gt;
         * type.
         * 
         * Group: producer
         * 
         * @param responseModels the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder responseModels(java.util.Map<java.lang.String, java.lang.Class<java.lang.Object>> responseModels) {
            doSetProperty("responseModels", responseModels);
            return this;
        }
    
        /**
         * Specify the value to use when sorting results. By default, queries
         * sort records by value.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param sortBy the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder sortBy(java.lang.String sortBy) {
            doSetProperty("sortBy", sortBy);
            return this;
        }
    
        /**
         * Specify the sort direction, ascending or descending. By default,
         * queries sort records in descending order. Use sysparm_sortdir=asc to
         * sort in ascending order.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param sortDir the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder sortDir(java.lang.String sortDir) {
            doSetProperty("sortDir", sortDir);
            return this;
        }
    
        /**
         * True to suppress auto generation of system fields (default: false).
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param suppressAutoSysField the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder suppressAutoSysField(java.lang.Boolean suppressAutoSysField) {
            doSetProperty("suppressAutoSysField", suppressAutoSysField);
            return this;
        }
    
        /**
         * Set this value to true to remove the Link header from the response.
         * The Link header allows you to request additional pages of data when
         * the number of records matching your query exceeds the query limit.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param suppressPaginationHeader the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder suppressPaginationHeader(java.lang.Boolean suppressPaginationHeader) {
            doSetProperty("suppressPaginationHeader", suppressPaginationHeader);
            return this;
        }
    
        /**
         * The default table, can be overridden by header CamelServiceNowTable.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param table the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder table(java.lang.String table) {
            doSetProperty("table", table);
            return this;
        }
    
        /**
         * Set this parameter to true to return only scorecards that have a
         * target.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param target the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder target(java.lang.Boolean target) {
            doSetProperty("target", target);
            return this;
        }
    
        /**
         * Gets only those categories whose parent is a catalog.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param topLevelOnly the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder topLevelOnly(java.lang.Boolean topLevelOnly) {
            doSetProperty("topLevelOnly", topLevelOnly);
            return this;
        }
    
        /**
         * The ServiceNow REST API version, default latest.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param apiVersion the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder apiVersion(java.lang.String apiVersion) {
            doSetProperty("apiVersion", apiVersion);
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
        default ServicenowComponentBuilder autowiredEnabled(boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
    
        
        /**
         * The date format used for Json serialization/deserialization.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: yyyy-MM-dd
         * Group: advanced
         * 
         * @param dateFormat the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder dateFormat(java.lang.String dateFormat) {
            doSetProperty("dateFormat", dateFormat);
            return this;
        }
    
        
        /**
         * The date-time format used for Json serialization/deserialization.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: yyyy-MM-dd HH:mm:ss
         * Group: advanced
         * 
         * @param dateTimeFormat the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder dateTimeFormat(java.lang.String dateTimeFormat) {
            doSetProperty("dateTimeFormat", dateTimeFormat);
            return this;
        }
    
        /**
         * To configure http-client.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.cxf.transports.http.configuration.HTTPClientPolicy&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param httpClientPolicy the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder httpClientPolicy(org.apache.cxf.transports.http.configuration.HTTPClientPolicy httpClientPolicy) {
            doSetProperty("httpClientPolicy", httpClientPolicy);
            return this;
        }
    
        /**
         * The ServiceNow instance name.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param instanceName the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder instanceName(java.lang.String instanceName) {
            doSetProperty("instanceName", instanceName);
            return this;
        }
    
        /**
         * Sets Jackson's ObjectMapper to use for request/reply.
         * 
         * The option is a:
         * &lt;code&gt;com.fasterxml.jackson.databind.ObjectMapper&lt;/code&gt;
         * type.
         * 
         * Group: advanced
         * 
         * @param mapper the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder mapper(com.fasterxml.jackson.databind.ObjectMapper mapper) {
            doSetProperty("mapper", mapper);
            return this;
        }
    
        /**
         * To configure proxy authentication.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.cxf.configuration.security.ProxyAuthorizationPolicy&lt;/code&gt; type.
         * 
         * Group: advanced
         * 
         * @param proxyAuthorizationPolicy the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder proxyAuthorizationPolicy(org.apache.cxf.configuration.security.ProxyAuthorizationPolicy proxyAuthorizationPolicy) {
            doSetProperty("proxyAuthorizationPolicy", proxyAuthorizationPolicy);
            return this;
        }
    
        
        /**
         * Set this parameter to true to retrieve the target record when using
         * import set api. The import set result is then replaced by the target
         * record.
         * 
         * The option is a: &lt;code&gt;java.lang.Boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: advanced
         * 
         * @param retrieveTargetRecordOnImport the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder retrieveTargetRecordOnImport(java.lang.Boolean retrieveTargetRecordOnImport) {
            doSetProperty("retrieveTargetRecordOnImport", retrieveTargetRecordOnImport);
            return this;
        }
    
        
        /**
         * The time format used for Json serialization/deserialization.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Default: HH:mm:ss
         * Group: advanced
         * 
         * @param timeFormat the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder timeFormat(java.lang.String timeFormat) {
            doSetProperty("timeFormat", timeFormat);
            return this;
        }
    
        /**
         * The proxy host name.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: proxy
         * 
         * @param proxyHost the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder proxyHost(java.lang.String proxyHost) {
            doSetProperty("proxyHost", proxyHost);
            return this;
        }
    
        /**
         * The proxy port number.
         * 
         * The option is a: &lt;code&gt;java.lang.Integer&lt;/code&gt; type.
         * 
         * Group: proxy
         * 
         * @param proxyPort the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder proxyPort(java.lang.Integer proxyPort) {
            doSetProperty("proxyPort", proxyPort);
            return this;
        }
    
        /**
         * The ServiceNow REST API url.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param apiUrl the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder apiUrl(java.lang.String apiUrl) {
            doSetProperty("apiUrl", apiUrl);
            return this;
        }
    
        /**
         * OAuth2 ClientID.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param oauthClientId the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder oauthClientId(java.lang.String oauthClientId) {
            doSetProperty("oauthClientId", oauthClientId);
            return this;
        }
    
        /**
         * OAuth2 ClientSecret.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param oauthClientSecret the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder oauthClientSecret(java.lang.String oauthClientSecret) {
            doSetProperty("oauthClientSecret", oauthClientSecret);
            return this;
        }
    
        /**
         * OAuth token Url.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param oauthTokenUrl the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder oauthTokenUrl(java.lang.String oauthTokenUrl) {
            doSetProperty("oauthTokenUrl", oauthTokenUrl);
            return this;
        }
    
        /**
         * ServiceNow account password, MUST be provided.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param password the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder password(java.lang.String password) {
            doSetProperty("password", password);
            return this;
        }
    
        /**
         * Password for proxy authentication.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param proxyPassword the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder proxyPassword(java.lang.String proxyPassword) {
            doSetProperty("proxyPassword", proxyPassword);
            return this;
        }
    
        /**
         * Username for proxy authentication.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param proxyUserName the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder proxyUserName(java.lang.String proxyUserName) {
            doSetProperty("proxyUserName", proxyUserName);
            return this;
        }
    
        /**
         * To configure security using SSLContextParameters. See
         * http://camel.apache.org/camel-configuration-utilities.html.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.support.jsse.SSLContextParameters&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param sslContextParameters the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder sslContextParameters(org.apache.camel.support.jsse.SSLContextParameters sslContextParameters) {
            doSetProperty("sslContextParameters", sslContextParameters);
            return this;
        }
    
        
        /**
         * Enable usage of global SSL context parameters.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: security
         * 
         * @param useGlobalSslContextParameters the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder useGlobalSslContextParameters(boolean useGlobalSslContextParameters) {
            doSetProperty("useGlobalSslContextParameters", useGlobalSslContextParameters);
            return this;
        }
    
        /**
         * ServiceNow user account name, MUST be provided.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param userName the value to set
         * @return the dsl builder
         */
        default ServicenowComponentBuilder userName(java.lang.String userName) {
            doSetProperty("userName", userName);
            return this;
        }
    }

    class ServicenowComponentBuilderImpl
            extends AbstractComponentBuilder<ServiceNowComponent>
            implements ServicenowComponentBuilder {
        @Override
        protected ServiceNowComponent buildConcreteComponent() {
            return new ServiceNowComponent();
        }
        private org.apache.camel.component.servicenow.ServiceNowConfiguration getOrCreateConfiguration(ServiceNowComponent component) {
            if (component.getConfiguration() == null) {
                component.setConfiguration(new org.apache.camel.component.servicenow.ServiceNowConfiguration());
            }
            return component.getConfiguration();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "configuration": ((ServiceNowComponent) component).setConfiguration((org.apache.camel.component.servicenow.ServiceNowConfiguration) value); return true;
            case "display": getOrCreateConfiguration((ServiceNowComponent) component).setDisplay((java.lang.String) value); return true;
            case "displayValue": getOrCreateConfiguration((ServiceNowComponent) component).setDisplayValue((java.lang.String) value); return true;
            case "excludeReferenceLink": getOrCreateConfiguration((ServiceNowComponent) component).setExcludeReferenceLink((java.lang.Boolean) value); return true;
            case "favorites": getOrCreateConfiguration((ServiceNowComponent) component).setFavorites((java.lang.Boolean) value); return true;
            case "includeAggregates": getOrCreateConfiguration((ServiceNowComponent) component).setIncludeAggregates((java.lang.Boolean) value); return true;
            case "includeAvailableAggregates": getOrCreateConfiguration((ServiceNowComponent) component).setIncludeAvailableAggregates((java.lang.Boolean) value); return true;
            case "includeAvailableBreakdowns": getOrCreateConfiguration((ServiceNowComponent) component).setIncludeAvailableBreakdowns((java.lang.Boolean) value); return true;
            case "includeScoreNotes": getOrCreateConfiguration((ServiceNowComponent) component).setIncludeScoreNotes((java.lang.Boolean) value); return true;
            case "includeScores": getOrCreateConfiguration((ServiceNowComponent) component).setIncludeScores((java.lang.Boolean) value); return true;
            case "inputDisplayValue": getOrCreateConfiguration((ServiceNowComponent) component).setInputDisplayValue((java.lang.Boolean) value); return true;
            case "key": getOrCreateConfiguration((ServiceNowComponent) component).setKey((java.lang.Boolean) value); return true;
            case "lazyStartProducer": ((ServiceNowComponent) component).setLazyStartProducer((boolean) value); return true;
            case "models": getOrCreateConfiguration((ServiceNowComponent) component).setModels((java.util.Map) value); return true;
            case "perPage": getOrCreateConfiguration((ServiceNowComponent) component).setPerPage((java.lang.Integer) value); return true;
            case "release": getOrCreateConfiguration((ServiceNowComponent) component).setRelease((org.apache.camel.component.servicenow.ServiceNowRelease) value); return true;
            case "requestModels": getOrCreateConfiguration((ServiceNowComponent) component).setRequestModels((java.util.Map) value); return true;
            case "resource": getOrCreateConfiguration((ServiceNowComponent) component).setResource((java.lang.String) value); return true;
            case "responseModels": getOrCreateConfiguration((ServiceNowComponent) component).setResponseModels((java.util.Map) value); return true;
            case "sortBy": getOrCreateConfiguration((ServiceNowComponent) component).setSortBy((java.lang.String) value); return true;
            case "sortDir": getOrCreateConfiguration((ServiceNowComponent) component).setSortDir((java.lang.String) value); return true;
            case "suppressAutoSysField": getOrCreateConfiguration((ServiceNowComponent) component).setSuppressAutoSysField((java.lang.Boolean) value); return true;
            case "suppressPaginationHeader": getOrCreateConfiguration((ServiceNowComponent) component).setSuppressPaginationHeader((java.lang.Boolean) value); return true;
            case "table": getOrCreateConfiguration((ServiceNowComponent) component).setTable((java.lang.String) value); return true;
            case "target": getOrCreateConfiguration((ServiceNowComponent) component).setTarget((java.lang.Boolean) value); return true;
            case "topLevelOnly": getOrCreateConfiguration((ServiceNowComponent) component).setTopLevelOnly((java.lang.Boolean) value); return true;
            case "apiVersion": getOrCreateConfiguration((ServiceNowComponent) component).setApiVersion((java.lang.String) value); return true;
            case "autowiredEnabled": ((ServiceNowComponent) component).setAutowiredEnabled((boolean) value); return true;
            case "dateFormat": getOrCreateConfiguration((ServiceNowComponent) component).setDateFormat((java.lang.String) value); return true;
            case "dateTimeFormat": getOrCreateConfiguration((ServiceNowComponent) component).setDateTimeFormat((java.lang.String) value); return true;
            case "httpClientPolicy": getOrCreateConfiguration((ServiceNowComponent) component).setHttpClientPolicy((org.apache.cxf.transports.http.configuration.HTTPClientPolicy) value); return true;
            case "instanceName": ((ServiceNowComponent) component).setInstanceName((java.lang.String) value); return true;
            case "mapper": getOrCreateConfiguration((ServiceNowComponent) component).setMapper((com.fasterxml.jackson.databind.ObjectMapper) value); return true;
            case "proxyAuthorizationPolicy": getOrCreateConfiguration((ServiceNowComponent) component).setProxyAuthorizationPolicy((org.apache.cxf.configuration.security.ProxyAuthorizationPolicy) value); return true;
            case "retrieveTargetRecordOnImport": getOrCreateConfiguration((ServiceNowComponent) component).setRetrieveTargetRecordOnImport((java.lang.Boolean) value); return true;
            case "timeFormat": getOrCreateConfiguration((ServiceNowComponent) component).setTimeFormat((java.lang.String) value); return true;
            case "proxyHost": getOrCreateConfiguration((ServiceNowComponent) component).setProxyHost((java.lang.String) value); return true;
            case "proxyPort": getOrCreateConfiguration((ServiceNowComponent) component).setProxyPort((java.lang.Integer) value); return true;
            case "apiUrl": getOrCreateConfiguration((ServiceNowComponent) component).setApiUrl((java.lang.String) value); return true;
            case "oauthClientId": getOrCreateConfiguration((ServiceNowComponent) component).setOauthClientId((java.lang.String) value); return true;
            case "oauthClientSecret": getOrCreateConfiguration((ServiceNowComponent) component).setOauthClientSecret((java.lang.String) value); return true;
            case "oauthTokenUrl": getOrCreateConfiguration((ServiceNowComponent) component).setOauthTokenUrl((java.lang.String) value); return true;
            case "password": getOrCreateConfiguration((ServiceNowComponent) component).setPassword((java.lang.String) value); return true;
            case "proxyPassword": getOrCreateConfiguration((ServiceNowComponent) component).setProxyPassword((java.lang.String) value); return true;
            case "proxyUserName": getOrCreateConfiguration((ServiceNowComponent) component).setProxyUserName((java.lang.String) value); return true;
            case "sslContextParameters": getOrCreateConfiguration((ServiceNowComponent) component).setSslContextParameters((org.apache.camel.support.jsse.SSLContextParameters) value); return true;
            case "useGlobalSslContextParameters": ((ServiceNowComponent) component).setUseGlobalSslContextParameters((boolean) value); return true;
            case "userName": getOrCreateConfiguration((ServiceNowComponent) component).setUserName((java.lang.String) value); return true;
            default: return false;
            }
        }
    }
}
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
import org.apache.camel.component.azure.storage.blob.BlobComponent;

/**
 * Store and retrieve blobs from Azure Storage Blob Service.
 * 
 * Generated by camel build tools - do NOT edit this file!
 */
@Generated("org.apache.camel.maven.packaging.ComponentDslMojo")
public interface AzureStorageBlobComponentBuilderFactory {

    /**
     * Azure Storage Blob Service (camel-azure-storage-blob)
     * Store and retrieve blobs from Azure Storage Blob Service.
     * 
     * Category: cloud,file
     * Since: 3.3
     * Maven coordinates: org.apache.camel:camel-azure-storage-blob
     * 
     * @return the dsl builder
     */
    static AzureStorageBlobComponentBuilder azureStorageBlob() {
        return new AzureStorageBlobComponentBuilderImpl();
    }

    /**
     * Builder for the Azure Storage Blob Service component.
     */
    interface AzureStorageBlobComponentBuilder extends ComponentBuilder<BlobComponent> {
    
        /**
         * The blob name, to consume specific blob from a container. However, on
         * producer it is only required for the operations on the blob level.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param blobName the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder blobName(java.lang.String blobName) {
            doSetProperty("blobName", blobName);
            return this;
        }
    
        /**
         * Set the blob offset for the upload or download operations, default is
         * 0.
         * 
         * The option is a: &lt;code&gt;long&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param blobOffset the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder blobOffset(long blobOffset) {
            doSetProperty("blobOffset", blobOffset);
            return this;
        }
    
        
        /**
         * The blob type in order to initiate the appropriate settings for each
         * blob type.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.azure.storage.blob.BlobType&lt;/code&gt; type.
         * 
         * Default: blockblob
         * Group: common
         * 
         * @param blobType the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder blobType(org.apache.camel.component.azure.storage.blob.BlobType blobType) {
            doSetProperty("blobType", blobType);
            return this;
        }
    
        
        /**
         * Close the stream after read or keep it open, default is true.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: common
         * 
         * @param closeStreamAfterRead the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder closeStreamAfterRead(boolean closeStreamAfterRead) {
            doSetProperty("closeStreamAfterRead", closeStreamAfterRead);
            return this;
        }
    
        /**
         * The component configurations.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.azure.storage.blob.BlobConfiguration&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param configuration the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder configuration(org.apache.camel.component.azure.storage.blob.BlobConfiguration configuration) {
            doSetProperty("configuration", configuration);
            return this;
        }
    
        /**
         * StorageSharedKeyCredential can be injected to create the azure
         * client, this holds the important authentication information.
         * 
         * The option is a:
         * &lt;code&gt;com.azure.storage.common.StorageSharedKeyCredential&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param credentials the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder credentials(com.azure.storage.common.StorageSharedKeyCredential credentials) {
            doSetProperty("credentials", credentials);
            return this;
        }
    
        
        /**
         * Determines the credential strategy to adopt.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.azure.storage.blob.CredentialType&lt;/code&gt; type.
         * 
         * Default: AZURE_IDENTITY
         * Group: common
         * 
         * @param credentialType the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder credentialType(org.apache.camel.component.azure.storage.blob.CredentialType credentialType) {
            doSetProperty("credentialType", credentialType);
            return this;
        }
    
        /**
         * How many bytes to include in the range. Must be greater than or equal
         * to 0 if specified.
         * 
         * The option is a: &lt;code&gt;java.lang.Long&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param dataCount the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder dataCount(java.lang.Long dataCount) {
            doSetProperty("dataCount", dataCount);
            return this;
        }
    
        /**
         * The file directory where the downloaded blobs will be saved to, this
         * can be used in both, producer and consumer.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param fileDir the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder fileDir(java.lang.String fileDir) {
            doSetProperty("fileDir", fileDir);
            return this;
        }
    
        
        /**
         * Sets whether a lease should be acquired when accessing the blob. When
         * set to true, the component will acquire a lease before performing
         * blob operations that require exclusive access.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: false
         * Group: common
         * 
         * @param leaseBlob the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder leaseBlob(boolean leaseBlob) {
            doSetProperty("leaseBlob", leaseBlob);
            return this;
        }
    
        
        /**
         * Sets the lease duration in seconds. Use -1 for infinite or a value
         * between 15 and 60 for fixed leases.
         * 
         * The option is a: &lt;code&gt;java.lang.Integer&lt;/code&gt; type.
         * 
         * Default: 60
         * Group: common
         * 
         * @param leaseDurationInSeconds the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder leaseDurationInSeconds(java.lang.Integer leaseDurationInSeconds) {
            doSetProperty("leaseDurationInSeconds", leaseDurationInSeconds);
            return this;
        }
    
        /**
         * Specifies the maximum number of blobs to return, including all
         * BlobPrefix elements. If the request does not specify
         * maxResultsPerPage or specifies a value greater than 5,000, the server
         * will return up to 5,000 items.
         * 
         * The option is a: &lt;code&gt;java.lang.Integer&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param maxResultsPerPage the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder maxResultsPerPage(java.lang.Integer maxResultsPerPage) {
            doSetProperty("maxResultsPerPage", maxResultsPerPage);
            return this;
        }
    
        /**
         * Specifies the maximum number of additional HTTP Get requests that
         * will be made while reading the data from a response body.
         * 
         * The option is a: &lt;code&gt;int&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param maxRetryRequests the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder maxRetryRequests(int maxRetryRequests) {
            doSetProperty("maxRetryRequests", maxRetryRequests);
            return this;
        }
    
        /**
         * Filters the results to return only blobs whose names begin with the
         * specified prefix. May be null to return all blobs.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param prefix the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder prefix(java.lang.String prefix) {
            doSetProperty("prefix", prefix);
            return this;
        }
    
        /**
         * Filters the results to return only blobs whose names match the
         * specified regular expression. May be null to return all if both
         * prefix and regex are set, regex takes the priority and prefix is
         * ignored.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param regex the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder regex(java.lang.String regex) {
            doSetProperty("regex", regex);
            return this;
        }
    
        /**
         * In case of usage of Shared Access Signature we'll need to set a SAS
         * Token.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param sasToken the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder sasToken(java.lang.String sasToken) {
            doSetProperty("sasToken", sasToken);
            return this;
        }
    
        /**
         * Client to a storage account. This client does not hold any state
         * about a particular storage account but is instead a convenient way of
         * sending off appropriate requests to the resource on the service. It
         * may also be used to construct URLs to blobs and containers. This
         * client contains operations on a service account. Operations on a
         * container are available on BlobContainerClient through
         * BlobServiceClient#getBlobContainerClient(String), and operations on a
         * blob are available on BlobClient through
         * BlobContainerClient#getBlobClient(String).
         * 
         * The option is a:
         * &lt;code&gt;com.azure.storage.blob.BlobServiceClient&lt;/code&gt;
         * type.
         * 
         * Group: common
         * 
         * @param serviceClient the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder serviceClient(com.azure.storage.blob.BlobServiceClient serviceClient) {
            doSetProperty("serviceClient", serviceClient);
            return this;
        }
    
        /**
         * An optional timeout value beyond which a RuntimeException will be
         * raised.
         * 
         * The option is a: &lt;code&gt;java.time.Duration&lt;/code&gt; type.
         * 
         * Group: common
         * 
         * @param timeout the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder timeout(java.time.Duration timeout) {
            doSetProperty("timeout", timeout);
            return this;
        }
    
        
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
        default AzureStorageBlobComponentBuilder bridgeErrorHandler(boolean bridgeErrorHandler) {
            doSetProperty("bridgeErrorHandler", bridgeErrorHandler);
            return this;
        }
    
        
        /**
         * A user-controlled value that you can use to track requests. The value
         * of the sequence number must be between 0 and 263 - 1.The default
         * value is 0.
         * 
         * The option is a: &lt;code&gt;java.lang.Long&lt;/code&gt; type.
         * 
         * Default: 0
         * Group: producer
         * 
         * @param blobSequenceNumber the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder blobSequenceNumber(java.lang.Long blobSequenceNumber) {
            doSetProperty("blobSequenceNumber", blobSequenceNumber);
            return this;
        }
    
        
        /**
         * Specifies which type of blocks to return.
         * 
         * The option is a:
         * &lt;code&gt;com.azure.storage.blob.models.BlockListType&lt;/code&gt;
         * type.
         * 
         * Default: COMMITTED
         * Group: producer
         * 
         * @param blockListType the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder blockListType(com.azure.storage.blob.models.BlockListType blockListType) {
            doSetProperty("blockListType", blockListType);
            return this;
        }
    
        /**
         * When using getChangeFeed producer operation, this gives additional
         * context that is passed through the Http pipeline during the service
         * call.
         * 
         * The option is a: &lt;code&gt;com.azure.core.util.Context&lt;/code&gt;
         * type.
         * 
         * Group: producer
         * 
         * @param changeFeedContext the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder changeFeedContext(com.azure.core.util.Context changeFeedContext) {
            doSetProperty("changeFeedContext", changeFeedContext);
            return this;
        }
    
        /**
         * When using getChangeFeed producer operation, this filters the results
         * to return events approximately before the end time. Note: A few
         * events belonging to the next hour can also be returned. A few events
         * belonging to this hour can be missing; to ensure all events from the
         * hour are returned, round the end time up by an hour.
         * 
         * The option is a: &lt;code&gt;java.time.OffsetDateTime&lt;/code&gt;
         * type.
         * 
         * Group: producer
         * 
         * @param changeFeedEndTime the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder changeFeedEndTime(java.time.OffsetDateTime changeFeedEndTime) {
            doSetProperty("changeFeedEndTime", changeFeedEndTime);
            return this;
        }
    
        /**
         * When using getChangeFeed producer operation, this filters the results
         * to return events approximately after the start time. Note: A few
         * events belonging to the previous hour can also be returned. A few
         * events belonging to this hour can be missing; to ensure all events
         * from the hour are returned, round the start time down by an hour.
         * 
         * The option is a: &lt;code&gt;java.time.OffsetDateTime&lt;/code&gt;
         * type.
         * 
         * Group: producer
         * 
         * @param changeFeedStartTime the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder changeFeedStartTime(java.time.OffsetDateTime changeFeedStartTime) {
            doSetProperty("changeFeedStartTime", changeFeedStartTime);
            return this;
        }
    
        
        /**
         * Close the stream after write or keep it open, default is true.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param closeStreamAfterWrite the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder closeStreamAfterWrite(boolean closeStreamAfterWrite) {
            doSetProperty("closeStreamAfterWrite", closeStreamAfterWrite);
            return this;
        }
    
        
        /**
         * When is set to true, the staged blocks will not be committed
         * directly.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param commitBlockListLater the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder commitBlockListLater(boolean commitBlockListLater) {
            doSetProperty("commitBlockListLater", commitBlockListLater);
            return this;
        }
    
        
        /**
         * When is set to true, the append blocks will be created when
         * committing append blocks.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param createAppendBlob the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder createAppendBlob(boolean createAppendBlob) {
            doSetProperty("createAppendBlob", createAppendBlob);
            return this;
        }
    
        
        /**
         * When is set to true, the page blob will be created when uploading
         * page blob.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: producer
         * 
         * @param createPageBlob the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder createPageBlob(boolean createPageBlob) {
            doSetProperty("createPageBlob", createPageBlob);
            return this;
        }
    
        /**
         * Override the default expiration (millis) of URL download link.
         * 
         * The option is a: &lt;code&gt;java.lang.Long&lt;/code&gt; type.
         * 
         * Group: producer
         * 
         * @param downloadLinkExpiration the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder downloadLinkExpiration(java.lang.Long downloadLinkExpiration) {
            doSetProperty("downloadLinkExpiration", downloadLinkExpiration);
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
        default AzureStorageBlobComponentBuilder lazyStartProducer(boolean lazyStartProducer) {
            doSetProperty("lazyStartProducer", lazyStartProducer);
            return this;
        }
    
        
        /**
         * The blob operation that can be used with this component on the
         * producer.
         * 
         * The option is a:
         * &lt;code&gt;org.apache.camel.component.azure.storage.blob.BlobOperationsDefinition&lt;/code&gt; type.
         * 
         * Default: listBlobContainers
         * Group: producer
         * 
         * @param operation the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder operation(org.apache.camel.component.azure.storage.blob.BlobOperationsDefinition operation) {
            doSetProperty("operation", operation);
            return this;
        }
    
        
        /**
         * Specifies the maximum size for the page blob, up to 8 TB. The page
         * blob size must be aligned to a 512-byte boundary.
         * 
         * The option is a: &lt;code&gt;java.lang.Long&lt;/code&gt; type.
         * 
         * Default: 512
         * Group: producer
         * 
         * @param pageBlobSize the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder pageBlobSize(java.lang.Long pageBlobSize) {
            doSetProperty("pageBlobSize", pageBlobSize);
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
        default AzureStorageBlobComponentBuilder autowiredEnabled(boolean autowiredEnabled) {
            doSetProperty("autowiredEnabled", autowiredEnabled);
            return this;
        }
    
        
        /**
         * Used for enabling or disabling all consumer based health checks from
         * this component.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: health
         * 
         * @param healthCheckConsumerEnabled the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder healthCheckConsumerEnabled(boolean healthCheckConsumerEnabled) {
            doSetProperty("healthCheckConsumerEnabled", healthCheckConsumerEnabled);
            return this;
        }
    
        
        /**
         * Used for enabling or disabling all producer based health checks from
         * this component. Notice: Camel has by default disabled all producer
         * based health-checks. You can turn on producer checks globally by
         * setting camel.health.producersEnabled=true.
         * 
         * The option is a: &lt;code&gt;boolean&lt;/code&gt; type.
         * 
         * Default: true
         * Group: health
         * 
         * @param healthCheckProducerEnabled the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder healthCheckProducerEnabled(boolean healthCheckProducerEnabled) {
            doSetProperty("healthCheckProducerEnabled", healthCheckProducerEnabled);
            return this;
        }
    
        /**
         * Access key for the associated azure account name to be used for
         * authentication with azure blob services.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param accessKey the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder accessKey(java.lang.String accessKey) {
            doSetProperty("accessKey", accessKey);
            return this;
        }
    
        /**
         * Source Blob Access Key: for copyblob operation, sadly, we need to
         * have an accessKey for the source blob we want to copy Passing an
         * accessKey as header, it's unsafe so we could set as key.
         * 
         * The option is a: &lt;code&gt;java.lang.String&lt;/code&gt; type.
         * 
         * Group: security
         * 
         * @param sourceBlobAccessKey the value to set
         * @return the dsl builder
         */
        default AzureStorageBlobComponentBuilder sourceBlobAccessKey(java.lang.String sourceBlobAccessKey) {
            doSetProperty("sourceBlobAccessKey", sourceBlobAccessKey);
            return this;
        }
    }

    class AzureStorageBlobComponentBuilderImpl
            extends AbstractComponentBuilder<BlobComponent>
            implements AzureStorageBlobComponentBuilder {
        @Override
        protected BlobComponent buildConcreteComponent() {
            return new BlobComponent();
        }
        private org.apache.camel.component.azure.storage.blob.BlobConfiguration getOrCreateConfiguration(BlobComponent component) {
            if (component.getConfiguration() == null) {
                component.setConfiguration(new org.apache.camel.component.azure.storage.blob.BlobConfiguration());
            }
            return component.getConfiguration();
        }
        @Override
        protected boolean setPropertyOnComponent(
                Component component,
                String name,
                Object value) {
            switch (name) {
            case "blobName": getOrCreateConfiguration((BlobComponent) component).setBlobName((java.lang.String) value); return true;
            case "blobOffset": getOrCreateConfiguration((BlobComponent) component).setBlobOffset((long) value); return true;
            case "blobType": getOrCreateConfiguration((BlobComponent) component).setBlobType((org.apache.camel.component.azure.storage.blob.BlobType) value); return true;
            case "closeStreamAfterRead": getOrCreateConfiguration((BlobComponent) component).setCloseStreamAfterRead((boolean) value); return true;
            case "configuration": ((BlobComponent) component).setConfiguration((org.apache.camel.component.azure.storage.blob.BlobConfiguration) value); return true;
            case "credentials": getOrCreateConfiguration((BlobComponent) component).setCredentials((com.azure.storage.common.StorageSharedKeyCredential) value); return true;
            case "credentialType": getOrCreateConfiguration((BlobComponent) component).setCredentialType((org.apache.camel.component.azure.storage.blob.CredentialType) value); return true;
            case "dataCount": getOrCreateConfiguration((BlobComponent) component).setDataCount((java.lang.Long) value); return true;
            case "fileDir": getOrCreateConfiguration((BlobComponent) component).setFileDir((java.lang.String) value); return true;
            case "leaseBlob": getOrCreateConfiguration((BlobComponent) component).setLeaseBlob((boolean) value); return true;
            case "leaseDurationInSeconds": getOrCreateConfiguration((BlobComponent) component).setLeaseDurationInSeconds((java.lang.Integer) value); return true;
            case "maxResultsPerPage": getOrCreateConfiguration((BlobComponent) component).setMaxResultsPerPage((java.lang.Integer) value); return true;
            case "maxRetryRequests": getOrCreateConfiguration((BlobComponent) component).setMaxRetryRequests((int) value); return true;
            case "prefix": getOrCreateConfiguration((BlobComponent) component).setPrefix((java.lang.String) value); return true;
            case "regex": getOrCreateConfiguration((BlobComponent) component).setRegex((java.lang.String) value); return true;
            case "sasToken": getOrCreateConfiguration((BlobComponent) component).setSasToken((java.lang.String) value); return true;
            case "serviceClient": getOrCreateConfiguration((BlobComponent) component).setServiceClient((com.azure.storage.blob.BlobServiceClient) value); return true;
            case "timeout": getOrCreateConfiguration((BlobComponent) component).setTimeout((java.time.Duration) value); return true;
            case "bridgeErrorHandler": ((BlobComponent) component).setBridgeErrorHandler((boolean) value); return true;
            case "blobSequenceNumber": getOrCreateConfiguration((BlobComponent) component).setBlobSequenceNumber((java.lang.Long) value); return true;
            case "blockListType": getOrCreateConfiguration((BlobComponent) component).setBlockListType((com.azure.storage.blob.models.BlockListType) value); return true;
            case "changeFeedContext": getOrCreateConfiguration((BlobComponent) component).setChangeFeedContext((com.azure.core.util.Context) value); return true;
            case "changeFeedEndTime": getOrCreateConfiguration((BlobComponent) component).setChangeFeedEndTime((java.time.OffsetDateTime) value); return true;
            case "changeFeedStartTime": getOrCreateConfiguration((BlobComponent) component).setChangeFeedStartTime((java.time.OffsetDateTime) value); return true;
            case "closeStreamAfterWrite": getOrCreateConfiguration((BlobComponent) component).setCloseStreamAfterWrite((boolean) value); return true;
            case "commitBlockListLater": getOrCreateConfiguration((BlobComponent) component).setCommitBlockListLater((boolean) value); return true;
            case "createAppendBlob": getOrCreateConfiguration((BlobComponent) component).setCreateAppendBlob((boolean) value); return true;
            case "createPageBlob": getOrCreateConfiguration((BlobComponent) component).setCreatePageBlob((boolean) value); return true;
            case "downloadLinkExpiration": getOrCreateConfiguration((BlobComponent) component).setDownloadLinkExpiration((java.lang.Long) value); return true;
            case "lazyStartProducer": ((BlobComponent) component).setLazyStartProducer((boolean) value); return true;
            case "operation": getOrCreateConfiguration((BlobComponent) component).setOperation((org.apache.camel.component.azure.storage.blob.BlobOperationsDefinition) value); return true;
            case "pageBlobSize": getOrCreateConfiguration((BlobComponent) component).setPageBlobSize((java.lang.Long) value); return true;
            case "autowiredEnabled": ((BlobComponent) component).setAutowiredEnabled((boolean) value); return true;
            case "healthCheckConsumerEnabled": ((BlobComponent) component).setHealthCheckConsumerEnabled((boolean) value); return true;
            case "healthCheckProducerEnabled": ((BlobComponent) component).setHealthCheckProducerEnabled((boolean) value); return true;
            case "accessKey": getOrCreateConfiguration((BlobComponent) component).setAccessKey((java.lang.String) value); return true;
            case "sourceBlobAccessKey": getOrCreateConfiguration((BlobComponent) component).setSourceBlobAccessKey((java.lang.String) value); return true;
            default: return false;
            }
        }
    }
}
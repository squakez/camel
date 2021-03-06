/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.component.azure.storage.datalake;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.ExtendedPropertyConfigurerGetter;
import org.apache.camel.spi.PropertyConfigurerGetter;
import org.apache.camel.spi.ConfigurerStrategy;
import org.apache.camel.spi.GeneratedPropertyConfigurer;
import org.apache.camel.util.CaseInsensitiveMap;
import org.apache.camel.support.component.PropertyConfigurerSupport;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@SuppressWarnings("unchecked")
public class DataLakeEndpointConfigurer extends PropertyConfigurerSupport implements GeneratedPropertyConfigurer, PropertyConfigurerGetter {

    @Override
    public boolean configure(CamelContext camelContext, Object obj, String name, Object value, boolean ignoreCase) {
        DataLakeEndpoint target = (DataLakeEndpoint) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "accountkey":
        case "accountKey": target.getConfiguration().setAccountKey(property(camelContext, java.lang.String.class, value)); return true;
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": target.setBridgeErrorHandler(property(camelContext, boolean.class, value)); return true;
        case "clientid":
        case "clientId": target.getConfiguration().setClientId(property(camelContext, java.lang.String.class, value)); return true;
        case "clientsecret":
        case "clientSecret": target.getConfiguration().setClientSecret(property(camelContext, java.lang.String.class, value)); return true;
        case "clientsecretcredential":
        case "clientSecretCredential": target.getConfiguration().setClientSecretCredential(property(camelContext, com.azure.identity.ClientSecretCredential.class, value)); return true;
        case "close": target.getConfiguration().setClose(property(camelContext, java.lang.Boolean.class, value)); return true;
        case "closestreamafterread":
        case "closeStreamAfterRead": target.getConfiguration().setCloseStreamAfterRead(property(camelContext, java.lang.Boolean.class, value)); return true;
        case "datacount":
        case "dataCount": target.getConfiguration().setDataCount(property(camelContext, java.lang.Long.class, value)); return true;
        case "datalakeserviceclient":
        case "dataLakeServiceClient": target.setDataLakeServiceClient(property(camelContext, com.azure.storage.file.datalake.DataLakeServiceClient.class, value)); return true;
        case "directoryname":
        case "directoryName": target.getConfiguration().setDirectoryName(property(camelContext, java.lang.String.class, value)); return true;
        case "downloadlinkexpiration":
        case "downloadLinkExpiration": target.getConfiguration().setDownloadLinkExpiration(property(camelContext, java.lang.Long.class, value)); return true;
        case "exceptionhandler":
        case "exceptionHandler": target.setExceptionHandler(property(camelContext, org.apache.camel.spi.ExceptionHandler.class, value)); return true;
        case "exchangepattern":
        case "exchangePattern": target.setExchangePattern(property(camelContext, org.apache.camel.ExchangePattern.class, value)); return true;
        case "expression": target.getConfiguration().setExpression(property(camelContext, java.lang.String.class, value)); return true;
        case "filedir":
        case "fileDir": target.getConfiguration().setFileDir(property(camelContext, java.lang.String.class, value)); return true;
        case "filename":
        case "fileName": target.getConfiguration().setFileName(property(camelContext, java.lang.String.class, value)); return true;
        case "fileoffset":
        case "fileOffset": target.getConfiguration().setFileOffset(property(camelContext, java.lang.Long.class, value)); return true;
        case "lazystartproducer":
        case "lazyStartProducer": target.setLazyStartProducer(property(camelContext, boolean.class, value)); return true;
        case "maxresults":
        case "maxResults": target.getConfiguration().setMaxResults(property(camelContext, java.lang.Integer.class, value)); return true;
        case "maxretryrequests":
        case "maxRetryRequests": target.getConfiguration().setMaxRetryRequests(property(camelContext, int.class, value)); return true;
        case "openoptions":
        case "openOptions": target.getConfiguration().setOpenOptions(property(camelContext, java.util.Set.class, value)); return true;
        case "operation": target.getConfiguration().setOperation(property(camelContext, org.apache.camel.component.azure.storage.datalake.DataLakeOperationsDefinition.class, value)); return true;
        case "path": target.getConfiguration().setPath(property(camelContext, java.lang.String.class, value)); return true;
        case "permission": target.getConfiguration().setPermission(property(camelContext, java.lang.String.class, value)); return true;
        case "position": target.getConfiguration().setPosition(property(camelContext, java.lang.Long.class, value)); return true;
        case "recursive": target.getConfiguration().setRecursive(property(camelContext, java.lang.Boolean.class, value)); return true;
        case "regex": target.getConfiguration().setRegex(property(camelContext, java.lang.String.class, value)); return true;
        case "retainuncommiteddata":
        case "retainUncommitedData": target.getConfiguration().setRetainUncommitedData(property(camelContext, java.lang.Boolean.class, value)); return true;
        case "serviceclient":
        case "serviceClient": target.getConfiguration().setServiceClient(property(camelContext, com.azure.storage.file.datalake.DataLakeServiceClient.class, value)); return true;
        case "sharedkeycredential":
        case "sharedKeyCredential": target.getConfiguration().setSharedKeyCredential(property(camelContext, com.azure.storage.common.StorageSharedKeyCredential.class, value)); return true;
        case "tenantid":
        case "tenantId": target.getConfiguration().setTenantId(property(camelContext, java.lang.String.class, value)); return true;
        case "timeout": target.getConfiguration().setTimeout(property(camelContext, java.time.Duration.class, value)); return true;
        case "umask": target.getConfiguration().setUmask(property(camelContext, java.lang.String.class, value)); return true;
        case "userprincipalnamereturned":
        case "userPrincipalNameReturned": target.getConfiguration().setUserPrincipalNameReturned(property(camelContext, java.lang.Boolean.class, value)); return true;
        default: return false;
        }
    }

    @Override
    public String[] getAutowiredNames() {
        return new String[]{"serviceClient"};
    }

    @Override
    public Class<?> getOptionType(String name, boolean ignoreCase) {
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "accountkey":
        case "accountKey": return java.lang.String.class;
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": return boolean.class;
        case "clientid":
        case "clientId": return java.lang.String.class;
        case "clientsecret":
        case "clientSecret": return java.lang.String.class;
        case "clientsecretcredential":
        case "clientSecretCredential": return com.azure.identity.ClientSecretCredential.class;
        case "close": return java.lang.Boolean.class;
        case "closestreamafterread":
        case "closeStreamAfterRead": return java.lang.Boolean.class;
        case "datacount":
        case "dataCount": return java.lang.Long.class;
        case "datalakeserviceclient":
        case "dataLakeServiceClient": return com.azure.storage.file.datalake.DataLakeServiceClient.class;
        case "directoryname":
        case "directoryName": return java.lang.String.class;
        case "downloadlinkexpiration":
        case "downloadLinkExpiration": return java.lang.Long.class;
        case "exceptionhandler":
        case "exceptionHandler": return org.apache.camel.spi.ExceptionHandler.class;
        case "exchangepattern":
        case "exchangePattern": return org.apache.camel.ExchangePattern.class;
        case "expression": return java.lang.String.class;
        case "filedir":
        case "fileDir": return java.lang.String.class;
        case "filename":
        case "fileName": return java.lang.String.class;
        case "fileoffset":
        case "fileOffset": return java.lang.Long.class;
        case "lazystartproducer":
        case "lazyStartProducer": return boolean.class;
        case "maxresults":
        case "maxResults": return java.lang.Integer.class;
        case "maxretryrequests":
        case "maxRetryRequests": return int.class;
        case "openoptions":
        case "openOptions": return java.util.Set.class;
        case "operation": return org.apache.camel.component.azure.storage.datalake.DataLakeOperationsDefinition.class;
        case "path": return java.lang.String.class;
        case "permission": return java.lang.String.class;
        case "position": return java.lang.Long.class;
        case "recursive": return java.lang.Boolean.class;
        case "regex": return java.lang.String.class;
        case "retainuncommiteddata":
        case "retainUncommitedData": return java.lang.Boolean.class;
        case "serviceclient":
        case "serviceClient": return com.azure.storage.file.datalake.DataLakeServiceClient.class;
        case "sharedkeycredential":
        case "sharedKeyCredential": return com.azure.storage.common.StorageSharedKeyCredential.class;
        case "tenantid":
        case "tenantId": return java.lang.String.class;
        case "timeout": return java.time.Duration.class;
        case "umask": return java.lang.String.class;
        case "userprincipalnamereturned":
        case "userPrincipalNameReturned": return java.lang.Boolean.class;
        default: return null;
        }
    }

    @Override
    public Object getOptionValue(Object obj, String name, boolean ignoreCase) {
        DataLakeEndpoint target = (DataLakeEndpoint) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "accountkey":
        case "accountKey": return target.getConfiguration().getAccountKey();
        case "bridgeerrorhandler":
        case "bridgeErrorHandler": return target.isBridgeErrorHandler();
        case "clientid":
        case "clientId": return target.getConfiguration().getClientId();
        case "clientsecret":
        case "clientSecret": return target.getConfiguration().getClientSecret();
        case "clientsecretcredential":
        case "clientSecretCredential": return target.getConfiguration().getClientSecretCredential();
        case "close": return target.getConfiguration().getClose();
        case "closestreamafterread":
        case "closeStreamAfterRead": return target.getConfiguration().getCloseStreamAfterRead();
        case "datacount":
        case "dataCount": return target.getConfiguration().getDataCount();
        case "datalakeserviceclient":
        case "dataLakeServiceClient": return target.getDataLakeServiceClient();
        case "directoryname":
        case "directoryName": return target.getConfiguration().getDirectoryName();
        case "downloadlinkexpiration":
        case "downloadLinkExpiration": return target.getConfiguration().getDownloadLinkExpiration();
        case "exceptionhandler":
        case "exceptionHandler": return target.getExceptionHandler();
        case "exchangepattern":
        case "exchangePattern": return target.getExchangePattern();
        case "expression": return target.getConfiguration().getExpression();
        case "filedir":
        case "fileDir": return target.getConfiguration().getFileDir();
        case "filename":
        case "fileName": return target.getConfiguration().getFileName();
        case "fileoffset":
        case "fileOffset": return target.getConfiguration().getFileOffset();
        case "lazystartproducer":
        case "lazyStartProducer": return target.isLazyStartProducer();
        case "maxresults":
        case "maxResults": return target.getConfiguration().getMaxResults();
        case "maxretryrequests":
        case "maxRetryRequests": return target.getConfiguration().getMaxRetryRequests();
        case "openoptions":
        case "openOptions": return target.getConfiguration().getOpenOptions();
        case "operation": return target.getConfiguration().getOperation();
        case "path": return target.getConfiguration().getPath();
        case "permission": return target.getConfiguration().getPermission();
        case "position": return target.getConfiguration().getPosition();
        case "recursive": return target.getConfiguration().getRecursive();
        case "regex": return target.getConfiguration().getRegex();
        case "retainuncommiteddata":
        case "retainUncommitedData": return target.getConfiguration().getRetainUncommitedData();
        case "serviceclient":
        case "serviceClient": return target.getConfiguration().getServiceClient();
        case "sharedkeycredential":
        case "sharedKeyCredential": return target.getConfiguration().getSharedKeyCredential();
        case "tenantid":
        case "tenantId": return target.getConfiguration().getTenantId();
        case "timeout": return target.getConfiguration().getTimeout();
        case "umask": return target.getConfiguration().getUmask();
        case "userprincipalnamereturned":
        case "userPrincipalNameReturned": return target.getConfiguration().getUserPrincipalNameReturned();
        default: return null;
        }
    }

    @Override
    public Object getCollectionValueType(Object target, String name, boolean ignoreCase) {
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "openoptions":
        case "openOptions": return java.nio.file.OpenOption.class;
        default: return null;
        }
    }
}


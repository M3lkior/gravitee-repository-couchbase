/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.repository.couchbase.common;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

/**
 * @author Ludovic DUSSART (ludovic.dussart.pro at gmail.com)
 */
public class CouchbaseFactory implements FactoryBean<Bucket> {

    private final Logger logger = LoggerFactory.getLogger(CouchbaseFactory.class);

    @Autowired
    private Environment environment;

    private final String propertyPrefix;

    public CouchbaseFactory(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix + ".couchbase.";
    }
    
    /**
     * @see <a href="http://docs.couchbase.com/developer/java-2.1/env-config.html">env-config</a>
     * @return
     */
    public DefaultCouchbaseEnvironment.Builder builder() {
    	DefaultCouchbaseEnvironment.Builder builder = DefaultCouchbaseEnvironment.builder();

    	//Bootstrapping options
        Boolean sslEnabled = readPropertyValue(propertyPrefix + "sslEnabled", Boolean.class);
        String sslKeystoreFile = readPropertyValue(propertyPrefix + "sslKeystoreFile", String.class);
        String sslKeystorePassword = readPropertyValue(propertyPrefix + "sslKeystorePassword", String.class);
        Boolean queryEnabled = readPropertyValue(propertyPrefix + "queryEnabled", Boolean.class);
        Integer queryPort = readPropertyValue(propertyPrefix + "queryPort", Integer.class);
        Boolean bootstrapHttpEnabled = readPropertyValue(propertyPrefix + "bootstrapHttpEnabled", Boolean.class);
        Integer bootstrapHttpDirectPort = readPropertyValue(propertyPrefix + "bootstrapHttpDirectPort", Integer.class);
        Integer bootstrapHttpSslPort = readPropertyValue(propertyPrefix + "bootstrapHttpSslPort", Integer.class);
        Boolean bootstrapCarrierEnabled = readPropertyValue(propertyPrefix + "bootstrapCarrierEnabled", Boolean.class);
        Integer bootstrapCarrierDirectPort = readPropertyValue(propertyPrefix + "bootstrapCarrierDirectPort", Integer.class);
        Integer bootstrapCarrierSslPort = readPropertyValue(propertyPrefix + "bootstrapCarrierSslPort", Integer.class);
        Boolean dnsSrvEnabled = readPropertyValue(propertyPrefix + "dnsSrvEnabled", Boolean.class);
        //Timeout options
        Long kvTimeout = readPropertyValue(propertyPrefix + "kvTimeout", Long.class);
        Long viewTimeout = readPropertyValue(propertyPrefix + "viewTimeout", Long.class);
        Long queryTimeout = readPropertyValue(propertyPrefix + "queryTimeout", Long.class);
        Long connectTimeout = readPropertyValue(propertyPrefix + "connectTimeout", Long.class);
        Long disconnectTimeout = readPropertyValue(propertyPrefix + "disconnectTimeout", Long.class);
        Long managementTimeout = readPropertyValue(propertyPrefix + "managementTimeout", Long.class);
        //Reliability options
        //type(e/f/l) - default exponential, upper/delay(fixed),lower,growBy 
        String reconnectDelay = readPropertyValue(propertyPrefix + "reconnectDelay", String.class);
        TimeUnit reconnectDelayTimeUnit = readPropertyValue(propertyPrefix + "reconnectDelayTimeUnit", TimeUnit.class, TimeUnit.MILLISECONDS);
        //type(e/f/l) - default exponential, upper/delay(fixed),lower,growBy 
        String retryDelay = readPropertyValue(propertyPrefix + "retryDelay", String.class);
        TimeUnit retryDelayTimeUnit = readPropertyValue(propertyPrefix + "retryDelayTimeUnit", TimeUnit.class, TimeUnit.MILLISECONDS);

        //TODO test beetween 3 types
        String retryStrategy = readPropertyValue(propertyPrefix + "retryStrategy", String.class);
        Long maxRequestLifetime = readPropertyValue(propertyPrefix + "maxRequestLifetime", Long.class);
        Long keepAliveInterval = readPropertyValue(propertyPrefix + "keepAliveInterval", Long.class);
        //Performance options
        //type(e/f/l) - default exponential, upper/delay(fixed),lower,growBy 
        String observeIntervalDelay = readPropertyValue(propertyPrefix + "observeIntervalDelay", String.class);
        TimeUnit observeIntervalDelayTimeUnit = readPropertyValue(propertyPrefix + "observeIntervalDelayTimeUnit", TimeUnit.class, TimeUnit.MILLISECONDS);
        Integer kvEndpoints = readPropertyValue(propertyPrefix + "kvEndpoints", Integer.class);
        Integer viewEndpoints = readPropertyValue(propertyPrefix + "viewEndpoints", Integer.class);
        Integer queryEndpoints = readPropertyValue(propertyPrefix + "queryEndpoints", Integer.class);
        Integer ioPoolSize = readPropertyValue(propertyPrefix + "ioPoolSize", Integer.class);
        Integer computationPoolSize = readPropertyValue(propertyPrefix + "computationPoolSize", Integer.class);
        //Advanced options
        Integer requestBufferSize = readPropertyValue(propertyPrefix + "requestBufferSize", Integer.class);
        Integer responseBufferSize = readPropertyValue(propertyPrefix + "responseBufferSize", Integer.class);
        
        if(sslEnabled != null)
        	builder.sslEnabled(sslEnabled);
        if(StringUtils.isNotBlank(sslKeystoreFile))
        	builder.sslKeystoreFile(sslKeystoreFile);
        if(StringUtils.isNotBlank(sslKeystorePassword))
        	builder.sslKeystorePassword(sslKeystorePassword);
        if(queryEnabled != null)
        	builder.queryEnabled(queryEnabled);
        if(queryPort != null)
        	builder.queryPort(queryPort);
        if(bootstrapHttpEnabled != null)
        	builder.bootstrapHttpEnabled(bootstrapHttpEnabled);
        if(bootstrapHttpDirectPort != null)
        	builder.bootstrapHttpDirectPort(bootstrapHttpDirectPort);
        if(bootstrapHttpSslPort != null)
        	builder.bootstrapHttpSslPort(bootstrapHttpSslPort);
        if(bootstrapCarrierEnabled != null)
        	builder.bootstrapCarrierEnabled(bootstrapCarrierEnabled);
        if(bootstrapCarrierDirectPort != null)
        	builder.bootstrapCarrierDirectPort(bootstrapCarrierDirectPort);
        if(bootstrapCarrierSslPort != null)
        	builder.bootstrapCarrierSslPort(bootstrapCarrierSslPort);
        if(dnsSrvEnabled != null)
        	builder.dnsSrvEnabled(dnsSrvEnabled);
        if(kvTimeout != null)
        	builder.kvTimeout(kvTimeout);
        if(viewTimeout != null)
        	builder.viewTimeout(viewTimeout);
        if(queryTimeout != null)
        	builder.queryTimeout(queryTimeout);
        if(connectTimeout != null)
        	builder.connectTimeout(connectTimeout);
        if(disconnectTimeout != null)
        	builder.disconnectTimeout(disconnectTimeout);
        if(managementTimeout != null)
        	builder.managementTimeout(managementTimeout);
        
        // //Reliability options
        //TODO
        //end of  //Reliability options
        if(maxRequestLifetime != null)
        	builder.maxRequestLifetime(maxRequestLifetime);
        if(keepAliveInterval != null)
        	builder.keepAliveInterval(keepAliveInterval);
        if(StringUtils.isNotBlank(observeIntervalDelay))
        	 //TODO builder.observeIntervalDelay(Delay.)
        if(kvEndpoints != null)
        	builder.kvEndpoints(kvEndpoints);
        if(viewEndpoints != null)
        	builder.viewEndpoints(viewEndpoints);
        if(queryEndpoints != null)
        	builder.queryEndpoints(queryEndpoints);
        if(ioPoolSize != null)
        	builder.ioPoolSize(ioPoolSize);
        if(computationPoolSize != null)
        	builder.computationPoolSize(computationPoolSize);
        if(requestBufferSize != null)
        	builder.requestBufferSize(requestBufferSize);
        if(responseBufferSize != null)
        	builder.responseBufferSize(responseBufferSize);
        
        return builder;
    }

    @Override
    public Bucket getObject() throws Exception {
    	
    	List<String> bootstrapHosts= Arrays.asList(
        			org.springframework.util.StringUtils.commaDelimitedListToStringArray(readPropertyValue(propertyPrefix + "hosts", String.class)
        					));
    	String bucketName =  readPropertyValue(propertyPrefix + "bucketname", String.class, "gravitee");
    	String bucketPassword = readPropertyValue(propertyPrefix + "bucketpassword", String.class);

        CouchbaseEnvironment options = builder().build();

        Cluster cluster = CouchbaseCluster.create(options, bootstrapHosts);
        return cluster.openBucket(bucketName, bucketPassword);
    }

    private String readPropertyValue(String propertyName) {
        return readPropertyValue(propertyName, String.class, null);
    }

    private <T> T readPropertyValue(String propertyName, Class<T> propertyType) {
        return readPropertyValue(propertyName, propertyType, null);
    }

    private <T> T readPropertyValue(String propertyName, Class<T> propertyType, T defaultValue) {
        T value = environment.getProperty(propertyName, propertyType, defaultValue);
        logger.debug("Read property {}: {}", propertyName, value);
        return value;
    }

    @Override
    public Class<?> getObjectType() {
        return Bucket.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

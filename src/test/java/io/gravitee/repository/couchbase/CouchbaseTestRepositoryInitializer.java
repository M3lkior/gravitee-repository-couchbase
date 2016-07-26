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
package io.gravitee.repository.couchbase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.query.Index;
import com.couchbase.client.java.query.N1qlQuery;

import io.gravitee.repository.Scope;
import io.gravitee.repository.config.TestRepositoryInitializer;

/**
 * @author Azize Elamrani (azize dot elamrani at gmail dot com)
 */
public class CouchbaseTestRepositoryInitializer implements TestRepositoryInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseTestRepositoryInitializer.class);

    private final static String COUCHBASE_HOST_KEY = "%s.couchbase.hosts";
    private final static String COUCHBASE_CLUSTER_USER_KEY = "%s.couchbase.clustermanager.username";
    private final static String COUCHBASE_CLUSTER_PASSWORD_KEY = "%s.couchbase.clustermanager.password";
    private final static String COUCHBASE_BUCKET_NAME_KEY = "%s.couchbase.bucketname";
    private final static String COUCHBASE_BUCKET_PASSWORD_KEY = "%s.couchbase.bucketpassword";

    @Autowired
    private CouchbaseTemplate couchbaseTemplate;

    @Autowired
    private Cluster couchbaseCluster;
    
    @Autowired
    private CouchbaseEnvironment couchbaseEnvironement;

    @Autowired
    @Qualifier("couchbaseBucket")
    private Bucket bucket;

    @Autowired
    private Environment environment;
    
    

    public void setUp() {
    	LOG.debug("ENV ? {}", environment.getProperty(String.format(COUCHBASE_HOST_KEY, Scope.MANAGEMENT.getName())));
    	BucketSettings bucketSettings = new DefaultBucketSettings.Builder()
    		    .type(BucketType.COUCHBASE)
    		    .name(environment.getProperty(String.format(COUCHBASE_BUCKET_NAME_KEY, Scope.MANAGEMENT.getName())))
    		    .password("test")
    		    .quota(100)
    		    //.replicas(1)
    		    .build();
    	 ClusterManager cm = getClusterManager();
    	 LOG.info("Inserting bucket for test...");
    	 if(!cm.hasBucket(environment.getProperty(String.format(COUCHBASE_BUCKET_NAME_KEY, Scope.MANAGEMENT.getName())))){
    		 cm.insertBucket(bucketSettings);
    		 couchbaseCluster.openBucket(environment.getProperty(String.format(COUCHBASE_BUCKET_NAME_KEY, Scope.MANAGEMENT.getName())),environment.getProperty(String.format(COUCHBASE_BUCKET_PASSWORD_KEY, Scope.MANAGEMENT.getName())));
    	 }
    	 
    	
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	couchbaseTemplate.queryN1QL( N1qlQuery.simple(Index.createPrimaryIndex().on(bucket.name())));
    }

    public void tearDown() {
        
       ClusterManager cm = getClusterManager();
       LOG.info("Dropping bucket...");
       cm.removeBucket(environment.getProperty(String.format(COUCHBASE_BUCKET_NAME_KEY, Scope.MANAGEMENT.getName())));
//        bucket.bucketManager().flush();
    }
    
    private ClusterManager getClusterManager(){
    	LOG.info("Cluster info :");
    	LOG.info(environment.getProperty(String.format(COUCHBASE_CLUSTER_USER_KEY, Scope.MANAGEMENT.getName())));
    	LOG.info(environment.getProperty(String.format(COUCHBASE_CLUSTER_PASSWORD_KEY, Scope.MANAGEMENT.getName())));
    	return couchbaseCluster.clusterManager(
        		environment.getProperty(String.format(COUCHBASE_CLUSTER_USER_KEY, Scope.MANAGEMENT.getName())),
        		environment.getProperty(String.format(COUCHBASE_CLUSTER_PASSWORD_KEY, Scope.MANAGEMENT.getName()))
        	);
    }
}

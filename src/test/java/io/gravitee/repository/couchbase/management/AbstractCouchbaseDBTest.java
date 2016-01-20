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
package io.gravitee.repository.couchbase.management;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import io.gravitee.repository.couchbase.management.config.util.CouchbaseTestContext;

/**
 * Allows to start/stop an instance of MongoDB for each tests and inject a data set provided.
 * The data set must be a json array.
 * The setup phase create a database and a collection named as the file.
 *
 * Example:
 * If the file provided path is:"/data/apis.json", for each test the setup will create a database and a collection
 * named "apis" with the file data set, execute the test, and then shut down.
 *
 *
 * @author Azize Elamrani (azize dot elamrani at gmail dot com)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestRepositoryConfiguration.class })
@ActiveProfiles("test")
public abstract class AbstractCouchbaseDBTest {

    private static Logger LOG = LoggerFactory.getLogger(AbstractCouchbaseDBTest.class);
    private static String BUCKET_NAME = "gravitee";

    @Autowired
    CouchbaseTemplate template;
    @Autowired
    Cluster cbCluster;
    
    @Autowired
    @Qualifier("couchbaseBucket")
    Bucket bucket;

    protected abstract String getTestCasesPath();

    private static final String JSON_EXTENSION = "json";
    
    private static CouchbaseTestContext ctx;
    
    @Before
    public void setup() throws Exception {
        LOG.info("Setup of Couchbase Cluster for Integration Test");

       // mongoDatabase = ((MongoClient) mongoClient).getDatabase(DATABASE_NAME);
        
        final File file = new File(AbstractCouchbaseDBTest.class.getResource(getTestCasesPath()).toURI());

        File[] collectionsDumps = file.listFiles(
                pathname -> pathname.isFile()
                        && JSON_EXTENSION.equalsIgnoreCase(FilenameUtils.getExtension(pathname.toString())));
        
        
//        ClusterManager cManager =cbCluster.clusterManager("Administrator", "password");
//        if(!cManager.hasBucket(BUCKET_NAME)){
//        	LOG.info("Creating bucket {}", BUCKET_NAME);
//        	BucketSettings bs = new DefaultBucketSettings.Builder().
//            		name(BUCKET_NAME)
//            		.password("test")
//            		.enableFlush(true)
//            		.quota(100);
//        	
//        	cManager.insertBucket(bs);
//        }else{
//        	bucket.bucketManager().flush();
//        }
        
        
        importJsonFiles(collectionsDumps);
    }

    private void importJsonFiles(File[] files) throws Exception {
    	if(files != null){
    		for (File file : files) {
				importJsonFile(file);
			}
    	}
    }
    
    private void importJsonFile(File file) throws Exception {
        
        final String documentType = FilenameUtils.getBaseName(file.getName());
        LOG.info("Inserting document of type {}", documentType);
        
        String jsonFileContent = FileUtils.readFileToString(file);
        if(jsonFileContent.startsWith("[")){
        JsonArray jsonArray = JsonArray.fromJson(jsonFileContent);
        jsonArray.forEach(jsonObject -> {
        	 JsonDocument document = JsonDocument.create(((JsonObject) jsonObject).getString("_id"), (JsonObject) jsonObject);
        	 bucket.insert(document);
        });
        }else{
        	JsonObject jsonObject = JsonObject.fromJson(jsonFileContent);
        	JsonDocument document = JsonDocument.create(jsonObject.getString("_id"), jsonObject);

        	bucket.insert(document);
        }
       
    }
    
   
}
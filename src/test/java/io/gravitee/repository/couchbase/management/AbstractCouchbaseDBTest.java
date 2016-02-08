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
import org.springframework.transaction.annotation.Transactional;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.query.Index;
import com.couchbase.client.java.query.N1qlQuery;

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
    
    
    @Before
    @Transactional
    public void setup() throws Exception {
        LOG.info("Setup of Couchbase Cluster for Integration Test");
        final File file = new File(AbstractCouchbaseDBTest.class.getResource(getTestCasesPath()).toURI());
        File[] collectionsDumps = file.listFiles(
                pathname -> pathname.isFile()
                        && JSON_EXTENSION.equalsIgnoreCase(FilenameUtils.getExtension(pathname.toString())));
       
        //template.queryN1QL( N1qlQuery.simple(Index.dropPrimaryIndex(bucket.name())));
        template.queryN1QL( N1qlQuery.simple(Index.createPrimaryIndex().on(bucket.name())));
        LOG.debug("Flushing bucket ...");
        bucket.bucketManager().flush();
      
        importJsonFiles(collectionsDumps);
        //workaround to avoid select before inserts commits ...
        Thread.sleep(1000L);
       
    }
    
    private void importJsonFiles(File[] files) throws Exception {
    	if(files != null){
    		for (File file : files) {
				importJsonFile(file);
			}
    	}
    }
    
    @Transactional
    private void importJsonFile(File file) {
        try{
	        final String documentType = FilenameUtils.getBaseName(file.getName());
	        LOG.info("Inserting document of type {}", documentType);
	        
	        String jsonFileContent = FileUtils.readFileToString(file);
	        if(jsonFileContent.startsWith("[")){
		        JsonArray jsonArray = JsonArray.fromJson(jsonFileContent);
		        jsonArray.forEach(jsonObject -> {
		        	String id = ((JsonObject) jsonObject).getString("_id");
		        	 JsonDocument document = JsonDocument.create(id, (JsonObject) jsonObject);
		        	 if(!bucket.exists(id)){
		        		 bucket.insert(document);
		        	 }else{
		        		 LOG.debug("Document already exist in bucket, skipping");
		        	 }
		        });
	        }else{
	        	JsonObject jsonObject = JsonObject.fromJson(jsonFileContent);
	        	String id = jsonObject.getString("_id");
	        	JsonDocument document = JsonDocument.create(id, jsonObject);
	        	if(!bucket.exists(id)){
	        		bucket.insert(document);
	        	}else{
	        		 LOG.debug("Document already exist in bucket, skipping");
	        	 }
	        }
        }catch(DocumentAlreadyExistsException e){
        	LOG.debug("Document already exist in bucket, skipping");
        }catch(Exception e){
        	LOG.error("Fail to insert document : ", e);
        }
       
    }
    
   
}
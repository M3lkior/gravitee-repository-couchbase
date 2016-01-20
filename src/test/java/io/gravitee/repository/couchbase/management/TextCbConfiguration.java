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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.bucket.BucketInfo;

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
public class TextCbConfiguration {

    private static Logger LOG = LoggerFactory.getLogger(TextCbConfiguration.class);
    
    @Autowired
    Bucket b;
    
    @Autowired
    BucketInfo bi;
    
    @Test
    public void testBucket(){
    	Assert.notNull(b);
    	
    }
   
}
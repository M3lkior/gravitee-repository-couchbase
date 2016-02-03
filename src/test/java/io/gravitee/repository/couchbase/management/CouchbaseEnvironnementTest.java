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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.couchbase.client.java.env.CouchbaseEnvironment;

/**
 * Testing class for Couchbase Environment configuration
 * 
 * @author Ludovic DUSSART (ludovic dot dussart dot pro at gmail dot com)
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { CouchbaseEnvironnementConfiguration.class })
@ActiveProfiles("test")
public class CouchbaseEnvironnementTest {
	private final static String KEY_PREFIX= "management.couchbase.";

    private static Logger LOG = LoggerFactory.getLogger(CouchbaseEnvironnementTest.class);
    
    @Autowired
    CouchbaseEnvironment couchbaseEnvironment;
    
    @Autowired
    Environment env;
    
    @Test
    public void testCouchbaseEnvironment(){
    	Assert.assertEquals("kvTimeout problem", (Long) env.getProperty(KEY_PREFIX + "kvTimeout", Long.class), (Long) couchbaseEnvironment.kvTimeout());
    }
   
}
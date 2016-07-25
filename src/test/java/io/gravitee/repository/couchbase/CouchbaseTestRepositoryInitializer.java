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

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.query.Index;
import com.couchbase.client.java.query.N1qlQuery;
import io.gravitee.repository.Scope;
import io.gravitee.repository.config.TestRepositoryInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

/**
 * @author Azize Elamrani (azize dot elamrani at gmail dot com)
 */
public class CouchbaseTestRepositoryInitializer implements TestRepositoryInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseTestRepositoryInitializer.class);

    @Autowired
    private CouchbaseTemplate couchbaseTemplate;

    @Autowired
    private CouchbaseEnvironment couchbaseEnvironment;

    @Autowired
    @Qualifier("couchbaseBucket")
    private Bucket bucket;

    @Autowired
    private Environment environment;

    public void setUp() {
    	LOG.debug("ENV ? {}", environment.getProperty(Scope.MANAGEMENT.getName() + ".couchbase.hosts", "gravitee"));

    	couchbaseTemplate.queryN1QL( N1qlQuery.simple(Index.createPrimaryIndex().on(bucket.name())));
    }

    public void tearDown() {
        LOG.info("Dropping database...");
//        bucket.bucketManager().flush();
    }
}

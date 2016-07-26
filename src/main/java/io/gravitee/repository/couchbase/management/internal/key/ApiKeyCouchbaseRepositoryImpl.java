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
package io.gravitee.repository.couchbase.management.internal.key;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.error.DocumentDoesNotExistException;

public class ApiKeyCouchbaseRepositoryImpl implements ApiKeyCouchbaseRepositoryCustom {
	private final static Logger logger = LoggerFactory.getLogger(ApiKeyCouchbaseRepositoryImpl.class);
	private final static String API_KEY_PATTERN = "apikey::%d";

	@Autowired
	private CouchbaseTemplate cbTemplate;

	
	private final static String API_KEY_COUNTER_ID = "apikey_counter";

	@Override
	public String getIdForApiKey() {
		Bucket bucket = cbTemplate.getCouchbaseBucket();

		JsonLongDocument longId = bucket.counter(API_KEY_COUNTER_ID, 1, 1);
		Long indexValue = longId.content();

		return String.format(API_KEY_PATTERN, indexValue);
	}
}

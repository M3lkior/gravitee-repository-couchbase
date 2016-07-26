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
package io.gravitee.repository.couchbase.management.internal.event;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.repository.query.support.N1qlUtils;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.query.dsl.path.WherePath;

import io.gravitee.repository.couchbase.management.internal.model.EventCouchbase;

public class EventCouchbaseRepositoryImpl implements EventCouchbaseRepositoryCustom {
	private final static Logger logger = LoggerFactory.getLogger(EventCouchbaseRepositoryImpl.class);
	private final static String EVENT_ID_PATTERN = "event::%d";
	private final static String EVENT_COUNTER_ID = "event_counter";
	private final static String CLASS_FIELD_VALUE = EventCouchbase.class.getName();
	private final static String CLASS_FIELD = "_class";
	
	private final static String PROPERTIES_PREFIX_FIELD= "properties.";
    @Autowired
    private CouchbaseTemplate cbTemplate;
    
    @Override
    public Collection<EventCouchbase> findByProperty(String key, String value) {
    	JsonObject parameters = JsonObject.create().put("value", value);
		WherePath baseStatement = N1qlUtils.createSelectFromForEntity(cbTemplate.getCouchbaseBucket().name());
		//workaroung because spring-data not include _class criteria in his build query
		Expression baseExpression = Expression.x(CLASS_FIELD).eq(Expression.x("$class"));
		parameters.put("class", CLASS_FIELD_VALUE);
		Expression propertyExpression = Expression.x(PROPERTIES_PREFIX_FIELD + key).eq(Expression.x("$value"));;
		
		N1qlQuery query = N1qlQuery.parameterized(baseStatement.where(baseExpression.and(propertyExpression)), parameters);
		return cbTemplate.findByN1QL(query, EventCouchbase.class);
    }

	@Override
	public String getIdForEvent() {
		Bucket bucket = cbTemplate.getCouchbaseBucket();

		JsonLongDocument counterDoc = bucket.counter(EVENT_COUNTER_ID, 1, 1);
		Long counter = counterDoc.content();

		return String.format(EVENT_ID_PATTERN, counter);
	}
}

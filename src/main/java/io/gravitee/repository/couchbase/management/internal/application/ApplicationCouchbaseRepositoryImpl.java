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
package io.gravitee.repository.couchbase.management.internal.application;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.functions.AggregateFunctions.count;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.repository.query.CountFragment;
import org.springframework.data.couchbase.repository.query.support.N1qlUtils;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.query.dsl.functions.Collections;
import com.couchbase.client.java.query.dsl.path.AsPath;
import com.couchbase.client.java.query.dsl.path.WherePath;

import io.gravitee.repository.couchbase.management.internal.model.ApplicationCouchbase;
import io.gravitee.repository.management.model.MembershipType;


public class ApplicationCouchbaseRepositoryImpl implements ApplicationCouchbaseRepositoryCustom {
	private final static String MEMBERS_FIELD = "members";
	private final static String MEMBER_FIELD = "member";
	private final static String MEMBERS_USER_FIELD = MEMBER_FIELD + ".`user`";
	private final static String MEMBERS_TYPE_FIELD = MEMBER_FIELD + ".type";
	private final static String CLASS_FIELD_VALUE = ApplicationCouchbase.class.getName();
	private final static String CLASS_FIELD = "_class";
	@Autowired
	private CouchbaseTemplate cbTemplate;

	@Override
	public Collection<ApplicationCouchbase> findByUser(String username, MembershipType membershipType) {
		JsonObject parameters = JsonObject.create();
		WherePath baseStatement = N1qlUtils.createSelectFromForEntity(cbTemplate.getCouchbaseBucket().name());
		//workaroung because spring-data not include _class criteria in his build query
		Expression baseExpression = Expression.x(CLASS_FIELD).eq(Expression.x("$class"));
		parameters.put("class", CLASS_FIELD_VALUE);
		Expression memberExpression = null;
		if (username != null) {
			parameters.put("username",username);

			if (membershipType == null) {
				memberExpression = Collections.anyIn(MEMBER_FIELD, Expression.x(MEMBERS_FIELD)).satisfies(Expression.x(MEMBERS_USER_FIELD).eq(Expression.x("$username")));;
			} else {
				parameters.put("membershipType",membershipType.name());
				memberExpression = Collections.anyIn(MEMBER_FIELD, Expression.x(MEMBERS_FIELD))
						.satisfies(
								Expression.x(MEMBERS_USER_FIELD).eq(Expression.x("$username"))
								.and(Expression.x(MEMBERS_TYPE_FIELD).eq(Expression.x("$membershipType")))
								);
			}
		}
		
		N1qlQuery query = N1qlQuery.parameterized(baseStatement.where(baseExpression.and(memberExpression)), parameters);
		return cbTemplate.findByN1QL(query, ApplicationCouchbase.class);
	}

	@Override
	public int countByUser(String username, MembershipType membershipType) {
	
		AsPath baseStatement = select(count("*").as(CountFragment.COUNT_ALIAS)).from(N1qlUtils.escapedBucket(cbTemplate.getCouchbaseBucket().name()));
		
		Expression e = Collections.anyIn(MEMBER_FIELD, Expression.x(MEMBERS_FIELD)).satisfies(Expression.x(MEMBERS_USER_FIELD).eq(Expression.x("$username")));;
		JsonObject parameters = JsonObject.create().put("username",username);
		if(membershipType != null){	
			parameters.put("membershipType",membershipType.name());
				e = Collections.anyIn(MEMBER_FIELD, Expression.x(MEMBERS_FIELD))
						.satisfies(Expression.x(MEMBERS_USER_FIELD).eq(Expression.x("$username"))
								.and(Expression.x(MEMBERS_TYPE_FIELD).eq(Expression.x("$membershipType"))));
		}

		N1qlQuery query = N1qlQuery.parameterized(baseStatement.where(e), parameters);
		N1qlQueryResult result = cbTemplate.queryN1QL(query);
		
		return result.iterator().next().value().getInt(CountFragment.COUNT_ALIAS);
	}
}

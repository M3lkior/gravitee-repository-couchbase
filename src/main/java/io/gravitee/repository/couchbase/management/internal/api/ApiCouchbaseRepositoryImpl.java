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
package io.gravitee.repository.couchbase.management.internal.api;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.dsl.Expression;
import com.couchbase.client.java.query.dsl.path.AsPath;

import io.gravitee.repository.couchbase.management.internal.model.ApiCouchbase;
import io.gravitee.repository.management.model.MembershipType;
import io.gravitee.repository.management.model.Visibility;

public class ApiCouchbaseRepositoryImpl implements ApiCouchbaseRepositoryCustom {

	@Autowired
	private CouchbaseTemplate cbTemplate;

	@Override
	public Collection<ApiCouchbase> findByMember(String username, MembershipType membershipType, Visibility visibility) {
				
		AsPath baseStatement = Select.select("*").from(this.cbTemplate.getCouchbaseBucket().name());
		
		
		Expression visibilityExpression = null;
		if (visibility != null) {
			visibilityExpression = Expression.x("visibility").eq(visibility.toString());
		}

		Expression memberExpression = null;
		if (username != null) {
			if (membershipType == null) {
				 memberExpression = Expression.x(username).in(Expression.x("members"));
			} else {
				memberExpression = Expression.x(username).in(Expression.x("members"))
						.and(Expression.x("type").eq(membershipType.name()));
			}
		}
		Expression e = null;
		if(visibilityExpression != null){
			e = visibilityExpression;
			
			if(memberExpression != null){
				e.and(memberExpression);
			}
		}else if(memberExpression != null){
			e = memberExpression;
		}
		
		N1qlQuery query = N1qlQuery.simple(baseStatement.where(e));
		return cbTemplate.findByN1QL(query, ApiCouchbase.class);
	}

	@Override
	public int countByUser(String username, MembershipType membershipType) {
//		Criteria criteriaMember;
//
//		if (membershipType == null) {
//			criteriaMember = Criteria.where("members").elemMatch(Criteria.where("user.$id").is(username));
//		} else {
//			criteriaMember = Criteria.where("members").elemMatch(Criteria.where("user.$id").is(username).and("type").is(membershipType));
//		}
//
//		Query query = new Query();
//		query.addCriteria(criteriaMember);

		return 0;//(int) cbTemplate.count(query, ApiCouchbase.class);
	}
}

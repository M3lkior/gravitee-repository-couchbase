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
package io.gravitee.repository.couchbase.management.internal.page;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

/**
 * @author Titouan COMPIEGNE
 */
public class PageCouchbaseRepositoryImpl implements PageCouchbaseRepositoryCustom {

	@Autowired
	private CouchbaseTemplate cbTemplate;

	public int findMaxPageOrderByApi(String apiId) {
//		Query query = new Query();
//		query.limit(1);
//		query.with(new Sort(Sort.Direction.DESC, "order"));
//		query.addCriteria(Criteria.where("api").is(apiId));
//
//		PageCouchbase page = cbTemplate.findOne(query, PageCouchbase.class);
		return 0;//(page != null) ? page.getOrder() : 0;
	}
}
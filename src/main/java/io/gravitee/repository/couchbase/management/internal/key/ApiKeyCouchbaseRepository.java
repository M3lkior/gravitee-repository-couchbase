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

import java.util.List;

import org.springframework.data.couchbase.repository.CouchbaseRepository;

import io.gravitee.repository.couchbase.management.internal.model.ApiKeyCouchbase;

public interface ApiKeyCouchbaseRepository extends CouchbaseRepository<ApiKeyCouchbase, String>, ApiKeyCouchbaseRepositoryCustom {

	List<ApiKeyCouchbase> findByApplicationAndApi(String applicationId, String apiId);

	List<ApiKeyCouchbase> findByApplication(String applicationId);

	List<ApiKeyCouchbase> findByApi(String apiId);

	ApiKeyCouchbase findByKey(String apiKey);
}




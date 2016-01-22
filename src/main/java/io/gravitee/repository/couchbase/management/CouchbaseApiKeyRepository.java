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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.gravitee.repository.couchbase.management.internal.api.ApiCouchbaseRepository;
import io.gravitee.repository.couchbase.management.internal.application.ApplicationCouchbaseRepository;
import io.gravitee.repository.couchbase.management.internal.key.ApiKeyCouchbaseRepository;
import io.gravitee.repository.couchbase.management.internal.model.ApiKeyCouchbase;
import io.gravitee.repository.couchbase.management.mapper.GraviteeMapper;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ApiKeyRepository;
import io.gravitee.repository.management.model.ApiKey;

@Component
public class CouchbaseApiKeyRepository implements ApiKeyRepository {

	private final static Logger logger = LoggerFactory.getLogger(CouchbaseApiKeyRepository.class);

	@Autowired
	private GraviteeMapper mapper;

	@Autowired
	private ApiKeyCouchbaseRepository internalApiKeyRepo;
	
	@Autowired
	private ApiCouchbaseRepository internalApiRepo;	
	
	@Autowired
	private ApplicationCouchbaseRepository internalApplicationRepo;

	private Set<ApiKey> map(Collection<ApiKeyCouchbase> apiKeys){
		if (apiKeys == null){
			return Collections.emptySet();
		}
		
		return apiKeys.stream().map(apiKeysCb -> {
            ApiKey key = mapper.map(apiKeysCb.getKey(), ApiKey.class);
            key.setApi(apiKeysCb.getKey());
			key.setApplication(apiKeysCb.getApplication());

            return key;
        }).collect(Collectors.toSet());
	}

	@Override
	public Set<ApiKey> findByApplicationAndApi(String applicationId, String apiId) throws TechnicalException {
		List<ApiKeyCouchbase> apiKeysCb = internalApiKeyRepo.findByApplicationAndApi(applicationId, apiId);
		
		return map(apiKeysCb);
	}

	@Override
	public Set<ApiKey> findByApplication(String applicationId) throws TechnicalException {
		List<ApiKeyCouchbase> apiKeysCb = internalApiKeyRepo.findByApplication(applicationId);
		
		return map(apiKeysCb);
	}


	@Override
	//fixme test create
	public ApiKey create(String applicationId, String apiId, ApiKey key) throws TechnicalException {
		ApiKeyCouchbase apiKeysCb = new ApiKeyCouchbase();
		apiKeysCb.setApi(internalApiRepo.findOne(apiId).getId());
		apiKeysCb.setApplication(internalApplicationRepo.findOne(applicationId).getId());
		apiKeysCb.setKey(mapper.map(key, ApiKeyCouchbase.class).getKey());
		
		internalApiKeyRepo.save(apiKeysCb);
		
		return key;
	}

	@Override
	public ApiKey update(ApiKey key) throws TechnicalException {
		ApiKeyCouchbase apiKey = internalApiKeyRepo.findOne(key.getKey());
		apiKey.setCreatedAt(key.getCreatedAt());
		apiKey.setExpiration(key.getExpiration());
		apiKey.setRevoked(key.isRevoked());
		apiKey.setRevokeAt(key.getRevokeAt());

		internalApiKeyRepo.save(apiKey);
		
		return key;
	}

	@Override
	public Optional<ApiKey> retrieve(String apiKey) throws TechnicalException {
		ApiKeyCouchbase apiKeyCb = internalApiKeyRepo.findOne(apiKey);

		if(apiKeyCb != null) {
			ApiKey retKey = mapper.map(apiKeyCb.getKey(), ApiKey.class);
			retKey.setApi(apiKeyCb.getApi());
			return Optional.of(retKey);
		}
		
		return Optional.empty();
	}

	@Override
	public Set<ApiKey> findByApi(String apiId) {
		Collection<ApiKeyCouchbase> apiKeysCb = internalApiKeyRepo.findByApi(apiId);
		return map(apiKeysCb);
	}

	@Override
	public void delete(String apiKey) throws TechnicalException {
		internalApiKeyRepo.delete(apiKey);
	}
}

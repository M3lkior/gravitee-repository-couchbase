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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.dozer.util.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.gravitee.repository.couchbase.management.internal.api.ApiCouchbaseRepository;
import io.gravitee.repository.couchbase.management.internal.key.ApiKeyCouchbaseRepository;
import io.gravitee.repository.couchbase.management.internal.model.ApiCouchbase;
import io.gravitee.repository.couchbase.management.internal.model.ApiKeyCouchbase;
import io.gravitee.repository.couchbase.management.internal.model.MembershipCouchbase;
import io.gravitee.repository.couchbase.management.internal.model.UserCouchbase;
import io.gravitee.repository.couchbase.management.internal.user.UserCouchbaseRepository;
import io.gravitee.repository.couchbase.management.mapper.GraviteeMapper;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ApiRepository;
import io.gravitee.repository.management.model.Api;
import io.gravitee.repository.management.model.Membership;
import io.gravitee.repository.management.model.MembershipType;
import io.gravitee.repository.management.model.User;
import io.gravitee.repository.management.model.Visibility;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 * @author Gravitee.io Team
 */
@Component
public class CouchbaseApiRepository implements ApiRepository {

	@Autowired
	private ApiKeyCouchbaseRepository internalApiKeyRepo;
	
	@Autowired
	private ApiCouchbaseRepository internalApiRepo;
	
	@Autowired
	private UserCouchbaseRepository internalUserRepo;
	
	@Autowired
	private GraviteeMapper mapper;
	
	@Override
	public Optional<Api> findById(String apiId) throws TechnicalException {
		ApiCouchbase api =  internalApiRepo.findOne(apiId);
		return Optional.ofNullable(mapApi(api));
	}

	@Override
	public Set<Api> findAll() throws TechnicalException {
		Iterable<ApiCouchbase> apis = internalApiRepo.findAll();
		return mapApis(IteratorUtils.toList(apis.iterator()));
	}
	
	@Override
	public Api create(Api api) throws TechnicalException {
		ApiCouchbase apiCb = mapApi(api);
		ApiCouchbase apiCbCreated = internalApiRepo.save(apiCb);
		return mapApi(apiCbCreated);
	}

	@Override
	public Api update(Api api) throws TechnicalException {
		ApiCouchbase apiCb = internalApiRepo.findOne(api.getId());

		// Update, but don't change invariant other creation information
		apiCb.setName(api.getName());
		apiCb.setDescription(api.getDescription());
		apiCb.setUpdatedAt(api.getUpdatedAt());
		apiCb.setLifecycleState(api.getLifecycleState());
		apiCb.setDefinition(api.getDefinition());
		apiCb.setVisibility(api.getVisibility());
		apiCb.setVersion(api.getVersion());
		
		ApiCouchbase applicationCbUpdated = internalApiRepo.save(apiCb);
		return mapApi(applicationCbUpdated);
	}

	@Override
	public void delete(String apiId) throws TechnicalException {
		internalApiRepo.delete(apiId);
	}

	@Override
	public Set<Api> findByMember(String username, MembershipType membershipType, Visibility visibility) throws TechnicalException {
		return mapApis(internalApiRepo.findByMember(username, membershipType, visibility));
	}

	@Override
	public int countByUser(String username, MembershipType membershipType) throws TechnicalException {
		return internalApiRepo.countByUser(username, membershipType);
	}

	@Override
	public Set<Api> findByApplication(String applicationId) throws TechnicalException {
		List<ApiKeyCouchbase> apiKeys = internalApiKeyRepo.findByApplication(applicationId);
		Set<Api> apis = new HashSet<Api>();
		for(ApiKeyCouchbase apikey: apiKeys){
			Optional<Api> api = findById(apikey.getApi());
			if(api.isPresent()){
				apis.add(api.get());
			}
		}
		
		return apis;
}
	
	@Override
	public void saveMember(String apiId, String username, MembershipType membershipType) throws TechnicalException {
		ApiCouchbase api = internalApiRepo.findOne(apiId);
		UserCouchbase user = internalUserRepo.findOne(username);

		Membership membership = getMember(apiId, username);
		if (membership == null) {
			MembershipCouchbase member = new MembershipCouchbase();
			member.setUser(user.getName());
			member.setType(membershipType);
			member.setCreatedAt(new Date());
			member.setUpdatedAt(member.getCreatedAt());

			api.getMembers().add(member);

			internalApiRepo.save(api);
		} else {
			for (MembershipCouchbase member : api.getMembers()) {
				if (member.getUser().equalsIgnoreCase(username)) {
					member.setType(membershipType);
					internalApiRepo.save(api);
					break;
				}
			}
		}
	}

	@Override
	public void deleteMember(String apiId, String username) throws TechnicalException {
		ApiCouchbase api = internalApiRepo.findOne(apiId);
		MembershipCouchbase memberToDelete = null;

		for (MembershipCouchbase member : api.getMembers()) {
			if (member.getUser().equalsIgnoreCase(username)) {
				memberToDelete = member;
			}
		}

		if (memberToDelete != null) {
			api.getMembers().remove(memberToDelete);
			internalApiRepo.save(api);
		}
	}

	@Override
	public Membership getMember(String apiId, String username) throws TechnicalException {
		Collection<Membership> members = getMembers(apiId, null);
		for (Membership member : members) {
			if (member.getUser().getUsername().equalsIgnoreCase(username)) {
				return member;
			}
		}

		return null;
	}

	@Override
	public Collection<Membership> getMembers(String apiId, MembershipType membershipType) throws TechnicalException {
		ApiCouchbase api = internalApiRepo.findOne(apiId);
		List<MembershipCouchbase> membersCb = api.getMembers();
		Set<Membership> members = new HashSet<>(membersCb.size());

		for (MembershipCouchbase membership : membersCb) {
			if (membershipType == null || (
					membershipType != null && membership.getType().toString().equalsIgnoreCase(membershipType.toString()))) {
				Membership member = new Membership();
				member.setUser(mapUser(internalUserRepo.findOne(membership.getUser())));
				member.setMembershipType(membership.getType());
				member.setCreatedAt(membership.getCreatedAt());
				member.setUpdatedAt(membership.getUpdatedAt());
				members.add(member);
			}
		}

		return members;
	}

	private User mapUser(final UserCouchbase userCb) {
		final User user = new User();
		user.setUsername(userCb.getName());
		user.setCreatedAt(userCb.getCreatedAt());
		user.setEmail(userCb.getEmail());
		user.setFirstname(userCb.getFirstname());
		user.setLastname(userCb.getLastname());
		user.setPassword(userCb.getPassword());
		user.setUpdatedAt(userCb.getUpdatedAt());
		user.setRoles(new HashSet<>(userCb.getRoles()));
		return user;
	}

	private Set<Api> mapApis(Collection<ApiCouchbase> apis) {
		return apis.stream().map(this::mapApi).collect(Collectors.toSet());
	}

	private ApiCouchbase mapApi(Api api){
		return (api == null) ? null : mapper.map(api, ApiCouchbase.class);
	}

	private Api mapApi(ApiCouchbase api){
		return (api == null) ? null : mapper.map(api, Api.class);
	}
}

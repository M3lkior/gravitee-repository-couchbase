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

import io.gravitee.repository.couchbase.management.internal.application.ApplicationCouchbaseRepository;
import io.gravitee.repository.couchbase.management.internal.model.ApplicationCouchbase;
import io.gravitee.repository.couchbase.management.internal.model.MembershipCouchbase;
import io.gravitee.repository.couchbase.management.internal.model.UserCouchbase;
import io.gravitee.repository.couchbase.management.internal.user.UserCouchbaseRepository;
import io.gravitee.repository.couchbase.management.mapper.GraviteeMapper;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ApplicationRepository;
import io.gravitee.repository.management.model.Application;
import io.gravitee.repository.management.model.Membership;
import io.gravitee.repository.management.model.MembershipType;
import io.gravitee.repository.management.model.User;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 * @author Gravitee.io Team
 */
@Component
public class CouchbaseApplicationRepository implements ApplicationRepository {

	@Autowired
	private ApplicationCouchbaseRepository internalApplicationRepo;
	
	@Autowired
	private UserCouchbaseRepository internalUserRepo;
	
	@Autowired
	private GraviteeMapper mapper;

	@Override
	public Set<Application> findAll() throws TechnicalException {
		Iterable<ApplicationCouchbase> applications = internalApplicationRepo.findAll();
		return mapApplications(IteratorUtils.toList(applications.iterator()));
	}

	@Override
	public Application create(Application application) throws TechnicalException {
		ApplicationCouchbase applicationCb = mapApplication(application);
		ApplicationCouchbase applicationCbCreated = internalApplicationRepo.save(applicationCb);
		return mapApplication(applicationCbCreated);
	}

	@Override
	public Application update(Application application) throws TechnicalException {
		ApplicationCouchbase applicationCb = internalApplicationRepo.findOne(application.getId());
		
		// Update, but don't change invariant other creation information
		applicationCb.setName(application.getName());
		applicationCb.setDescription(application.getDescription());
		applicationCb.setUpdatedAt(application.getUpdatedAt());
		applicationCb.setType(application.getType());

		ApplicationCouchbase applicationMongoUpdated = internalApplicationRepo.save(applicationCb);
		return mapApplication(applicationMongoUpdated);
	}

	@Override
	public Optional<Application> findById(String applicationId) throws TechnicalException {
		ApplicationCouchbase application = internalApplicationRepo.findOne(applicationId);
		return Optional.ofNullable(mapApplication(application));
	}

	@Override
	public void delete(String applicationId) throws TechnicalException {
		internalApplicationRepo.delete(applicationId);
	}

	@Override
	public Set<Application> findByUser(String username, MembershipType membershipType) throws TechnicalException {
		return mapApplications(internalApplicationRepo.findByUser(username, membershipType));
	}
	
	@Override
	public void saveMember(String applicationId, String username, MembershipType membershipType) throws TechnicalException {
		ApplicationCouchbase applicationCb = internalApplicationRepo.findOne(applicationId);
		UserCouchbase userCb = internalUserRepo.findOne(username);

		Membership membership = getMember(applicationId, username);
		if (membership == null) {
			MembershipCouchbase memberCb = new MembershipCouchbase();
			memberCb.setUser(userCb.getUsername());
			memberCb.setType(membershipType);
			memberCb.setCreatedAt(new Date());
			memberCb.setUpdatedAt(memberCb.getCreatedAt());

			applicationCb.getMembers().add(memberCb);

			internalApplicationRepo.save(applicationCb);
		} else {
			for (MembershipCouchbase membershipCb : applicationCb.getMembers()) {
				if (membershipCb.getUser().equalsIgnoreCase(username)) {
					membershipCb.setType(membershipType);
					internalApplicationRepo.save(applicationCb);
					break;
				}
			}
		}
	}

	@Override
	public void deleteMember(String applicationId, String username) throws TechnicalException {
		ApplicationCouchbase application = internalApplicationRepo.findOne(applicationId);
		MembershipCouchbase memberToDelete = null;

		for (MembershipCouchbase memberCb : application.getMembers()) {
			if (memberCb.getUser().equalsIgnoreCase(username)) {
				memberToDelete = memberCb;
			}
		}

		if (memberToDelete != null) {
			application.getMembers().remove(memberToDelete);
			internalApplicationRepo.save(application);
		}
	}

	@Override
	public Collection<Membership> getMembers(String applicationId, MembershipType membershipType) throws TechnicalException {
		ApplicationCouchbase application = internalApplicationRepo.findOne(applicationId);
		List<MembershipCouchbase> membersCb = application.getMembers();
		Set<Membership> members = new HashSet<>(membersCb.size());

		for (MembershipCouchbase memberCb : membersCb) {
			if (membershipType == null || (
					membershipType != null && memberCb.getType().toString().equalsIgnoreCase(membershipType.toString()))) {
				Membership member = new Membership();
				member.setUser(mapUser(internalUserRepo.findOne(memberCb.getUser())));
				member.setMembershipType(memberCb.getType());
				member.setCreatedAt(memberCb.getCreatedAt());
				member.setUpdatedAt(memberCb.getUpdatedAt());
				members.add(member);
			}
		}

		return members;
	}

	private User mapUser(final UserCouchbase userCb) {
		final User user = new User();
		user.setUsername(userCb.getUsername());
		user.setCreatedAt(userCb.getCreatedAt());
		user.setEmail(userCb.getEmail());
		user.setFirstname(userCb.getFirstname());
		user.setLastname(userCb.getLastname());
		user.setPassword(userCb.getPassword());
		user.setUpdatedAt(userCb.getUpdatedAt());
		user.setRoles( userCb.getRoles() != null ? new HashSet<>(userCb.getRoles()) :  new HashSet<>());
		return user;
	}

	@Override
	public Membership getMember(String application, String username) throws TechnicalException {
		Collection<Membership> members = getMembers(application, null);
		for (Membership member : members) {
			if (member.getUser().getUsername().equalsIgnoreCase(username)) {
				return member;
			}
		}

		return null;
	}

	private Set<Application> mapApplications(Collection<ApplicationCouchbase> applications){
		return applications.stream().map(this::mapApplication).collect(Collectors.toSet());
	}
	
	private Application mapApplication(ApplicationCouchbase application) {
		return (application == null) ? null : mapper.map(application, Application.class);
	}
	
	private ApplicationCouchbase mapApplication(Application application) {
		return (application == null) ? null : mapper.map(application, ApplicationCouchbase.class);
	}
}

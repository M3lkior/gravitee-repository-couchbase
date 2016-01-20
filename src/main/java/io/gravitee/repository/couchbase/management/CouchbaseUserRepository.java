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

import java.util.Optional;
import java.util.Set;

import org.dozer.util.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.gravitee.repository.couchbase.management.internal.model.UserCouchbase;
import io.gravitee.repository.couchbase.management.internal.user.UserCouchbaseRepository;
import io.gravitee.repository.couchbase.management.mapper.GraviteeMapper;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.UserRepository;
import io.gravitee.repository.management.model.User;

/**
 * @author David BRASSELY (brasseld at gmail.com)
 */
@Component
public class CouchbaseUserRepository implements UserRepository {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserCouchbaseRepository internalUserRepo;

	@Autowired
	private GraviteeMapper mapper;

	@Override
	public Optional<User> findByUsername(String username) throws TechnicalException {
		logger.debug("Find user by name user [{}]", username);

		UserCouchbase user = internalUserRepo.findOne(username);
		User res = mapper.map(user, User.class);

		logger.debug("Find user by name user [{}] - Done", username);
		return Optional.ofNullable(res);
	}

	@Override
	public Set<User> findAll() throws TechnicalException {
		logger.debug("Find all users");
		
		Iterable<UserCouchbase> users = internalUserRepo.findAll();
		Set<User> res = mapper.collection2set(IteratorUtils.toList(users.iterator()), UserCouchbase.class, User.class);

		logger.debug("Find all users - Done");
		return res;
	}

	@Override
	public User create(User user) throws TechnicalException {
		logger.debug("Create user [{}]", user.getUsername());
		
		UserCouchbase userCouchbase = mapper.map(user, UserCouchbase.class);
		UserCouchbase createdUserCb = internalUserRepo.save(userCouchbase);
		
		User res = mapper.map(createdUserCb, User.class);
		
		logger.debug("Create user [{}] - Done", user.getUsername());
		
		return res;
	}

	@Override
	public Optional<User> findByEmail(String email) throws TechnicalException {
		logger.debug("Find users by email [{}]", email);

		UserCouchbase userMongo = internalUserRepo.findByEmail(email);
		User res = mapper.map(userMongo, User.class);

		logger.debug("Find users by email [{}] - Done", email);
		return Optional.ofNullable(res);
	}
}

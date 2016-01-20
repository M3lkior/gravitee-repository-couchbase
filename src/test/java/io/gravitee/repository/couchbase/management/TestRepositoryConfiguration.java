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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.couchbase.repository.config.EnableCouchbaseRepositories;

import com.couchbase.client.java.env.CouchbaseEnvironment;

import io.gravitee.repository.Scope;
import io.gravitee.repository.couchbase.common.AbstractRepositoryConfiguration;
import io.gravitee.repository.couchbase.common.CouchbaseFactory;

@Configuration
@ComponentScan
@PropertySource("classpath:gravitee.yml")
@EnableCouchbaseRepositories
public class TestRepositoryConfiguration extends AbstractRepositoryConfiguration {

	
	
	@Override
	protected String getScope() {
		return Scope.MANAGEMENT.getName();
	}
	
	@Bean
	protected CouchbaseFactory couchbaseFactory(){
		return new CouchbaseFactory(getScope());
	}
	@Autowired
	private CouchbaseFactory cbFactory;
	
	@Bean
	  protected CouchbaseEnvironment getEnvironment() {
	    return cbFactory.builder()
	        .build();
	  }

}

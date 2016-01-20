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
package io.gravitee.repository.couchbase.management.internal.model;

import org.springframework.data.couchbase.core.mapping.Document;

import io.gravitee.repository.management.model.MembershipType;

/**
 * @author Ludovic Dussart
 */
public class MembershipCouchbase extends Auditable {

	private String user;

    private MembershipType type;

    public MembershipType getType() {
  		return type;
  	}

  	public void setType(MembershipType type) {
  		this.type = type;
  	}
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}

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
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import io.gravitee.repository.couchbase.management.internal.model.PageCouchbase;
import io.gravitee.repository.couchbase.management.internal.page.PageCouchbaseRepository;
import io.gravitee.repository.couchbase.management.mapper.GraviteeMapper;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.PageRepository;
import io.gravitee.repository.management.model.Page;

@Component
public class CouchbasePageRepository implements PageRepository {

	private final static Logger logger = LoggerFactory.getLogger(CouchbasePageRepository.class);

	@Autowired
	private PageCouchbaseRepository internalPageRepo;

	@Autowired
	private GraviteeMapper mapper;


	@Override
	public Collection<Page> findByApi(String apiId) throws TechnicalException {
		logger.debug("Find pages by api {}", apiId);

		List<PageCouchbase> pages = internalPageRepo.findByApi(apiId);
		Set<Page> res = mapper.collection2set(pages, PageCouchbase.class, Page.class);

		logger.debug("Find pages by api {} - Done", apiId);
		return res;
	}

	@Override
	public Optional<Page> findById(String pageId) throws TechnicalException {
		logger.debug("Find page by ID [{}]", pageId);

		PageCouchbase page = internalPageRepo.findOne(pageId);
		Page res = mapper.map(page, Page.class);

		logger.debug("Find page by ID [{}] - Done", pageId);
		return Optional.ofNullable(res);
	}

	@Override
	public Page create(Page page) throws TechnicalException {
		logger.debug("Create page [{}]", page.getName());
		
		PageCouchbase pageCb = mapper.map(page, PageCouchbase.class);
		PageCouchbase createdPageCb = internalPageRepo.save(pageCb);
		
		Page res = mapper.map(createdPageCb, Page.class);
		
		logger.debug("Create page [{}] - Done", page.getName());
		
		return res;
	}

	@Override
	public Page update(Page page) throws TechnicalException {
		if(page == null || page.getName() == null){
			throw new IllegalStateException("Page to update must have a name");
		}
		
		// Search team by name
		PageCouchbase pageCb = internalPageRepo.findOne(page.getId());
		
		if(pageCb == null){
			throw new IllegalStateException(String.format("No page found with name [%s]", page.getId()));
		}
		
		try{
			//Update
			pageCb.setName(page.getName());
			pageCb.setContent(page.getContent());
			pageCb.setLastContributor(page.getLastContributor());
			pageCb.setUpdatedAt(page.getUpdatedAt());
			pageCb.setOrder(page.getOrder());
			pageCb.setPublished(page.isPublished());
			
			PageCouchbase pageMongoUpdated = internalPageRepo.save(pageCb);
			return mapper.map(pageMongoUpdated, Page.class);

		}catch(Exception e){
			
			logger.error("An error occured when updating page",e);
			throw new TechnicalException("An error occured when updating page");
		}
	}

	@Override
	public void delete(String pageId) throws TechnicalException {
		try{
			internalPageRepo.delete(pageId);
		}catch(Exception e){
			logger.error("An error occured when deleting page [{}]", pageId, e);
			throw new TechnicalException("An error occured when deleting page");
		}
	}

	@Override
	public Integer findMaxPageOrderByApi(String apiId) throws TechnicalException {
		try{
			List<PageCouchbase> pages = internalPageRepo.findByApiOrderByOrderDesc(apiId);
			return CollectionUtils.isEmpty(pages) ? 0 : pages.get(0).getOrder();
		}catch(Exception e){
			logger.error("An error occured when searching max order page for api name [{}]", apiId, e);
			throw new TechnicalException("An error occured when searching max order page for api name");
		}
	}
}

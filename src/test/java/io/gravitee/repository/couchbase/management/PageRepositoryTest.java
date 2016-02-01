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
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.gravitee.repository.management.model.Page;
import io.gravitee.repository.management.model.PageType;

public class PageRepositoryTest extends AbstractCouchbaseDBTest {

    private final static Logger logger = LoggerFactory.getLogger(PageRepositoryTest.class);

    @Autowired
    private CouchbasePageRepository pageRepository;

    @Override
    protected String getTestCasesPath() {
        return "/data/page-tests/";
    }

    @Test
    public void createPageTest() {
        try {
        	Page page = new Page();
        	page.setApi("api1");
        	page.setContent("My doc content");
        	page.setCreatedAt(new Date());
        	page.setId("test-page");
        	page.setName("test-page");
        	page.setPublished(false);
        	page.setType(PageType.MARKDOWN);

        	Page pageCreated = pageRepository.create(page);

            Assert.assertEquals("Invalid saved page type.", "md", pageCreated.getType());
            
            Optional<Page> pageReturned = pageRepository.findById(pageCreated.getId());
            Assert.assertTrue("Page not found, actual", pageReturned.isPresent());
            
            Assert.assertEquals("Page name not equals ...", pageReturned.get().getName(), pageCreated.getName());
            
        } catch (Exception e) {
            logger.error("Error while calling createPageTest", e);
            Assert.fail("Error while calling createPageTest");
        }
    }

    @Test
    public void findByIdTest() {
        try {
            Optional<Page> page = pageRepository.findById("page1");
            Assert.assertTrue(page.isPresent());
            Assert.assertTrue("RAML".equals(page.get().getType()));
        } catch (Exception e) {
            logger.error("Error while finding event event1", e);
            Assert.fail("Error while finding event event1");
        }
    }

    @Test
    public void findByApi() {
        try {
            String apiId = "api1";
            Collection<Page> pages = pageRepository.findByApi(apiId);
            Assert.assertTrue(2 == pages.size());
            Optional<Page> page = pages.stream().sorted((e1, e2) -> e1.getCreatedAt().compareTo(e2.getCreatedAt())).findFirst();
            Assert.assertTrue(page.isPresent());
            Assert.assertTrue("page1".equals(page.get().getId()));
        } catch (Exception e) {
            logger.error("Error while finding event by api", e);
            Assert.fail("Error while finding event by api");
        }
    }
    
    @Test
    public void findPublishedByApi() {
        try {
            String apiId = "api1";
            Collection<Page> pages = pageRepository.findPublishedByApi(apiId);
            Assert.fail();
//            Assert.assertTrue(2 == pages.size());
//            Optional<Page> page = pages.stream().sorted((e1, e2) -> e1.getCreatedAt().compareTo(e2.getCreatedAt())).findFirst();
//            Assert.assertTrue(page.isPresent());
//            Assert.assertTrue("page1".equals(page.get().getId()));
        } catch (Exception e) {
            logger.error("Error while finding event by api", e);
            Assert.fail("Error while finding event by api");
        }
    }
    
    @Test
    public void findMaxPageOrderByApi() {
        try {
            String apiId = "api1";
           Integer maxOrder = pageRepository.findMaxPageOrderByApi(apiId);
            Assert.fail();
//            Assert.assertTrue(2 == pages.size());
//            Optional<Page> page = pages.stream().sorted((e1, e2) -> e1.getCreatedAt().compareTo(e2.getCreatedAt())).findFirst();
//            Assert.assertTrue(page.isPresent());
//            Assert.assertTrue("page1".equals(page.get().getId()));
        } catch (Exception e) {
            logger.error("Error while finding event by api", e);
            Assert.fail("Error while finding event by api");
        }
    }
    
}

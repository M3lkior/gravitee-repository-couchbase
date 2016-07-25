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
package io.gravitee.repository.couchbase.common;

import com.couchbase.client.java.env.CouchbaseEnvironment;
import io.gravitee.repository.Scope;
import io.gravitee.repository.couchbase.management.mapper.GraviteeDozerMapper;
import io.gravitee.repository.couchbase.management.mapper.GraviteeMapper;
import io.gravitee.repository.couchbase.management.transaction.NoTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.couchbase.config.AbstractCouchbaseConfiguration;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.query.Consistency;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ludovic DUSSART (ludovic dot dussart dot pro at gmail dot com)
 */
public abstract class AbstractRepositoryConfiguration extends AbstractCouchbaseConfiguration {

    private final Logger LOGGER = LoggerFactory.getLogger(AbstractRepositoryConfiguration.class);

    @Autowired
    private Environment environment;

    @Override
    protected CouchbaseEnvironment getEnvironment() {
        try {
            return new CouchbaseEnvironmentFactory(environment, Scope.MANAGEMENT.getName()).get().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    protected abstract String getScope();

    @Override
    protected List<String> getBootstrapHosts() {
        String hostsAsString = environment.getProperty(getScope() + ".couchbase.hosts", "gravitee");
        return Arrays.asList(StringUtils.commaDelimitedListToStringArray(hostsAsString));
    }

    @Override
    protected String getBucketName() {
        return environment.getProperty(getScope() + ".couchbase.bucketname", "gravitee");
    }

    @Override
    protected String getBucketPassword() {
        return environment.getProperty(getScope() + ".couchbase.bucketpassword", "");
    }

    @Override
    protected String getMappingBasePackage() {
        return getClass().getPackage().getName();
    }

    @Bean
    public GraviteeMapper graviteeMapper() {
        return new GraviteeDozerMapper();
    }

    @Override
    protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
        String basePackage = getMappingBasePackage();
        Set<Class<?>> initialEntitySet = new HashSet<Class<?>>();

        if (StringUtils.hasText(basePackage)) {
            ClassPathScanningCandidateComponentProvider componentProvider = new ClassPathScanningCandidateComponentProvider(
                    false);
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Document.class));
            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

            for (BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
                initialEntitySet.add(ClassUtils.forName(candidate.getBeanClassName(),
                        this.getClass().getClassLoader()));
            }
        }

        return initialEntitySet;
    }

    @Bean
    public AbstractPlatformTransactionManager graviteeTransactionManager() {
        return new NoTransactionManager();
    }

    @Override
    /**
     * Always return up-to-date datas
     */
    protected Consistency getDefaultConsistency() {
        return Consistency.STRONGLY_CONSISTENT;
    }
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.gravitee.repository.couchbase.management.internal.event.EventCouchbaseRepository;
import io.gravitee.repository.couchbase.management.internal.model.EventCouchbase;
import io.gravitee.repository.couchbase.management.mapper.GraviteeMapper;
import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.EventRepository;
import io.gravitee.repository.management.model.Event;
import io.gravitee.repository.management.model.EventType;

@Component
public class CouchbaseEventRepository implements EventRepository {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
	private GraviteeMapper mapper;
    @Autowired
    private EventCouchbaseRepository internalEventRepo;

    @Override
    public Optional<Event> findById(String id) throws TechnicalException {
        logger.debug("Find event by ID [{}]", id);

        EventCouchbase event = internalEventRepo.findOne(id);
        Event res = mapper.map(event, Event.class);

        logger.debug("Find event by ID [{}] - Done", id);
        return Optional.ofNullable(res);
    }

    @Override
    public Event create(Event event) throws TechnicalException {
        logger.debug("Create event [{}]", event.getId());

        EventCouchbase eventCb = mapper.map(event, EventCouchbase.class);
        if(StringUtils.isEmpty(eventCb.getId())){
        	eventCb.setId(internalEventRepo.getIdForEvent());
        }
        EventCouchbase createdEventMongo = internalEventRepo.save(eventCb);

        Event res = mapper.map(createdEventMongo, Event.class);

        logger.debug("Create event [{}] - Done", event.getId());

        return res;
    }

    @Override
    public Event update(Event event) throws TechnicalException {
        if (event == null || event.getId() == null) {
            throw new IllegalStateException("Event to update must have an id");
        }

        EventCouchbase eventCb = internalEventRepo.findOne(event.getId());
        if (eventCb == null) {
            throw new IllegalStateException(String.format("No event found with id [%s]", event.getId()));
        }

        try {
        	eventCb.setProperties(event.getProperties());
            eventCb.setType(event.getType());
            eventCb.setPayload(event.getPayload());
            eventCb.setParentId(event.getParentId());

            EventCouchbase eventMongoUpdated = internalEventRepo.save(eventCb);
            return mapper.map(eventMongoUpdated, Event.class);
        } catch (Exception e) {
            logger.error("An error occured when updating event", e);
            throw new TechnicalException("An error occured when updating event");
        }
    }

    @Override
    public void delete(String id) throws TechnicalException {
        try {
            internalEventRepo.delete(id);
        } catch (Exception e) {
            logger.error("An error occured when deleting event [{}]", id, e);
            throw new TechnicalException("An error occured when deleting event");
        }
    }

    @Override
    public Set<Event> findByType(List<EventType> eventTypes) {
        List<String> types = new ArrayList<String>();
        for (EventType eventType : eventTypes) {
            types.add(eventType.toString());
        }
        Collection<EventCouchbase> eventsCb = internalEventRepo.findByTypeIn(types);
        return mapper.collection2set(eventsCb, EventCouchbase.class, Event.class);
    }

    @Override
    public Set<Event> findByProperty(String key, String value) {
        Collection<EventCouchbase> eventsCb = internalEventRepo.findByProperty(key, value);

        return mapper.collection2set(eventsCb, EventCouchbase.class, Event.class);
    }

//    private Set<Event> mapEvents(Collection<EventCouchbase> events) {
//        return events.stream().map(this::mapEvent).collect(Collectors.toSet());
//    }

//    private EventCouchbase mapEvent(Event event) {
//    	EventCouchbase eventMongo = new EventCouchbase();
//        eventMongo.setId(event.getId());
//        eventMongo.setType(event.getType().toString());
//        eventMongo.setPayload(event.getPayload());
//        eventMongo.setParentId(event.getParentId());
//        eventMongo.setProperties(event.getProperties());
//        eventMongo.setCreatedAt(event.getCreatedAt());
//        eventMongo.setUpdatedAt(event.getUpdatedAt());
//
//        return eventMongo;
//    }
//
//    private Event mapEvent(EventCouchbase eventMongo) {
//        Event event = new Event();
//        event.setId(eventMongo.getId());
//        event.setType(EventType.valueOf(eventMongo.getType()));
//        event.setPayload(eventMongo.getPayload());
//        event.setParentId(eventMongo.getParentId());
//        event.setProperties(eventMongo.getProperties());
//        event.setCreatedAt(eventMongo.getCreatedAt());
//        event.setUpdatedAt(eventMongo.getUpdatedAt());
//
//        return event;
//    }

}

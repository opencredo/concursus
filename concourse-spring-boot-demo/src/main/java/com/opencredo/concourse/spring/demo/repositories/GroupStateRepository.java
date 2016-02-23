package com.opencredo.concourse.spring.demo.repositories;

import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.collecting.ProxyingEventStreamCollector;
import com.opencredo.concourse.spring.demo.events.GroupCreatedEvent;
import com.opencredo.concourse.spring.demo.events.GroupUpdatedEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class GroupStateRepository {

    private final EventSource eventSource;

    @Autowired
    public GroupStateRepository(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    public Optional<GroupState> getGroupState(UUID groupId) {
        ProxyingEventStreamCollector<GroupState, GroupCreatedEvent, GroupUpdatedEvents> collector = ProxyingEventStreamCollector.proxying(
                GroupState.class,
                GroupCreatedEvent.class,
                GroupUpdatedEvents.class);

        return collector.collect(eventSource, groupId,
                caller -> (ts, id, name) -> caller.accept(new GroupState(id, name)),
                GroupStateUpdater::new)
                .filter(g -> !g.isDeleted());
    }

    private static class GroupStateUpdater implements GroupUpdatedEvents {
        private final GroupState groupState;

        public GroupStateUpdater(GroupState groupState) {
            this.groupState = groupState;
        }

        @Override
        public void userAdded(StreamTimestamp ts, UUID groupId, UUID userId) {
            groupState.addUser(userId);
        }

        @Override
        public void userRemoved(StreamTimestamp ts, UUID groupId, UUID userId) {
            groupState.removeUser(userId);
        }

        @Override
        public void deleted(StreamTimestamp ts, UUID groupId) {
            groupState.delete();
        }

        @Override
        public void changedName(StreamTimestamp ts, UUID groupId, String newName) {
            groupState.setName(newName);
        }
    }
}

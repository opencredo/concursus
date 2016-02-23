package com.opencredo.concourse.spring.demo.repositories;

import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.events.methods.collecting.ProxyingEventStreamCollector;
import com.opencredo.concourse.spring.demo.events.UserCreatedEvent;
import com.opencredo.concourse.spring.demo.events.UserUpdatedEvents;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserStateRepository {

    private final EventSource eventSource;

    @Autowired
    public UserStateRepository(EventSource eventSource) {
        this.eventSource = eventSource;
    }

    public Optional<UserState> getUserState(UUID userId) {
        ProxyingEventStreamCollector<UserState, UserCreatedEvent, UserUpdatedEvents> collector = ProxyingEventStreamCollector.proxying(
                UserState.class,
                UserCreatedEvent.class,
                UserUpdatedEvents.class
        );

        return collector.collect(eventSource, userId,
                caller -> (ts, id, name, pwd) -> caller.accept(new UserState(id, name, pwd)),
                UserUpdater::new)
                .filter(userState -> !userState.isDeleted());
    }

    private static class UserUpdater implements UserUpdatedEvents {
        private final UserState userState;

        public UserUpdater(UserState userState) {
            this.userState = userState;
        }

        @Override
        public void changedName(StreamTimestamp ts, UUID userId, String newName) {
            userState.setName(newName);
        }

        @Override
        public void addedToGroup(StreamTimestamp ts, UUID userId, UUID groupId) {
            userState.addGroupId(groupId);
        }

        @Override
        public void removedFromGroup(StreamTimestamp ts, UUID userId, UUID groupId) {
            userState.removeGroupId(groupId);
        }

        @Override
        public void deleted(StreamTimestamp ts, UUID userId) {
            userState.delete();
        }

        @Override
        public void updatedPassword(StreamTimestamp ts, UUID userId, String newPasswordHash) {
            userState.setPasswordHash(newPasswordHash);
        }
    }
}

package com.opencredo.concourse.spring.demo.services;

import com.opencredo.concourse.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concourse.domain.events.sourcing.EventSource;
import com.opencredo.concourse.domain.events.views.EventView;
import com.opencredo.concourse.mapping.events.methods.history.MappingEventHistoryFetcher;
import com.opencredo.concourse.spring.demo.events.UserEvents;
import com.opencredo.concourse.spring.demo.repositories.GroupStateRepository;
import com.opencredo.concourse.spring.demo.repositories.UserStateRepository;
import com.opencredo.concourse.spring.demo.views.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class UserService {

    private final EventSource eventSource;
    private final UserStateRepository userStateRepository;
    private final GroupStateRepository groupStateRepository;
    private final AggregateCatalogue aggregateCatalogue;

    @Autowired
    public UserService(EventSource eventSource, UserStateRepository userStateRepository, GroupStateRepository groupStateRepository, AggregateCatalogue aggregateCatalogue) {
        this.eventSource = eventSource;
        this.userStateRepository = userStateRepository;
        this.groupStateRepository = groupStateRepository;
        this.aggregateCatalogue = aggregateCatalogue;
    }

    public List<EventView> getHistory(UUID userId) {
        return MappingEventHistoryFetcher.mapping(UserEvents.class).getHistory(eventSource, userId)
                .stream().map(EventView::of).collect(toList());
    }

    public boolean checkPassword(UUID userId, String passwordHash) {
        return userStateRepository.getUserState(userId).map(userState -> userState.getPasswordHash().equals(passwordHash)).orElse(false);
    }

    public Optional<UserView> getUser(UUID userId) {
        return userStateRepository.getUserState(userId).map(user ->
                new UserView(
                        user.getId(),
                        user.getName(),
                        getGroupNames(user.getGroupIds())));
    }

    private Map<UUID, String> getGroupNames(Collection<UUID> groupIds) {
        return groupStateRepository.getGroupStates(groupIds).entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                e -> e.getValue().getName()
        ));
    }

    public Map<String, String> getUsers() {
        List<UUID> users = aggregateCatalogue.getUuids("user");
        return userStateRepository.getUserStates(users).entrySet().stream().collect(
                Collectors.toMap(
                        e -> e.getValue().getName(),
                        e -> "/api/v1/acl/users/" + e.getKey())
                );
    }
}

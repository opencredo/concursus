package com.opencredo.concursus.spring.demo.services;

import com.opencredo.concursus.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concursus.domain.events.sourcing.EventSource;
import com.opencredo.concursus.domain.events.views.EventView;
import com.opencredo.concursus.domain.events.state.StateRepository;
import com.opencredo.concursus.mapping.events.methods.history.MappingEventHistoryFetcher;
import com.opencredo.concursus.spring.demo.events.UserEvents;
import com.opencredo.concursus.spring.demo.repositories.GroupState;
import com.opencredo.concursus.spring.demo.repositories.UserState;
import com.opencredo.concursus.spring.demo.views.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Component
public class UserService {

    private final EventSource eventSource;
    private final StateRepository<UserState> userStateRepository;
    private final StateRepository<GroupState> groupStateRepository;
    private final AggregateCatalogue aggregateCatalogue;

    @Autowired
    public UserService(EventSource eventSource, StateRepository<UserState> userStateRepository, StateRepository<GroupState> groupStateRepository, AggregateCatalogue aggregateCatalogue) {
        this.eventSource = eventSource;
        this.userStateRepository = userStateRepository;
        this.groupStateRepository = groupStateRepository;
        this.aggregateCatalogue = aggregateCatalogue;
    }

    public List<EventView> getHistory(String userId) {
        return MappingEventHistoryFetcher.mapping(UserEvents.class).getHistory(eventSource, userId)
                .stream().map(EventView::of).collect(toList());
    }

    public boolean checkPassword(String userId, String passwordHash) {
        return userStateRepository.getState(userId)
                .filter(s -> !s.isDeleted())
                .map(userState -> userState.getPasswordHash().equals(passwordHash)).orElse(false);
    }

    public Optional<UserView> getUser(String userId) {
        return userStateRepository.getState(userId)
                .filter(s -> !s.isDeleted())
                .map(user ->
                new UserView(
                        user.getId(),
                        user.getName(),
                        getGroupNames(user.getGroupIds())));
    }

    private Map<String, String> getGroupNames(Collection<String> groupIds) {
        return groupStateRepository.getStates(groupIds).entrySet().stream()
                .filter(e -> !e.getValue().isDeleted())
                .collect(Collectors.toMap(
                Entry::getKey,
                e -> e.getValue().getName()
        ));
    }

    public Map<String, String> getUsers() {
        List<String> users = aggregateCatalogue.getAggregateIds("user");
        return userStateRepository.getStates(users).entrySet().stream()
                .filter(e -> !e.getValue().isDeleted())
                .collect(
                Collectors.toMap(
                        e -> e.getValue().getName(),
                        e -> "/api/v1/acl/users/" + e.getKey())
                );
    }
}

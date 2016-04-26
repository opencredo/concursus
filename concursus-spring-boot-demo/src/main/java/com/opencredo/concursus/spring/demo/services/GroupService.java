package com.opencredo.concursus.spring.demo.services;

import com.opencredo.concursus.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concursus.domain.events.state.StateRepository;
import com.opencredo.concursus.spring.demo.repositories.GroupState;
import com.opencredo.concursus.spring.demo.repositories.UserState;
import com.opencredo.concursus.spring.demo.views.GroupView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Component
public class GroupService {

    private final StateRepository<GroupState> groupStateRepository;
    private final StateRepository<UserState> userStateRepository;
    private final AggregateCatalogue aggregateCatalogue;

    @Autowired
    public GroupService(StateRepository<GroupState> groupStateRepository, StateRepository<UserState> userStateRepository, AggregateCatalogue aggregateCatalogue) {
        this.groupStateRepository = groupStateRepository;
        this.userStateRepository = userStateRepository;
        this.aggregateCatalogue = aggregateCatalogue;
    }

    public Optional<GroupView> getGroup(String groupId) {
        return groupStateRepository.getState(groupId)
                .filter(s -> !s.isDeleted())
                .map(group -> new GroupView(group.getId(), group.getName(), getUserNames(group.getUsers())));
    }

    private Map<String, String> getUserNames(Collection<String> userIds) {
        return userStateRepository.getStates(userIds).entrySet().stream()
                .filter(e -> !e.getValue().isDeleted())
                .collect(Collectors.toMap(
                Entry::getKey,
                e -> e.getValue().getName()
        ));
    }

    public Map<String, String> getGroups() {
        List<String> groups = aggregateCatalogue.getAggregateIds("group");
        return groupStateRepository.getStates(groups).entrySet().stream()
                .filter(e -> !e.getValue().isDeleted())
                .collect(
                Collectors.toMap(
                        e -> e.getValue().getName(),
                        e -> "/api/v1/acl/groups/" + e.getKey())
        );
    }

}

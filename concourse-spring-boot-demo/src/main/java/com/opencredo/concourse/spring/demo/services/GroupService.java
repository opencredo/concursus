package com.opencredo.concourse.spring.demo.services;

import com.opencredo.concourse.domain.events.cataloguing.AggregateCatalogue;
import com.opencredo.concourse.spring.demo.repositories.GroupStateRepository;
import com.opencredo.concourse.spring.demo.repositories.UserStateRepository;
import com.opencredo.concourse.spring.demo.views.GroupView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

@Component
public class GroupService {

    private final GroupStateRepository groupStateRepository;
    private final UserStateRepository userStateRepository;
    private final AggregateCatalogue aggregateCatalogue;

    @Autowired
    public GroupService(GroupStateRepository groupStateRepository, UserStateRepository userStateRepository, AggregateCatalogue aggregateCatalogue) {
        this.groupStateRepository = groupStateRepository;
        this.userStateRepository = userStateRepository;
        this.aggregateCatalogue = aggregateCatalogue;
    }

    public Optional<GroupView> getGroup(UUID groupId) {
        return groupStateRepository.getGroupState(groupId).map(group -> new GroupView(group.getId(), group.getName(), getUserNames(group.getUsers())));
    }

    private Map<UUID, String> getUserNames(Collection<UUID> userIds) {
        return userStateRepository.getUserStates(userIds).entrySet().stream().collect(Collectors.toMap(
                Entry::getKey,
                e -> e.getValue().getName()
        ));
    }

    public Map<String, String> getGroups() {
        List<UUID> groups = aggregateCatalogue.getUuids("group");
        return groupStateRepository.getGroupStates(groups).entrySet().stream().collect(
                Collectors.toMap(
                        e -> e.getValue().getName(),
                        e -> "/api/v1/acl/groups/" + e.getKey())
        );
    }

}

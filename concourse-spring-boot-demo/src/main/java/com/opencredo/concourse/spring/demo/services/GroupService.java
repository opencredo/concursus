package com.opencredo.concourse.spring.demo.services;

import com.opencredo.concourse.spring.demo.repositories.GroupStateRepository;
import com.opencredo.concourse.spring.demo.repositories.UserStateRepository;
import com.opencredo.concourse.spring.demo.views.GroupView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GroupService {

    private final GroupStateRepository groupStateRepository;
    private final UserStateRepository userStateRepository;

    @Autowired
    public GroupService(GroupStateRepository groupStateRepository, UserStateRepository userStateRepository) {
        this.groupStateRepository = groupStateRepository;
        this.userStateRepository = userStateRepository;
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

}

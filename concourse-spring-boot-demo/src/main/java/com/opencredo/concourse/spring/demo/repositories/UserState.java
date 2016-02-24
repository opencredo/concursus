package com.opencredo.concourse.spring.demo.repositories;

import com.opencredo.concourse.mapping.annotations.HandlesEvent;
import com.opencredo.concourse.mapping.annotations.HandlesEventsFor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@HandlesEventsFor("user")
public final class UserState {

    @HandlesEvent
    public static UserState created(UUID id, String userName, String passwordHash) {
        return new UserState(id, userName, passwordHash);
    }

    private final UUID id;
    private String name;
    private String passwordHash;
    private final Set<UUID> groupIds = new HashSet<>();
    private boolean isDeleted = false;

    public UserState(UUID id, String name, String passwordHash) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    @HandlesEvent
    public void addedToGroup(UUID groupId) {
        groupIds.add(groupId);
    }

    @HandlesEvent
    public void removedFromGroup(UUID groupId) {
       groupIds.remove(groupId);
    }

    @HandlesEvent
    public void nameChanged(String newName) {
        this.name = name;
    }

    @HandlesEvent
    public void deleted() {
        isDeleted = true;
    }

    public Set<UUID> getGroupIds() {
        return groupIds;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @HandlesEvent
    public void passwordHashUpdated(String newPasswordHash) {
        passwordHash = newPasswordHash;
    }
}

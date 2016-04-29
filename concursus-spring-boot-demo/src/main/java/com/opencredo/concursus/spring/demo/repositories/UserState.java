package com.opencredo.concursus.spring.demo.repositories;

import com.opencredo.concursus.mapping.annotations.HandlesEvent;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;

import java.util.HashSet;
import java.util.Set;

@HandlesEventsFor("user")
public final class UserState {

    @HandlesEvent
    public static UserState created(String id, String userName, String passwordHash) {
        return new UserState(id, userName, passwordHash);
    }

    private final String id;
    private String name;
    private String passwordHash;
    private final Set<String> groupIds = new HashSet<>();
    private boolean isDeleted = false;

    public UserState(String id, String name, String passwordHash) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    @HandlesEvent
    public void addedToGroup(String groupId) {
        groupIds.add(groupId);
    }

    @HandlesEvent
    public void removedFromGroup(String groupId) {
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

    public Set<String> getGroupIds() {
        return groupIds;
    }

    public String getId() {
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

package com.opencredo.concourse.spring.demo.services;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class UserState {

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

    public void addGroupId(UUID groupId) {
        groupIds.add(groupId);
    }

    public void removeGroupId(UUID groupId) {
       groupIds.remove(groupId);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void delete() {
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

    public void setPasswordHash(String newPasswordHash) {
        passwordHash = newPasswordHash;
    }
}

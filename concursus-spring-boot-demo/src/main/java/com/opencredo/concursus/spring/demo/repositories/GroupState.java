package com.opencredo.concursus.spring.demo.repositories;

import com.opencredo.concursus.mapping.annotations.HandlesEvent;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@HandlesEventsFor("group")
public final class GroupState {

    @HandlesEvent
    public static GroupState created(UUID id, String groupName) {
        return new GroupState(id, groupName);
    }

    private final UUID id;
    private String name;
    private final Set<UUID> users = new HashSet<>();
    private boolean deleted = false;

    public GroupState(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<UUID> getUsers() {
        return users;
    }

    @HandlesEvent
    public void nameChanged(String newName) {
        this.name = newName;
    }

    @HandlesEvent
    public void userAdded(UUID userId) {
        users.add(userId);
    }

    @HandlesEvent
    public void deleted() {
        deleted = true;
    }

    @HandlesEvent
    public void userRemoved(UUID userId) {
        users.remove(userId);
    }

    public boolean isDeleted() {
        return deleted;
    }
}

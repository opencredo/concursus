package com.opencredo.concursus.spring.demo.repositories;

import com.opencredo.concursus.mapping.annotations.HandlesEvent;
import com.opencredo.concursus.mapping.annotations.HandlesEventsFor;

import java.util.HashSet;
import java.util.Set;

@HandlesEventsFor("group")
public final class GroupState {

    @HandlesEvent
    public static GroupState created(String id, String groupName) {
        return new GroupState(id, groupName);
    }

    private final String id;
    private String name;
    private final Set<String> users = new HashSet<>();
    private boolean deleted = false;

    public GroupState(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<String> getUsers() {
        return users;
    }

    @HandlesEvent
    public void nameChanged(String newName) {
        this.name = newName;
    }

    @HandlesEvent
    public void userAdded(String userId) {
        users.add(userId);
    }

    @HandlesEvent
    public void deleted() {
        deleted = true;
    }

    @HandlesEvent
    public void userRemoved(String userId) {
        users.remove(userId);
    }

    public boolean isDeleted() {
        return deleted;
    }
}

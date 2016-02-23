package com.opencredo.concourse.spring.demo.repositories;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class GroupState {

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

    public void setName(String name) {
        this.name = name;
    }

    public Set<UUID> getUsers() {
        return users;
    }

    public void addUser(UUID uuid) {
        users.add(uuid);
    }

    public void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    public void delete() {
        deleted = true;
    }

    public boolean isDeleted() {
        return deleted;
    }
}

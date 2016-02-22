package com.opencredo.concourse.spring.demo.views;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public final class UserView {

    @JsonProperty
    private final UUID id;

    @JsonProperty
    private final String name;

    @JsonProperty
    private final Map<UUID, String> groups;

    public UserView(UUID id, String name, Map<UUID, String> groups) {
        this.id = id;
        this.name = name;
        this.groups = groups;
    }

    public UserView withNewName(String newName) {
        return new UserView(id, newName, groups);
    }
}

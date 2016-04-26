package com.opencredo.concursus.spring.demo.views;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public final class UserView {

    @JsonProperty
    private final String id;

    @JsonProperty
    private final String name;

    @JsonProperty
    private final Map<String, String> groups;

    public UserView(String id, String name, Map<String, String> groups) {
        this.id = id;
        this.name = name;
        this.groups = groups;
    }

    public UserView withNewName(String newName) {
        return new UserView(id, newName, groups);
    }
}

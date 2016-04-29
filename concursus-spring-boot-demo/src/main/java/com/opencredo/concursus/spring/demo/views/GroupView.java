package com.opencredo.concursus.spring.demo.views;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public final class GroupView {

    @JsonProperty
    private final String id;

    @JsonProperty
    private final String groupName;

    @JsonProperty
    private final Map<String, String> users;

    public GroupView(String id, String groupName, Map<String, String> users) {
        this.id = id;
        this.groupName = groupName;
        this.users = users;
    }
}

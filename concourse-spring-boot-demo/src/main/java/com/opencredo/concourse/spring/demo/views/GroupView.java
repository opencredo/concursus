package com.opencredo.concourse.spring.demo.views;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.UUID;

public final class GroupView {

    @JsonProperty
    private final UUID id;

    @JsonProperty
    private final String groupName;

    @JsonProperty
    private final Map<UUID, String> users;

    public GroupView(UUID id, String groupName, Map<UUID, String> users) {
        this.id = id;
        this.groupName = groupName;
        this.users = users;
    }
}

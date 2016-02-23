package com.opencredo.concourse.spring.demo.views;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class CreateUserRequest {

    private final String name;
    private final String password;

    @JsonCreator
    public static CreateUserRequest of(
            @JsonProperty("name") String name,
            @JsonProperty("password") String password) {
        return new CreateUserRequest(name, password);
    }

    private CreateUserRequest(String name, String password) {
        this.name = name;
        this.password = password;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }
}

package com.opencredo.concourse.spring.demo.views;

import com.fasterxml.jackson.annotation.JsonCreator;

public final class CreateUserRequest {

    private final String name;
    private final String password;

    @JsonCreator
    public CreateUserRequest(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}

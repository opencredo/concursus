package com.opencredo.concourse.spring.demo.controllers;

import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.commands.methods.proxying.ProxyingCommandBus;
import com.opencredo.concourse.spring.demo.commands.UserCommands;
import com.opencredo.concourse.spring.demo.services.UserService;
import com.opencredo.concourse.spring.demo.views.CreateUserRequest;
import com.opencredo.concourse.spring.demo.views.UserView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/api/v1/acl")
public class UserController {

    private final UserCommands userCommands;
    private final UserService userService;

    public UserController(ProxyingCommandBus commandBus, UserService userService) {
        userCommands = commandBus.getDispatcherFor(UserCommands.class);
        this.userService = userService;
    }

    @RequestMapping(path = "users/{userId}", method = RequestMethod.GET)
    public UserView getUser(@PathVariable("userId") UUID userId) {
        return userService.getUser(userId).orElseThrow(UserNotFoundException::new);
    }

    @RequestMapping(path = "users", method = RequestMethod.POST)
    public CompletableFuture<ResponseEntity<?>> createUser(@RequestBody CreateUserRequest createUserRequest) throws NoSuchAlgorithmException {
        UUID id  = UUID.randomUUID();
        return userCommands.create(
                StreamTimestamp.of("admin", Instant.now()),
                id,
                createUserRequest.getName(),
                MessageDigest.getInstance("MD5").digest(createUserRequest.getPassword().getBytes()))
                .thenApply(userId -> ResponseEntity.created(URI.create("/users/" + userId)).build());
    }

    @RequestMapping(path = "users/{userId}/name", method = RequestMethod.POST)
    public CompletableFuture<Void> updateUsername(@PathVariable("userId") UUID userId,
                                   @RequestBody String name) {
        return userCommands.updateName(
                StreamTimestamp.of("admin", Instant.now()),
                userId,
                name);
    }

}

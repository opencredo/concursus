package com.opencredo.concursus.spring.demo.controllers;

import com.opencredo.concursus.domain.events.views.EventView;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.commands.methods.proxying.CommandProxyFactory;
import com.opencredo.concursus.spring.demo.commands.UserCommands;
import com.opencredo.concursus.spring.demo.services.UserService;
import com.opencredo.concursus.spring.demo.views.CreateUserRequest;
import com.opencredo.concursus.spring.demo.views.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/acl/users")
public class UserController {

    private final UserCommands userCommands;
    private final UserService userService;

    @Autowired
    public UserController(CommandProxyFactory commandBus, UserService userService) {
        userCommands = commandBus.getProxy(UserCommands.class);
        this.userService = userService;
    }

    @RequestMapping(path = "", method = RequestMethod.GET)
    public Map<String, String> getUsers() {
        return userService.getUsers();
    }

    @RequestMapping(path = "{userId}", method = RequestMethod.GET)
    public UserView getUser(@PathVariable("userId") String userId) {
        return userService.getUser(userId).orElseThrow(UserNotFoundException::new);
    }

    @RequestMapping(path = "{userId}/history", method = RequestMethod.GET)
    public List<EventView> getHistory(@PathVariable("userId") String userId) {
        return userService.getHistory(userId);
    }

    @RequestMapping(path = "", method = RequestMethod.POST)
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest createUserRequest) throws NoSuchAlgorithmException {
        String id  = UUID.randomUUID().toString();

        userCommands.create(
                StreamTimestamp.of("admin", Instant.now()),
                id,
                createUserRequest.getName(),
                MessageDigest.getInstance("MD5").digest(createUserRequest.getPassword().getBytes()));

        return ResponseEntity.created(URI.create("/users/" + id)).build();
    }

    @RequestMapping(path = "{userId}/name", method = RequestMethod.POST)
    public void updateUsername(@PathVariable("userId") String userId,
                                   @RequestBody String name) {
        userCommands.updateName(
                StreamTimestamp.of("admin", Instant.now()),
                userId,
                name);
    }

    @RequestMapping(path = "{userId}/groups", method = RequestMethod.POST)
    public void addUserToGroup(@PathVariable("userId") String userId,
                                                  @RequestBody String groupId) {
        userCommands.addToGroup(
                StreamTimestamp.of("admin", Instant.now()),
                userId,
                groupId);
    }

    @RequestMapping(path = "{userId}/groups/{groupId}", method = RequestMethod.DELETE)
    public void removeUserFromGroup(@PathVariable("userId") String userId,
                                                  @PathVariable("groupId") String groupId) {
        userCommands.removeFromGroup(
                StreamTimestamp.of("admin", Instant.now()),
                userId,
                groupId);
    }

    @RequestMapping(path = "{userId}", method = RequestMethod.DELETE)
    public void deleteUser(@PathVariable("userId") String userId) {
        userCommands.delete(
                StreamTimestamp.of("admin", Instant.now()),
                userId);
    }

}

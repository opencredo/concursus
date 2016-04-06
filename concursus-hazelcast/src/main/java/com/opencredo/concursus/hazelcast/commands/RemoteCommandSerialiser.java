package com.opencredo.concursus.hazelcast.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import com.opencredo.concursus.domain.commands.CommandTypeMatcher;
import com.opencredo.concursus.domain.json.commands.CommandJson;

import java.io.IOException;

final class RemoteCommandSerialiser implements StreamSerializer<RemoteCommand> {

    static RemoteCommandSerialiser using(ObjectMapper objectMapper, CommandTypeMatcher commandTypeMatcher) {
        return new RemoteCommandSerialiser(objectMapper, commandTypeMatcher);
    }

    private final ObjectMapper objectMapper;
    private final CommandTypeMatcher commandTypeMatcher;

    private RemoteCommandSerialiser(ObjectMapper objectMapper, CommandTypeMatcher commandTypeMatcher) {
        this.objectMapper = objectMapper;
        this.commandTypeMatcher = commandTypeMatcher;
    }

    @Override
    public void write(ObjectDataOutput objectDataOutput, RemoteCommand command) throws IOException {
        objectDataOutput.writeUTF(CommandJson.toString(command.getCommand(), objectMapper));
    }

    @Override
    public RemoteCommand read(ObjectDataInput objectDataInput) throws IOException {
        String commandJson = objectDataInput.readUTF();
        return CommandJson.fromString(commandJson, commandTypeMatcher, objectMapper)
                .map(RemoteCommand::processing)
                .orElseThrow(() -> new IllegalArgumentException("No command type matcher found for command " + commandJson));
    }

    @Override
    public int getTypeId() {
        return 100;
    }

    @Override
    public void destroy() {

    }
}

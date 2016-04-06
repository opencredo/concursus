package com.opencredo.concursus.hazelcast.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ManagedContext;
import com.opencredo.concursus.domain.commands.CommandType;
import com.opencredo.concursus.domain.commands.CommandTypeInfo;
import com.opencredo.concursus.domain.commands.CommandTypeMatcher;
import com.opencredo.concursus.domain.commands.dispatching.CommandExecutor;
import com.opencredo.concursus.mapping.commands.methods.dispatching.CommandHandlerSubscribable;
import com.opencredo.concursus.mapping.commands.methods.dispatching.MethodDispatchingCommandProcessor;
import com.opencredo.concursus.mapping.commands.methods.reflection.CommandInterfaceInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to configure a {@link HazelcastInstance} with a suitable {@link com.hazelcast.core.IExecutorService} for
 * distributed execution of commands, and a custom {@link com.hazelcast.nio.serialization.Serializer} that can
 * serialise and deserialise command requests for execution on remote nodes, to support the use of the
 * {@link HazelcastCommandExecutor}.
 */
public final class HazelcastCommandExecutorConfiguration implements CommandHandlerSubscribable {

    /**
     * Create a {@link HazelcastCommandExecutorConfiguration} using the supplied {@link ObjectMapper} for JSON
     * serialisation, with a default executor name "concursus" and default one-thread-per-node configuration.
     * @param objectMapper the {@link ObjectMapper} to use for JSON serialisation.
     * @return The constructed {@link HazelcastCommandExecutorConfiguration}.
     */
    public static HazelcastCommandExecutorConfiguration using(ObjectMapper objectMapper) {
        return using(objectMapper, "concursus", 1);
    }

    /**
     * Create a {@link HazelcastCommandExecutorConfiguration} using the supplied {@link ObjectMapper} for JSON
     * serialisation, with the supplied executor name and threads-per-node configuration.
     * @param objectMapper the {@link ObjectMapper} to use for JSON serialisation.
     * @param executorName The name to use for the {@link com.hazelcast.core.IExecutorService}.
     * @param threadsPerNode The number of threads each node provides to the {@link com.hazelcast.core.IExecutorService}.
     * @return The constructed {@link HazelcastCommandExecutorConfiguration}.
     */
    public static HazelcastCommandExecutorConfiguration using(ObjectMapper objectMapper, String executorName, int threadsPerNode) {
        return new HazelcastCommandExecutorConfiguration(objectMapper, executorName, threadsPerNode);
    }

    private final ObjectMapper objectMapper;
    private final String executorName;
    private final int threadsPerNode;
    private final Map<CommandType, CommandTypeInfo> typeInfoMap = new HashMap<>();
    private final MethodDispatchingCommandProcessor dispatchingCommandProcessor =
            MethodDispatchingCommandProcessor.create();

    private HazelcastCommandExecutorConfiguration(ObjectMapper objectMapper, String executorName, int threadsPerNode) {
        this.objectMapper = objectMapper;
        this.executorName = executorName;
        this.threadsPerNode = threadsPerNode;
    }

    /**
     * Add configuration to the supplied {@link Config} to support the use of a {@link HazelcastCommandExecutor}.
     * @param config The {@link Config} to configure.
     * @return The updated {@link Config}.
     */
    public Config addCommandExecutorConfiguration(Config config) {
        SerializerConfig serializerConfig = new SerializerConfig()
                .setImplementation(RemoteCommandSerialiser.using(
                        objectMapper,
                        CommandTypeMatcher.matchingAgainst(typeInfoMap)))
                .setTypeClass(RemoteCommand.class);

        ManagedContext managedContext = CommandProcessingManagedContext
                .processingCommandsWith(dispatchingCommandProcessor);

        config.getSerializationConfig().addSerializerConfig(serializerConfig);

        config.setManagedContext(config.getManagedContext() == null
                ? managedContext
                : CompositeManagedContext.of(managedContext, config.getManagedContext()));

        config.addExecutorConfig(new ExecutorConfig(executorName, threadsPerNode));
        return config;
    }

    /**
     * Create a {@link CommandExecutor} which dispatches commands to Hazelcast nodes for execution.
     * @param hazelcastInstance The {@link HazelcastInstance} to obtain
     * @return
     */
    public CommandExecutor getCommandExecutor(HazelcastInstance hazelcastInstance) {
        return HazelcastCommandExecutor.using(hazelcastInstance.getExecutorService(executorName));
    }

    @Override
    public <H> CommandHandlerSubscribable subscribe(Class<? extends H> handlerClass, H commandHandler) {
        typeInfoMap.putAll(CommandInterfaceInfo.forInterface(handlerClass).getTypeInfoMap());
        dispatchingCommandProcessor.subscribe(handlerClass, commandHandler);
        return this;
    }
}

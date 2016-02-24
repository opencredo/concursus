package com.opencredo.concourse.mapping.commands.methods.reflection;

import com.opencredo.concourse.data.tuples.*;
import com.opencredo.concourse.domain.commands.Command;
import com.opencredo.concourse.domain.commands.CommandType;
import com.opencredo.concourse.domain.common.AggregateId;
import com.opencredo.concourse.domain.common.VersionedName;
import com.opencredo.concourse.domain.time.StreamTimestamp;
import com.opencredo.concourse.mapping.reflection.ParameterArgs;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class CommandMethodMapping {

    public static CommandMethodMapping forMethod(Method method) {
        checkNotNull(method, "method must not be null");

        Class<?> klass = method.getDeclaringClass();

        final String aggregateType = CommandInterfaceReflection.getAggregateType(klass);
        final VersionedName commandName = CommandInterfaceReflection.getCommandName(method);

        ParameterArgs parameterArgs = ParameterArgs.forMethod(method, 2);
        TupleSchema schema = parameterArgs.getTupleSchema(CommandType.of(aggregateType, commandName).toString());
        TupleKey[] tupleKeys = parameterArgs.getTupleKeys(schema);

        Type returnType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];

        return new CommandMethodMapping(
                aggregateType,
                commandName,
                schema,
                tupleKeys,
                returnType);
    }

    private final String aggregateType;
    private final VersionedName commandName;
    private final TupleSchema tupleSchema;
    private final TupleKey[] tupleKeys;
    private final Type resultType;

    private CommandMethodMapping(String aggregateType, VersionedName commandName, TupleSchema tupleSchema, TupleKey[] tupleKeys, Type resultType) {
        this.aggregateType = aggregateType;
        this.commandName = commandName;
        this.tupleSchema = tupleSchema;
        this.tupleKeys = tupleKeys;
        this.resultType = resultType;
    }

    public Command mapArguments(Object[] args) {
        checkNotNull(args, "args must not be null");
        checkArgument(args.length == tupleKeys.length + 2,
                "Expected %s args, received %s", tupleKeys.length +2, args.length);

        return Command.of(
                AggregateId.of(aggregateType, (UUID) args[1]),
                (StreamTimestamp) args[0],
                commandName,
                makeTupleFromArgs(args),
                resultType
        );
    }

    public CommandType getCommandType() {
        return CommandType.of(aggregateType, commandName);
    }

    private Tuple makeTupleFromArgs(Object[] args) {
        return tupleSchema.make(IntStream.range(0, tupleKeys.length)
                .mapToObj(getValueFrom(args))
                .toArray(TupleKeyValue[]::new));
    }

    public Object[] mapCommand(Command command) {
        checkNotNull(command, "command must not be null");

        Object[] args = new Object[tupleKeys.length + 2];
        args[0] = command.getCommandTimestamp();
        args[1] = command.getAggregateId().getId();

        populateArgsFromTuple(command, args);

        return args;
    }

    private void populateArgsFromTuple(Command command, Object[] args) {
        IntStream.range(0, tupleKeys.length).forEach(i ->
            args[i + 2] = command.getParameters().get(tupleKeys[i]));
    }

    @SuppressWarnings("unchecked")
    private IntFunction<TupleKeyValue> getValueFrom(Object[] args) {
        return i -> tupleKeys[i].of(args[i + 2]);
    }
}

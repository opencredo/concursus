package com.opencredo.concursus.mapping.commands.methods.reflection;

import com.opencredo.concursus.data.tuples.Tuple;
import com.opencredo.concursus.data.tuples.TupleKey;
import com.opencredo.concursus.data.tuples.TupleKeyValue;
import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.domain.commands.Command;
import com.opencredo.concursus.domain.commands.CommandType;
import com.opencredo.concursus.domain.commands.CommandTypeInfo;
import com.opencredo.concursus.domain.common.AggregateId;
import com.opencredo.concursus.domain.common.VersionedName;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import com.opencredo.concursus.mapping.annotations.Name;
import com.opencredo.concursus.mapping.reflection.ParameterArgs;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class CommandMethodMapping {

    public static CommandMethodMapping forMethod(Method method, String aggregateType) {
        checkNotNull(method, "method must not be null");

        final VersionedName commandName = getCommandName(method);

        ParameterArgs parameterArgs = ParameterArgs.forMethod(method, 2);
        TupleSchema schema = parameterArgs.getTupleSchema(CommandType.of(aggregateType, commandName).toString());
        TupleKey[] tupleKeys = parameterArgs.getTupleKeys(schema);

        return new CommandMethodMapping(
                aggregateType,
                commandName,
                schema,
                tupleKeys,
                method.getGenericReturnType());
    }

    private static VersionedName getCommandName(Method method) {
        if (method.isAnnotationPresent(Name.class)) {
            Name name = method.getAnnotation(Name.class);
            return VersionedName.of(name.value(), name.version());
        }
        return VersionedName.of(method.getName(), "0");
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
        checkArgument(args[0] instanceof StreamTimestamp, "first argument %s is not a StreamTimestamp", args[0]);
        checkArgument(args[1] instanceof String, "second argument %s is not a String", args[0]);

        return Command.of(
                AggregateId.of(aggregateType, (String) args[1]),
                (StreamTimestamp) args[0],
                commandName,
                makeTupleFromArgs(args),
                resultType
        );
    }

    public CommandType getCommandType() {
        return CommandType.of(aggregateType, commandName);
    }

    public CommandTypeInfo getCommandTypeInfo() {
        return CommandTypeInfo.of(tupleSchema, resultType);
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

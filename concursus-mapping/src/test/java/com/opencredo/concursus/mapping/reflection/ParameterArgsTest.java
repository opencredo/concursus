package com.opencredo.concursus.mapping.reflection;

import com.opencredo.concursus.data.tuples.TupleKey;
import com.opencredo.concursus.data.tuples.TupleSchema;
import com.opencredo.concursus.domain.time.StreamTimestamp;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public class ParameterArgsTest {

    public interface TestInterface {
        void methodWithArgsToSkip(StreamTimestamp ts, UUID aggregateId, String a, Integer b, Optional<Long> c);
        void methodWithArgsInNonAlphabeticalOrder(String b, Integer a, Optional<Long> c);
        void methodWithArgsInAlphabeticalOrder(Integer a, String b, Optional<Long> c);
    }

    @Test
    public void skipsUnwantedArgs() throws NoSuchMethodException {
        Method methodWithArgsToSkip = TestInterface.class.getMethod("methodWithArgsToSkip", StreamTimestamp.class, UUID.class, String.class, Integer.class, Optional.class);
        ParameterArgs parameterArgs = ParameterArgs.forMethod(methodWithArgsToSkip, 2);
        TupleSchema schema = parameterArgs.getTupleSchema("schema");

        assertThat(schema.getName(), equalTo("schema"));
        assertThat(tupleKeyNames(parameterArgs), contains("a", "b", "c"));
    }

    @Test
    public void schemaArgsAreInAlphabeticalOrder()  throws NoSuchMethodException {
        Method methodWithArgsInNonAlphabeticalOrder = TestInterface.class.getMethod("methodWithArgsInNonAlphabeticalOrder", String.class, Integer.class, Optional.class);
        ParameterArgs unorderedParameterArgs = ParameterArgs.forMethod(methodWithArgsInNonAlphabeticalOrder, 0);
        TupleSchema unorderedSchema = unorderedParameterArgs.getTupleSchema("schema");

        Method methodWithArgsInAlphabeticalOrder = TestInterface.class.getMethod("methodWithArgsInAlphabeticalOrder", Integer.class, String.class, Optional.class);
        ParameterArgs orderedParameterArgs = ParameterArgs.forMethod(methodWithArgsInAlphabeticalOrder, 0);
        TupleSchema orderedSchema = unorderedParameterArgs.getTupleSchema("schema");

        assertThat(unorderedSchema, equalTo(orderedSchema));
        assertThat(tupleKeyNames(unorderedParameterArgs),
                contains("b", "a", "c"));
        assertThat(tupleKeyNames(orderedParameterArgs),
                contains("a", "b", "c"));
    }

    private List<String> tupleKeyNames(ParameterArgs parameterArgs) {
        TupleSchema schema = parameterArgs.getTupleSchema("test");
        return Stream.of(parameterArgs.getTupleKeys(schema)).map(TupleKey::getName).collect(Collectors.toList());
    }
}

package com.opencredo.concourse.domain.events;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Accumulator<I, O> extends Consumer<I>, Supplier<O> {

}

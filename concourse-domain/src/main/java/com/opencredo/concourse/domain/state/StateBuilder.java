package com.opencredo.concourse.domain.state;

import com.opencredo.concourse.domain.events.channels.EventOutChannel;

import java.util.Optional;
import java.util.function.Supplier;

public interface StateBuilder<T> extends EventOutChannel, Supplier<Optional<T>> {
}

package com.opencredo.concourse.domain.events.storing;

import com.opencredo.concourse.domain.events.consuming.EventLog;
import com.opencredo.concourse.domain.events.sourcing.PreloadableEventSource;

public interface EventStore extends EventLog, PreloadableEventSource {
}

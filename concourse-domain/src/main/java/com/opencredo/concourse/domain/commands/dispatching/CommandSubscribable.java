package com.opencredo.concourse.domain.commands.dispatching;

import com.opencredo.concourse.domain.commands.CommandType;

public interface CommandSubscribable {

    CommandSubscribable subscribe(CommandType commandType, CommandProcessor commandProcessor);

}

package com.opencredo.concursus.domain.commands.dispatching;

import com.opencredo.concursus.domain.commands.CommandType;

public interface CommandSubscribable {

    CommandSubscribable subscribe(CommandType commandType, CommandProcessor commandProcessor);

}

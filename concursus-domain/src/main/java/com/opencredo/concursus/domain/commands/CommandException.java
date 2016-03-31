package com.opencredo.concursus.domain.commands;

public class CommandException extends RuntimeException {
    public CommandException(Throwable cause) {
        super(cause);
    }
}

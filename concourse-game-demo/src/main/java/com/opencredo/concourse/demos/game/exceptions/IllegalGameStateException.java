package com.opencredo.concourse.demos.game.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IllegalGameStateException extends RuntimeException {

    public IllegalGameStateException(String message) {
        super(message);
    }
}

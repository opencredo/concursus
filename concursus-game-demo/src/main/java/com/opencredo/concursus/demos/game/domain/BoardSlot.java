package com.opencredo.concursus.demos.game.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class BoardSlot {

    @JsonCreator
    public static BoardSlot of(int index, BoardRow row) {
        return new BoardSlot(index, row);
    }

    private final int index;
    private final BoardRow row;

    private BoardSlot(int index, BoardRow row) {
        this.index = index;
        this.row = row;
    }

    @JsonProperty
    public int getIndex() {
        return index;
    }

    @JsonProperty
    public BoardRow getRow() {
        return row;
    }

    @Override
    public String toString() {
        return row + ": " + index;
    }
}

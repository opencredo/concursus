package com.opencredo.concursus.domain.events.sealing;

public final class SealedEventData {

    private final String dataType;
    private final byte[] data;

    public SealedEventData(String dataType, byte[] data) {
        this.dataType = dataType;
        this.data = data;
    }

    public String getDataType() {
        return dataType;
    }

    public byte[] getData() {
        return data;
    }
}

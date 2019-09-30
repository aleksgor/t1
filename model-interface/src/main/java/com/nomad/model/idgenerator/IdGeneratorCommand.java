package com.nomad.model.idgenerator;

public enum IdGeneratorCommand {

    GET_NEXT_ID(1), GET_STATUS(3);

    private int code;

    private IdGeneratorCommand(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

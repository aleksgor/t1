package com.nomad.communication;

public interface ServerService<CommonMessage, CommonAnswer> {
    void start() throws Exception;

    void stop() throws Exception;

}

package com.nomad.serializer;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;

public interface Serializer<T> {

    void write(MessageOutputStream out, T data) throws IOException, SystemException;

    T read(MessageInputStream input) throws IOException, SystemException;

}

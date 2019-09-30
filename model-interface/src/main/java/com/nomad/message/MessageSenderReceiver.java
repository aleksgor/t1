package com.nomad.message;

import java.io.InputStream;
import java.io.OutputStream;

import com.nomad.exception.SystemException;


public interface MessageSenderReceiver {


    void assembleMessageHeader(MessageHeader header, OutputStream output) throws SystemException ;
    void assembleResult(Result result, OutputStream output) throws SystemException ;
    MessageHeader getMessageHeader(InputStream is) throws SystemException ;
    Result getResult(InputStream is) throws SystemException ;

    RawMessage getRawMessage(InputStream is) throws SystemException ;
    void assembleRawMessage(RawMessage message, OutputStream output) throws SystemException ;

    Body getBody(InputStream is ) throws SystemException;
    void assembleBody(Body body, OutputStream output) throws SystemException ;

    byte[] getByteFromBody(Body message) throws SystemException;
    Body getBodyFromByte(byte[] data) throws SystemException;

    Object getData(InputStream is) throws SystemException ;
    byte[] getEmptyBody() throws  SystemException;

    void storeObject (Object object, OutputStream output )throws SystemException;
    Object getObject (InputStream is)throws SystemException;
    byte getMessageVersion() ;

    long getInBytes() ;
    long getOutBytes() ;
    void reset();

}

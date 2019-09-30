package com.nomad.model;

import com.nomad.exception.SystemException;


public interface TransactInvoker {
    String startTransaction() throws SystemException;

    void rollBack(String transactionId) throws SystemException;

    void commit(String transactionId) throws SystemException;
}

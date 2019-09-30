package com.nomad.exception;

import java.util.Arrays;
import java.util.Collection;

import com.nomad.model.Identifier;

public class BlockException extends LogicalException {


    public BlockException(String code, Collection<? extends Identifier> blockedSessions) {
        super(code,blockedSessions.toArray());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Collection<Identifier> getBlockedSessions() {
        Collection result=Arrays.asList(getArgs());
        return ( Collection<Identifier>) result;
    }

}

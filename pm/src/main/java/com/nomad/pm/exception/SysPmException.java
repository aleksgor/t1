package com.nomad.pm.exception;

import com.nomad.exception.SystemException;


public class SysPmException extends SystemException
{
   

	public SysPmException()
    {
    }

    public SysPmException(String message)
    {
        super(message);
    }

    public SysPmException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

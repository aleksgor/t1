package com.nomad.cache.commonclientserver;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.message.OperationStatus;
import com.nomad.serializer.Serializer;

public class ResultSerializer implements Serializer<ResultImpl> {

    @Override
    public void write(MessageOutputStream out, ResultImpl data) throws IOException, SystemException {

        out.writeString(data.getErrorCode());
        out.writeString(data.getMessage());
        OperationStatus status = data.getOperationStatus();
        String result = null;
        if (status != null) {
            result = status.toString();
        }
        out.writeString(result);
        out.writeList(data.getArguments());
    }

    @Override
    public ResultImpl read(MessageInputStream input) throws IOException, SystemException {
        ResultImpl result = new ResultImpl();
        result.setErrorCode(input.readString());
        result.setMessage(input.readString());

        String res = input.readString();

        if (res != null) {
            try {
                result.setStatus(OperationStatus.valueOf(res));
            } catch (IllegalArgumentException e) {

            }
        }
         input.readList(result.getArguments());
        return result;
    }

}

package com.nomad.cache.commonclientserver.update;

import java.io.IOException;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.update.UpdateItem.Operation;
import com.nomad.model.update.UpdateItemImpl;
import com.nomad.serializer.Serializer;

public class UpdateItemSerializer implements Serializer<UpdateItemImpl> {

    @Override
    public void write(final MessageOutputStream out, final UpdateItemImpl data) throws IOException, SystemException {
        out.writeString(data.getFieldName());
        String sOperation=null;
        if(data.getOperation()!=null){
            sOperation=data.getOperation().name();
        }
        out.writeString(sOperation);
        out.writeString(data.getValue());
        
    }

    @Override
    public UpdateItemImpl read(final MessageInputStream input) throws IOException, SystemException {
        String fieldName= input.readString();
        String data=input.readString();
        Operation operation= data==null? null: Operation.valueOf(data);
        data= input.readString();
        
        return new UpdateItemImpl(fieldName,data,operation);
    }

}

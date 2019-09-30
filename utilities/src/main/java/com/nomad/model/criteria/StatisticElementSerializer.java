package com.nomad.model.criteria;

import java.io.IOException;
import java.util.Collection;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class StatisticElementSerializer implements Serializer<StatisticElementImpl> {

    @Override
    public void write(MessageOutputStream out, StatisticElementImpl data) throws IOException, SystemException {

        out.writeString(data.getDescription());
        out.writeString(data.getFieldName());
        out.writeString(data.getModelName());
        if (data.getFunction() == null) {
            out.writeString(null);
        } else {
            out.writeString(data.getFunction().name());
        }
        out.writeObject(data.getValue());
        out.writeList(data.getChildren());
        out.writeBoolean(data.isFieldOnly());

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public StatisticElementImpl read(MessageInputStream input) throws IOException, SystemException {
        StatisticElementImpl result = new StatisticElementImpl();
        result.setDescription(input.readString());
        result.setFieldName(input.readString());
        result.setModelName(input.readString());
        String function = input.readString();
        if (function != null) {
            result.setFunction(AggregateFunction.valueOf(function));
        }
        result.setValue(input.readObject());
        input.readList((Collection) result.getChildren());
        result.setFieldOnly(input.readBoolean());
        return result;
    }

}

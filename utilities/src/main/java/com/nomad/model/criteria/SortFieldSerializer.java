package com.nomad.model.criteria;

import java.io.IOException;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.SortField.Order;
import com.nomad.serializer.Serializer;

public class SortFieldSerializer  implements Serializer<SortFieldImpl>{

    @Override
    public void write(MessageOutputStream out, SortFieldImpl data) throws IOException {
        out.writeString(data.getFieldName());
        out.writeString(data.getOrder().toString());
        out.writeString(data.getRelationName());

    }

    @Override
    public SortFieldImpl read(MessageInputStream input) throws IOException {
        SortFieldImpl result = new SortFieldImpl();
        result.setFieldName(input.readString());
        result.setOrder(Order.valueOf(input.readString()));
        result.setRelationName(input.readString());
        return result;
    }

}

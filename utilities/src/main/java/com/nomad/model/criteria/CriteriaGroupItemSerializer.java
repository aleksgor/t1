package com.nomad.model.criteria;

import java.io.IOException;
import java.util.List;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class CriteriaGroupItemSerializer implements Serializer<CriteriaGroupItem>{

    @Override
    public void write(MessageOutputStream out, CriteriaGroupItem data) throws IOException, SystemException {
        out.writeList(data.getCriteria());


    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public CriteriaGroupItem read(MessageInputStream input) throws IOException, SystemException {
        CriteriaGroupItem result = new CriteriaGroupItem();
        result.getCriteria().addAll((List)input.readList());
        return result;
    }

}

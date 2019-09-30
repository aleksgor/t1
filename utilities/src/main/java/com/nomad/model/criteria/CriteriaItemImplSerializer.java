package com.nomad.model.criteria;

import java.io.IOException;
import java.util.List;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.Criteria;
import com.nomad.model.Criteria.Case;
import com.nomad.model.Criteria.Concatenation;
import com.nomad.model.Criteria.Condition;
import com.nomad.model.Model;
import com.nomad.model.RequestType;
import com.nomad.serializer.Serializer;


public class CriteriaItemImplSerializer implements Serializer<CriteriaItemImpl>{

    @Override
    public void write(MessageOutputStream out, CriteriaItemImpl data) throws IOException, SystemException {

        out.writeString(data.getFieldName());
        out.writeObject(data.getFieldValue());
        out.writeString(data.getFieldType().name());
        out.writeString(data.getCondition().name());
        out.writeString(data.getModelName());
        out.writeString(data.getCaseValue().name());
        out.writeList(data.getCriteria());
        String string = null;
        if(data.getConcatenation()!=null){
            string = data.getConcatenation().name();
        }
        out.writeString(string);
        out.writeObject(data.getCriteriaForIn());


    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public CriteriaItemImpl read(MessageInputStream input) throws IOException, SystemException {
        CriteriaItemImpl result= new CriteriaItemImpl();
        result.setFieldName(input.readString());
        result.setFieldValue(input.readObject());
        result.setFieldType(RequestType.valueOf(input.readString()));
        result.setCondition(Condition.valueOf(input.readString()));
        result.setModelName(input.readString());
        result.setCaseValue(Case.valueOf(input.readString()));
        result.setCriteria((List)input.readList());
        String string = input.readString();
        if (string != null) {
            result.setConcatenation(Concatenation.valueOf(string));
        }
        result.setCriteriaForIn((Criteria<? extends Model>) input.readObject());

        return result;
    }

}

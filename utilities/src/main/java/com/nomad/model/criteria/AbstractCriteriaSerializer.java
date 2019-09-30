package com.nomad.model.criteria;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

@SuppressWarnings("rawtypes")
public class AbstractCriteriaSerializer implements Serializer<AbstractCriteria>{

    @SuppressWarnings("unchecked")
    @Override
    public void write(MessageOutputStream out, AbstractCriteria data) throws IOException, SystemException {

        out.writeString(data.getClass().getName());
        out.writeList(new ArrayList<String>(data.getRelationsLoad()));
        out.writeList(data.getCriteria());
        out.writeList(data.getOrder());
        out.writeBoolean(data.isCalculateCount());
        out.writeLong(data.getStartPosition());
        out.writeInteger(data.getPageSize());
        out.writeObject(data.getResult());
        out.writeBoolean(data.isBinaryLoad());
        out.writeBoolean(data.isDistinct());
        out.writeBoolean(data.isStatisticOnly());
        out.writeList(data.getStatistics());
        out.writeList(data.getGroups());

    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractCriteria read(MessageInputStream input) throws IOException, SystemException {

        try {
            String className=input.readString();
            Class<AbstractCriteria<?>> criteriaResult = (Class<AbstractCriteria<?>>) Class.forName(className);
            AbstractCriteria<?> result = criteriaResult.newInstance();

            result.getRelationsLoad().addAll((List)input.readList());
            result.getCriteria().addAll((List)input.readList());
            result.getOrder().addAll((List)input.readList());
            result.setCalculateCount(input.readBoolean());
            result.setStartPosition(input.readLong());
            result.setPageSize(input.readInteger());
            result.setResult((StatisticResult) input.readObject());
            result.setBinaryLoad(input.readBoolean());
            result.setDistinct(input.readBoolean());
            result.setStatisticOnly(input.readBoolean());
            input.readList((Collection) result.getStatistics());
            input.readList((Collection) result.getGroups());

            return result;

        } catch (Exception e) {
            throw new SystemException(e.getMessage(),e);
        }

    }

}

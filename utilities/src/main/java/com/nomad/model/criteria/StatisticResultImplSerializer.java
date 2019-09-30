package com.nomad.model.criteria;

import java.io.IOException;
import java.util.List;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class StatisticResultImplSerializer implements Serializer<StatisticResultImpl> {

    @Override
    public void write(MessageOutputStream out, StatisticResultImpl data) throws IOException, SystemException {

        out.writeLong(data.getCountAllRow());
        out.writeInteger(data.getPageSize());
        out.writeList(data.getResultList());
        out.writeLong(data.getStartPosition());
        out.writeList(data.getIdentifiers());
        out.writeList(data.getStatistics());
        out.writeList(data.getGroups());
    }

    @Override
    public StatisticResultImpl read(MessageInputStream input) throws IOException, SystemException {
        StatisticResultImpl<?> result = new StatisticResultImpl();
        result.setCountAllRow(input.readLong());
        result.setPageSize(input.readInteger());
        result.setResultList((List) input.readList());
        result.setStartPosition(input.readLong());
        result.setIdentifiers((List) input.readList());
        result.setStatistics((List<StatisticElement>) input.readList());
        result.setGroups((List<StatisticElement>) input.readList());
        return result;
    }

}

package com.nomad.server.statistic;

import com.nomad.exception.SystemException;
import com.nomad.server.ServiceInterface;

public interface InformationPublisherService extends ServiceInterface {

    void publicData(final Object bean, final String serverName, final String type, final String name);

    Object getData(final String serverName, final String type, final String name) throws SystemException;

}

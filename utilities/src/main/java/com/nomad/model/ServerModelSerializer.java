package com.nomad.model;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.model.command.CommandServerModel;
import com.nomad.model.idgenerator.IdGeneratorServerModel;
import com.nomad.model.management.ManagementServerModel;
import com.nomad.model.session.SessionServerModel;
import com.nomad.serializer.Serializer;

public class ServerModelSerializer implements Serializer<ServerModelImpl> {

    @Override
    public void write(final MessageOutputStream out, final ServerModelImpl data) throws IOException, SystemException {

        out.writeObject(data.getCommandServerModel());
        out.writeObject(data.getManagementServerModel());
        out.writeString(data.getPluginPath());
        out.writeList(data.getSerializers());
        out.writeObject(data.getSessionCallBackServerModel());

        out.writeList(data.getListeners());
        out.writeList(data.getCommandPlugins());
        out.writeList(data.getStoreModels());
        out.writeList(data.getServers());
        out.writeList(data.getClients());
        out.writeString(data.getServerName());

        final Map<Object, Object> map = new HashMap<>(data.getProperties());
        out.writeMap(map);
        out.writeList(data.getDataSources());
        out.writeObject(data.getSessionServerModel());
        out.writeList(data.getSessionClientModels());
        out.writeList(data.getSaveServerModels());
        out.writeList(data.getSaveClientModels());
        out.writeBoolean(data.isLocalSessions());
        out.writeObject(data.getIdGeneratorServerModel());
        out.writeList(data.getIdGeneratorClientModels());

    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ServerModelImpl read(final MessageInputStream input) throws IOException, SystemException {
        final ServerModelImpl result = new ServerModelImpl();

        result.setCommandServerModel((CommandServerModel) input.readObject());
        result.setManagementServerModel((ManagementServerModel) input.readObject());
        result.setPluginPath(input.readString());
        result.getSerializers().addAll((Collection) input.readList());
        result.setSessionCallBackServerModel((SessionCallBackServerModel) input.readObject());

        result.getListeners().addAll((Collection) input.readList());
        result.getCommandPlugins().addAll((Collection) input.readList());
        result.getStoreModels().addAll((Collection) input.readList());
        result.getServers().addAll((Collection) input.readList());
        result.getClients().addAll((Collection) input.readList());
        result.setServerName(input.readString());
        final Map<Object, Object> map = (Map<Object, Object>) input.readMap();
        result.getProperties().putAll(map);
        final Collection<?> l = input.readList();
        if (l != null) {
            for (final Object object : l) {
                result.addDataSources((DataSourceModel) object);
            }
        }

        result.setSessionServerModel((SessionServerModel) input.readObject());
        result.getSessionClientModels().addAll((Collection) input.readList());
        result.getSaveServerModels().addAll((List) input.readList());
        result.getSaveClientModels().addAll((List) input.readList());
        result.setLocalSessions(input.readBoolean());
        result.setIdGeneratorServerModel((IdGeneratorServerModel) input.readObject());
        input.readList((Collection) result.getIdGeneratorClientModels());

        return result;
    }

}

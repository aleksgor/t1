package com.nomad.server.commands;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.InternalTransactDataStore;
import com.nomad.exception.SystemException;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ServerModel;
import com.nomad.model.StoreModel;

public abstract class CommonCommand implements Command {
    protected static Logger LOGGER = LoggerFactory.getLogger(CommonCommand.class);

    protected ServerModel server;
    protected InternalTransactDataStore store;

    public CommonCommand(final InternalTransactDataStore store, final ServerModel server) {
        this.server = server;
        this.store = store;
    }

    protected Identifier getId(final String modelName, final List<String[]> values) {
        Identifier id = null;
        try {

            final List<StoreModel> models = server.getStoreModels();
            for (final StoreModel storeData : models) {
                if (modelName.equals(storeData.getModel())) {
                    Class<?> clazz = getIdentifierClass(storeData.getClazz());
                    Identifier result = (Identifier) clazz.newInstance();
                    for (final String[] objects : values) {
                        final String methodName = objects[0];
                        final String value = objects[1];
                        new PropertyDescriptor(methodName, clazz).getWriteMethod().invoke(result, value);
                    }
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return id;

    }

    @SuppressWarnings("unchecked")
    public Class<Identifier> getIdentifierClass(String modelClassName) throws SystemException {
        if (modelClassName != null) {
            try {
                Class<Model> modelClass = (Class<Model>) Class.forName(modelClassName);
                Method method = modelClass.getMethod("getIdentifier");
                return (Class<Identifier>) method.getReturnType();
            } catch (NoSuchMethodException | ClassNotFoundException e) {
                throw new SystemException(e);
            }
        }

        return null;
    }

    protected String getFullCommand(final String[] input) {
        String result = "";
        for (final String string : input) {
            result += string + " ";
        }
        return result;
    }
}

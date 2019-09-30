package com.nomad.saver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.SystemException;
import com.nomad.model.CacheMatcherModelImpl;
import com.nomad.model.CommandPluginModelImpl;
import com.nomad.model.CommonClientModelImpl;
import com.nomad.model.CommonServerModelImpl;
import com.nomad.model.ConnectModelImpl;
import com.nomad.model.DataSourceModelImpl;
import com.nomad.model.ListenerModelImpl;
import com.nomad.model.ServerModel;
import com.nomad.model.ServerModelImpl;
import com.nomad.model.StoreModelImpl;
import com.nomad.model.idgenerator.IdGeneratorClientModelImpl;
import com.nomad.model.idgenerator.IdGeneratorServerModelImpl;

public class LoadConfiguration {
    protected static Logger LOGGER = LoggerFactory.getLogger(LoadConfiguration.class);

    public ServerModel load(final File file) throws SystemException {
        try {

            final JAXBContext jaxbContext = JAXBContext.newInstance(ServerModelImpl.class, StoreModelImpl.class, CacheMatcherModelImpl.class,
                    ListenerModelImpl.class, CommandPluginModelImpl.class, ConnectModelImpl.class, CommonClientModelImpl.class, CommonServerModelImpl.class,DataSourceModelImpl.class);

            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final ServerModel result = (ServerModel) unmarshaller.unmarshal(file);
            return result;
        } catch (final JAXBException e) {
            throw new SystemException(e.getMessage(),e);
        }
    }

    public void save(final File file, ServerModel server) throws SystemException {
        try {

            final JAXBContext jaxbContext = JAXBContext.newInstance(ServerModelImpl.class, StoreModelImpl.class, CacheMatcherModelImpl.class, ListenerModelImpl.class,
                    CommandPluginModelImpl.class, ConnectModelImpl.class, CommonClientModelImpl.class, CommonServerModelImpl.class, DataSourceModelImpl.class,
                    IdGeneratorClientModelImpl.class, IdGeneratorServerModelImpl.class);

            final Marshaller marshaller = jaxbContext.createMarshaller();
            // file.deleteOnExit();
            OutputStream io = new FileOutputStream(file);
            marshaller.marshal(server, io);
            io.flush();

        } catch (IOException | JAXBException e) {
            throw new SystemException(e.getMessage(),e);
        }
    }

    public ServerModel load(final InputStream input) throws SystemException {
        try {

            final JAXBContext jaxbContext = JAXBContext.newInstance(ServerModelImpl.class, StoreModelImpl.class, CacheMatcherModelImpl.class,
                    ListenerModelImpl.class, CommandPluginModelImpl.class, ConnectModelImpl.class, CommonClientModelImpl.class, CommonServerModelImpl.class,DataSourceModelImpl.class);

            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            final ServerModel result = (ServerModel) unmarshaller.unmarshal(input);
            return result;
        } catch (final JAXBException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

}

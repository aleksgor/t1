package com.nomad.cache.test;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.StartFormXml;
import com.nomad.cache.commonclientserver.BodyImpl;
import com.nomad.cache.commonclientserver.BodyImplSerializer;
import com.nomad.io.serializer.SerializerFactory;

public class LongStart {
    protected static Logger LOGGER = LoggerFactory.getLogger(LongStart.class);

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final LongStart starter= new LongStart();
        try {
            starter.start();
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(),e);
        }
    }


    public void start() throws Exception{

        SerializerFactory.registerSerializer(BodyImpl.class, BodyImplSerializer.class);
        final String[] files = { "configuration/translator.xml", "configuration/cacheManager.xml", "configuration/cache1.xml", "configuration/cache2.xml",
                "configuration/cache3.xml" };
        final StartFormXml  starter = new StartFormXml();

        starter.startServers(files);
    }
}

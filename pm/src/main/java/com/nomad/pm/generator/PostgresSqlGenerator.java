package com.nomad.pm.generator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.server.DataDefinitionService;


public class PostgresSqlGenerator extends CommonSqlGenerator {
    public PostgresSqlGenerator(DataDefinitionService dataDefinitionService) {
        super(dataDefinitionService, false);
    }

    protected static Logger LOGGER = LoggerFactory.getLogger(PostgresSqlGenerator.class);


}

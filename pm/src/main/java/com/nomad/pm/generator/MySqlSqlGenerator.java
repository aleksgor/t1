package com.nomad.pm.generator;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.Model;
import com.nomad.server.DataDefinitionService;


public class MySqlSqlGenerator<T extends Model> extends CommonSqlGenerator {
    public MySqlSqlGenerator(DataDefinitionService dataDefinitionService, boolean noAlias) {
        super(dataDefinitionService,noAlias);
    }

    protected static Logger LOGGER = LoggerFactory.getLogger(MySqlSqlGenerator.class);


}

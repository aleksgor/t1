package com.nomad.pm.generator;



public class MSSQLGenerator extends Sql92Generator {

    public MSSQLGenerator() {
    }

    @Override
    protected String getRowSql(String sql, long startPosition, long countRow) {
        return sql;

    }

    @Override
    protected boolean supportLimit() {
        return false;
    }

}
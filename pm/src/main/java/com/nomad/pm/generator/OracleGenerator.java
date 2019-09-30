package com.nomad.pm.generator;


public class OracleGenerator extends Sql92Generator {

    public OracleGenerator() {
    }

    @Override
    protected String getRowSql(String sql, long startPosition, long countRow) {
        return " select b.* from (select a.*,rownum AS AG$CR  from (" + sql + ") a ) b WHERE b.AG$CR>=" + startPosition + " AND b.AG$CR<" + (startPosition + countRow);
    }

    @Override
    protected boolean supportLimit() {
        return true;
    }

}

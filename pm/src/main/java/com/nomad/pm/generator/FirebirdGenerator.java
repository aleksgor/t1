package com.nomad.pm.generator;


/**
 * @author alexgor Date: 04.02.2005 Time: 10:54:58
 */
public class FirebirdGenerator extends Sql92Generator {

    public FirebirdGenerator() {
        super();
    }


    @Override
    protected String getRowSql(String sql, final long startPosition, final long countRow) {

        return sql += " skip " + countRow + " first " + (startPosition - 1);

    }

    @Override
    protected boolean supportLimit() {
        return true;
    }

}

package com.nomad.pm.generator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.nomad.model.Model;
import com.nomad.model.criteria.AbstractCriteria;
import com.nomad.pm.exception.SysPmException;

/**
 * @author alexgor Date: 04.02.2005 Time: 10:51:45
 */
public class MysqlGenerator extends Sql92Generator {

    public MysqlGenerator() {
        super();
    }

    @Override
    protected String getRowSql(String sql, long startPosition, long countRow) {
        return sql += " LIMIT " + countRow + " OFFSET " + (startPosition - 1);
    }

    @Override
    protected boolean supportLimit() {
        return true;
    }


    @Override
    protected SqlGenerator getSqlGenerator(boolean noAlias) {
        return new CommonSqlGenerator(dataDefinitionService, noAlias);
    }

    @Override
    protected long getCountSelect(final AbstractCriteria<? extends Model> criteria, final SqlGenerator plan) throws SysPmException {
        long result = 0;
        final String sql = "SELECT COUNT(*) FROM (SELECT " + getDistinct(criteria) + plan.getSqlSelect() + " FROM " + plan.getSqlFrom() + plan.getSqlWhere() + ")  as counter ";

        LOGGER.debug("sql count getList:{}", sql);
        ResultSet resultSet = null;
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            plan.setStatements(statement);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getLong(1);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(),e);
            throw new SysPmException("Error in generator getList :" + sql + ":" + e.toString());
        } finally {
            close(resultSet);
        }
        return result;

    }

}

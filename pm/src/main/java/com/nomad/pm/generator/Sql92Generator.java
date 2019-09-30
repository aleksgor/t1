package com.nomad.pm.generator;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.datadefinition.DataDefinitionServiceImpl;
import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.model.Criteria;
import com.nomad.model.Field;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.ModelDescription;
import com.nomad.model.criteria.AbstractCriteria;
import com.nomad.model.criteria.StatisticElement;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.model.criteria.StatisticResultImpl;
import com.nomad.pm.exception.SysPmException;
import com.nomad.pm.local.JDBConnection;
import com.nomad.server.ConnectionPool;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.PmDataInvoker;
import com.nomad.server.ServerContext;

public class Sql92Generator implements PmDataInvoker, Serializable {

    protected static Logger LOGGER = LoggerFactory.getLogger(PmDataInvoker.class);
    protected static String prefix = "pm_";
    @SuppressWarnings("unused")
    private final Map<String, String> getSelect = new ConcurrentHashMap<String, String>();
    @SuppressWarnings("unused")
    private final Map<String, SqlGenerator> getSelectPlan = new ConcurrentHashMap<String, SqlGenerator>();
    @SuppressWarnings("unused")
    private final Map<String, String> eraseSelect = new ConcurrentHashMap<String, String>();

    protected DataDefinitionService dataDefinitionService;

    protected Hashtable<String, String> properties = null;

    private volatile ConnectionPool<JDBConnection> connectPool;
    private int pageSize = 50;

    final static Object data = new Object();

    private final static String SELECT_KEY_SQL = "SELECT KEYVALUE FROM SY_KEYS WHERE TABLENAME=?";
    private final static String UPDATE_KEY_SQL = "UPDATE SY_KEYS SET KEYVALUE=KEYVALUE+? WHERE TABLENAME=?";

    public void setExternalProperties(final Hashtable<String, String> parameters) {
        properties = parameters;
    }

    public Hashtable<String, String> getExternalProperties() {
        return properties;
    }

    public Sql92Generator() {

    }

    @Override
    public void init(final Properties properties, final ServerContext context, String connectName) throws SystemException {
        final String fileConfigurationName = properties.getProperty("fileConfiguration", "pm.cfg.xml");
        if (context != null) {
            dataDefinitionService = context.getDataDefinitionService(connectName, fileConfigurationName);
        } else {
            dataDefinitionService = new DataDefinitionServiceImpl(connectName, fileConfigurationName, null);
            dataDefinitionService.start();
        }
    }

    @Override
    public DataDefinitionService getDataDefinitionService() {

        return dataDefinitionService;
    }

    @Override
    public int eraseModel(final Criteria<? extends Model> criteria) throws SysPmException {

        LOGGER.debug("eraseModel Criteria:{}", criteria);
        int result = 0;
        final SqlGenerator plan = getSqlGenerator(true);
        plan.setCriteria(criteria);
        final String sql = "DELETE FROM " + plan.getSqlFrom() + plan.getSqlWhere();

        LOGGER.debug("sql delete :{}", sql);
        try (JDBConnection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            plan.setStatements(statement);
            result = statement.executeUpdate();
        } catch (final SQLException e) {
            LOGGER.error("Error in generator getList :" + sql + ":" + e.toString(), e);
            throw new SysPmException("Error in eraseModel by criteria :" + sql + ":" + e.toString()+" criteria:"+criteria);
        }

        LOGGER.debug("sql delete return :{}", result);
        return result;

    }

    @Override
    public Collection<Model> updateModel(final Collection<Model> models) throws SystemException {
        LOGGER.debug("updateModel Models:{}", models);
        if (models == null || models.size() == 0) {
            return Collections.emptyList();
        }
        Collection<Model> result = new ArrayList<>();
        final SqlGenerator plan = getSqlGenerator(false);
        plan.updateModel(models.iterator().next());

        final String sql = "UPDATE " + plan.getSqlFrom() + " SET " + plan.getSqlSelect() + plan.getSqlWhere();

        LOGGER.debug("sql update :{}", sql);
        try (JDBConnection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            for (Model model : models) {
                plan.setValues(model, statement);
                statement.executeUpdate();
                createIdentifierFromModel(model);
                result.add(model);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new SysPmException("Error in generator update :" + sql + ":" + e.toString(), e);
        }

        LOGGER.debug("updateModel return Model:{}", result);

        return result;
    }

    @Override
    public <T extends Model> StatisticResult<T> getIds(final Criteria<T> criteria) throws SystemException {
        StatisticResult<T> result = new StatisticResultImpl<>();
        LOGGER.debug("getList Criteria:{}", criteria);
        final AbstractCriteria<T> commonCriteria = (AbstractCriteria<T>) criteria;

        final SqlGenerator plan = getSqlGenerator(false);
        plan.setCriteriaForId(commonCriteria);
        if (plan.isError()) {
            throw new SysPmException(plan.getErrorMessage());
        }
        String sql = "SELECT " + getDistinct(commonCriteria) + "" + plan.getSqlSelect() + " FROM " + plan.getSqlFrom() + plan.getSqlWhere()
                + plan.getSqlOrder(); // TOD
        // ORDER
        long countAllRow = 0;
        long startPosition = commonCriteria.getStartPosition();
        final long countRow = commonCriteria.getPageSize();
        if (supportLimit()) {
            if (commonCriteria.getPageSize() > 0) {
                countAllRow = getCountSelect(commonCriteria, plan);
                if (startPosition > (countAllRow - countRow)) {
                    startPosition = countAllRow - countRow + 1;
                }
                if (startPosition < 1) {
                    startPosition = 1;
                }
                result.setStartPosition(startPosition);
                result.setCountAllRow(countAllRow);
                sql = getRowSql(sql, startPosition, countRow);
            }
        }

        LOGGER.debug("sql getList:{}", sql);
        ResultSet resultSet = null;
        result.setIdentifiers(new ArrayList<Identifier>());
        try (JDBConnection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {

            plan.setStatements(statement);
            resultSet = statement.executeQuery();

            int current = 0;
            while (resultSet.next()) {
                if (supportLimit() || criteria.getPageSize() <= 0) {
                    final Identifier identifier = plan.getIdentifierFromResultSet(resultSet);
                    result.getIdentifiers().add(identifier);
                } else {
                    if (current >= (criteria.getStartPosition() - 1) && (current < (criteria.getStartPosition() - 1 + criteria.getPageSize()))) {
                        final Identifier identifier = plan.getIdentifierFromResultSet(resultSet);
                        result.getIdentifiers().add(identifier);
                    }
                }
                current++;
            }

            if (!supportLimit() || criteria.getPageSize() <= 0) {
                result.setCountAllRow(current);
            }

        } catch (final Throwable e) {
            String message = "Error in generator getList :" + sql + " :" + e.toString() +" criteria:"+criteria;
            LOGGER.error(message, e);
            throw new SysPmException(message, e);
        } finally {
            close(resultSet);
        }
        // statistic
        if (criteria.isStatisticRequest()) {
            executeStatisticRequest(plan);
            result.setStatistics(new ArrayList<StatisticElement>(plan.getStatisticElements()));
        }
        // grouping
        if (criteria.isGroupingRequest()) {
            executeGroupingRequest(criteria, plan);
            result.setGroups(new ArrayList<StatisticElement>(plan.getGroupElements()));
        }

        LOGGER.debug("getList return :{}", result);

        return result;
    }

    @Override
    public <T extends Model> StatisticResult<T> getList(final Criteria<T> criteriaIn) throws SysPmException {
        StatisticResult<T> result = new StatisticResultImpl<>();
        LOGGER.debug("getList Criteria:{}", criteriaIn);
        final AbstractCriteria<T> criteria = (AbstractCriteria<T>) criteriaIn;

        final SqlGenerator plan = getSqlGenerator(false);
        plan.setCriteria(criteria);
        if (plan.isError()) {
            throw new SysPmException(plan.getErrorMessage());
        }
        String sql = "SELECT " + getDistinct(criteria) + plan.getSqlSelect() + " FROM " + plan.getSqlFrom() + plan.getSqlWhere() + plan.getSqlOrder(); // TOD
        // ORDER

        long countAllRow = 0;
        long startPosition = criteria.getStartPosition();
        final long countRow = criteria.getPageSize();
        if (supportLimit()) {
            if (criteria.getPageSize() > 0) {
                countAllRow = getCountSelect(criteria, plan);
                if (startPosition > (countAllRow - countRow)) {
                    startPosition = countAllRow - countRow + 1;
                }
                if (startPosition < 1) {
                    startPosition = 1;
                }
                criteria.setStartPosition(startPosition);
                result.setCountAllRow(countAllRow);
                sql = getRowSql(sql, startPosition, countRow);
            }
        }

        LOGGER.debug("sql getList:{}", sql);
        ResultSet resultSet = null;
        result.setResultList(new ArrayList<T>());
        try (JDBConnection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {

            plan.setStatements(statement);
            resultSet = statement.executeQuery();
            int current = 0;
            while (resultSet.next()) {
                if (supportLimit() || criteria.getPageSize() <= 0) {
                    final T model = plan.getObjectFromResultSet(resultSet);

                    result.getResultList().add(model);
                } else {
                    if (current >= (criteria.getStartPosition() - 1) && (current < (criteria.getStartPosition() - 1 + criteria.getPageSize()))) {
                        final T model = plan.getObjectFromResultSet(resultSet);
                        result.getResultList().add(model);
                    }
                }
                current++;
            }

            if (!supportLimit() || criteria.getPageSize() <= 0) {
                result.setCountAllRow(current);
            }

        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            throw new SysPmException("Error in generator getList :" + sql + ":" + e.toString(), e);
        } finally {
            close(resultSet);
        }

        LOGGER.debug("getList return :{}", result);
        // statistic
        if (criteriaIn.isStatisticRequest()) {
            executeStatisticRequest(plan);
            result.setStatistics(new ArrayList<StatisticElement>(plan.getStatisticElements()));
        }
        // grouping
        if (criteria.isGroupingRequest()) {
            executeGroupingRequest(criteria, plan);
            result.setGroups(new ArrayList<StatisticElement>(plan.getGroupElements()));
        }

        return result;
    }

    protected void executeStatisticRequest(SqlGenerator plan) throws SysPmException {

        LOGGER.debug("executeStatisticrequest plan:{}", plan);
        String sql = "SELECT " + plan.getStatisticSelect() + " FROM " + plan.getSqlFrom() + plan.getSqlWhere();

        LOGGER.debug("sql executeStatisticrequest:{}", sql);
        ResultSet resultSet = null;
        try (JDBConnection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            plan.setStatements(statement);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                for (int i = 1; i <= plan.getStatisticElements().size(); i++) {
                    StatisticElement element = plan.getStatisticElements().get((i - 1));
                    element.setValue(resultSet.getObject(i));
                }
            }
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            throw new SysPmException("Error in generator executeStatisticrequest :" + sql + ":" + e.toString(), e);
        } finally {
            close(resultSet);
        }

        LOGGER.debug("executeStatisticrequest return :{}", plan.getStatisticElements());

    }

    protected void executeGroupingRequest(final Criteria<? extends Model> criteria, SqlGenerator plan) throws SysPmException {

        LOGGER.debug("executeStatisticrequest Criteria:{}", criteria);
        String sql = "SELECT " + plan.getGroupSelect() + " FROM " + plan.getSqlFrom() + plan.getSqlWhere() + plan.getGroupBy() + " ORDER BY "
                + plan.getGroupOrderBy();
        LOGGER.debug("sql executeStatisticrequest:{}", sql);
        ResultSet resultSet = null;
        try (JDBConnection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            plan.setStatements(statement);
            resultSet = statement.executeQuery();
            List<StatisticElement> headers = getUniqueFields(criteria.getGroups(), plan);
            HashMap<StatisticElement, StatisticElement> rows = new LinkedHashMap<>();
            while (resultSet.next()) {
                StatisticElement current = null;
                for (int i = 0; i < headers.size(); i++) {
                    StatisticElement template = headers.get(i).getCopy();
                    template.setValue(resultSet.getObject(i + 1));
                    if (i == 0) {
                        current = rows.get(template);
                        if (current == null) {
                            current = template;
                            rows.put(current, current);
                        }
                    } else {
                        StatisticElement child = current.getChild(template);
                        if (child == null) {
                            child = template;
                            current.getChildren().add(child);
                            current = child;
                        } else {
                            current = child;
                        }
                    }
                }
                for (int i = 0; i < plan.getGroupElements().size(); i++) {
                    StatisticElement element = plan.getGroupElements().get(i).getCopy();
                    element.setValue(resultSet.getObject(headers.size() + i + 1));
                    current.getChildren().add(element);
                }
            }
            plan.getGroupElements().clear();
            plan.getGroupElements().addAll(rows.keySet());
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            throw new SysPmException("Error in generator executeStatisticrequest :" + sql + ":" + e.toString(), e);
        } finally {
            close(resultSet);
        }

        LOGGER.debug("executeStatisticrequest return :{}", plan.getStatisticElements());

    }

    private List<StatisticElement> getUniqueFields(Set<StatisticElement> in, SqlGenerator plan) {
        List<StatisticElement> result = new ArrayList<>();
        HashSet<String> fields = new HashSet<>();
        for (StatisticElement statisticElement : in) {
            if (!statisticElement.isFieldOnly()) {
                String fieldName = plan.getFullDataBaseName(statisticElement.getModelName(), statisticElement.getFieldName());
                if (!fields.contains(fieldName)) {
                    StatisticElement copy = statisticElement.getCopy();
                    copy.setFunction(null);
                    result.add(copy);
                    fields.add(fieldName);
                }
            }
        }
        return result;
    }

    @Override
    public Identifier createIdentifierFromModel(final Model m) throws SysPmException {
        try {

            final ModelDescription table = dataDefinitionService.getModelDescription(m.getModelName());

            final Class<?> clazz = Class.forName(table.getClassId());
            final Identifier identifier = (Identifier) clazz.newInstance();

            for (final Field field : table.getPrimaryKeyFields()) {
                final Object value = new PropertyDescriptor(field.getName(), m.getClass()).getReadMethod().invoke(m);
                new PropertyDescriptor(field.getName(), identifier.getClass()).getWriteMethod().invoke(identifier, value);
            }
            return identifier;
        } catch (final Throwable e) {
            LOGGER.error("Error !", e);
            throw new SysPmException(e.getMessage(), e);
        }

    }

    @Override
    public Collection<Model> addModel(final Collection<Model> models) throws SystemException {
        LOGGER.debug("addModel Model:{}", models);
        if (models == null || models.size() == 0) {
            return Collections.emptyList();
        }
        Collection<Model> results = new ArrayList<>(models.size());
        final SqlGenerator plan = getSqlGenerator(false);
        plan.addModel(models.iterator().next());

        final String sql = "INSERT INTO " + plan.getSqlFrom() + " ( " + plan.getSqlSelect() + " ) VALUES (" + plan.getSqlParameters() + ")";

        LOGGER.debug("sql add:{}", sql);

        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Model model : models) {
                plan.setValues(model, statement);
                statement.executeUpdate();
                model.setIdentifier(createIdentifierFromModel(model));
                results.add(model);
            }

        } catch (final SQLException e) {
            throw new SysPmException("Error in generator getList :" + sql + " model:" + models + ":" + e.toString());
        } catch (final Throwable e) {
            LOGGER.error("Error in dataStore:", e);
            throw new SysPmException("Error in generator getList :" + sql + " model:" + models + ":" + e.toString());
        }
        return results;
    }

    protected String getDistinct(final Criteria<? extends Model> criteria) {
        return criteria.isDistinct() ? " DISTINCT " : "";
    }

    @Override
    public BigInteger getNextKey(final String nameModel, int count) {

        LOGGER.debug("getNextKey for :{}", nameModel);
        BigDecimal result = new BigDecimal("1");
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        JDBConnection connection = null;
        int transactionIsolation = Connection.TRANSACTION_NONE;
        try {
            connection = getConnection();
            transactionIsolation = connection.getTransactionIsolation();
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            connection.setAutoCommit(false);
            PreparedStatement statementNextKey = connection.prepareStatement(UPDATE_KEY_SQL);

            synchronized (data) {
                statementNextKey.setInt(1, count);
                statementNextKey.setString(2, nameModel);
                int existRow = statementNextKey.executeUpdate();
                if (existRow == 0) {
                    final String sql = "INSERT INTO SY_KEYS (KEYVALUE,ITERATOR,TABLENAME) VALUES (?,?,?)";
                    result = result.add(new BigDecimal("" + count));
                    statement = connection.prepareStatement(sql);
                    statement.setBigDecimal(1, result);
                    statement.setInt(2, count);
                    statement.setString(3, nameModel);
                    statement.executeUpdate();
                } else {
                    PreparedStatement statementNextKey1 = connection.prepareStatement(SELECT_KEY_SQL);
                    statementNextKey1.setString(1, nameModel);
                    resultSet = statementNextKey1.executeQuery();
                    resultSet.next();
                    result = resultSet.getBigDecimal(1);
                }
                LOGGER.debug("getNextKey return :{}", result);

                return result.toBigInteger();
            }

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.commit();
                    connection.setAutoCommit(true);
                    connection.setTransactionIsolation(transactionIsolation);
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            close(resultSet);
            close(statement);
        }

        LOGGER.debug("getNextKey return :{}", result);

        return result.toBigInteger();
    }

    /*
     * ARRAY, BIGINT, BINARY, BIT, BLOB, BOOLEAN, CHAR, CLOB, DATALINK, DATE,
     * DECIMAL, DISTINCT DOUBLE FLOAT INTEGER JAVA_OBJECT LONGVARBINARY
     * LONGVARCHAR NULL NUMERIC OTHER REAL REF SMALLINT STRUCT TIME TIMESTAMP
     * TINYINT VARBINARY VARCHAR
     */

    @Override
    public Model getModel(final Identifier identifier) throws ModelNotExistException, SysPmException {

        LOGGER.debug("getModel by  Identifier :{}", identifier);

        Model result = null;
        String sql = "";
        final SqlGenerator plan = getSqlGenerator(false);
        plan.setIdentifiers(Arrays.asList(identifier));
        sql = "SELECT " + plan.getSqlSelect() + " FROM " + plan.getSqlFrom() + plan.getSqlWhere();
        LOGGER.debug("generate new sql: {}", sql);
        LOGGER.debug("sql getModel:{}", sql);
        ResultSet resultSet = null;
        try (JDBConnection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {

            plan.setStatements(statement);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = plan.getObjectFromResultSet(resultSet);
                createIdentifierFromModel(result);
            } else {
                throw new ModelNotExistException();
            }
        } catch (final ModelNotExistException e) {
            throw e;
        } catch (final SysPmException e) {
            throw e;
        } catch (final Throwable e) {

            LOGGER.error("sql:" + sql + " " + e.getMessage(), e);
            throw new SysPmException("Error in generator getList :" + sql + ":" + e.toString());
        } finally {

            close(resultSet);
        }

        LOGGER.debug("getModel by  Identifier return :{}", result);

        return result;

    }

    @Override
    public int eraseModel(final Collection<Identifier> identifiers) throws ModelNotExistException, SystemException {
        int result = 0;
        if (identifiers.size() > pageSize) {
            Collection<Identifier> page = new ArrayList<>(pageSize);
            for (Identifier identifier : identifiers) {
                page.add(identifier);
                if (page.size() >= pageSize) {
                    result += erasePageModels(page);
                    page.clear();
                }
            }
            if (!page.isEmpty()) {
                result += erasePageModels(page);
            }
        } else {
            result += erasePageModels(identifiers);
        }
        return result;

    }

    public int erasePageModels(final Collection<Identifier> identifiers) throws ModelNotExistException, SystemException {
        LOGGER.debug("eraseModel by  Identifiers  :{}", identifiers);
        if (identifiers == null || identifiers.size() == 0) {
            return 0;
        }
        int result = 0;
        final SqlGenerator plan = getSqlGenerator(true);
        plan.setIdentifiers(identifiers);

        final String sql = "DELETE FROM " + plan.getSqlFrom() + plan.getSqlWhere();

        try (JDBConnection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            plan.setValues(identifiers, statement);
            result = statement.executeUpdate();

        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new SysPmException("Error in generator getList :" + sql + ":" + e.toString());
        }

        return result;

    }

    protected SqlGenerator getSqlGenerator(boolean noAlias) {
        return new CommonSqlGenerator(dataDefinitionService, false);
    }

    protected long getCountSelect(final AbstractCriteria<? extends Model> criteria, final SqlGenerator plan) throws SysPmException {
        long result = 0;
        final String sql = "SELECT COUNT(*) FROM (SELECT " + getDistinct(criteria) + plan.getSqlSelect() + " FROM " + plan.getSqlFrom() + plan.getSqlWhere()
                + ")  ";

        LOGGER.debug("sql count getList:{}", sql);
        ResultSet resultSet = null;
        try (JDBConnection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {
            plan.setStatements(statement);
            resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getLong(1);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new SysPmException("Error in generator getList :" + sql + ":" + e.toString());
        } finally {
            close(resultSet);
        }
        return result;

    }

    protected String getRowSql(final String sql, final long startPosition, final long countRow) {
        return " select b.* from (select a.*,rownum AS AG$CR  from (" + sql + ") a ) b WHERE b.AG$CR>=" + startPosition + " AND b.AG$CR<"
                + (startPosition + countRow);

    }

    protected boolean supportLimit() {
        return false;
    }

    @Override
    public JDBConnection getConnection() throws SysPmException {
        LOGGER.debug("get connection");
        JDBConnection result = connectPool.getObject();
        return result;

    }

    public ConnectionPool<JDBConnection> getConnectionPool() throws SysPmException {
        LOGGER.debug("get connection");
        return connectPool;

    }

    protected void close(final JDBConnection connection) {
        LOGGER.debug("close connection");

        if (connection != null) {
            connection.close();
        }

    }

    protected void close(PreparedStatement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (final SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    protected void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
                resultSet = null;
            } catch (final SQLException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void setDataDefinitionService(final DataDefinitionService dataDefinition) {
        dataDefinitionService = dataDefinition;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void setConnectPool(final ConnectionPool connectPool) {
        this.connectPool = connectPool;
    }

    @Override
    public void close() {
        connectPool.close();
    }

    @Override
    public Collection<Model> getModel(final Collection<Identifier> identifiers) throws ModelNotExistException, SystemException {

        LOGGER.debug("getModel by  Identifier :{}", identifiers);

        Collection<Model> result = new ArrayList<>(identifiers.size());
        String sql = "";
        final SqlGenerator plan = getSqlGenerator(false);
        plan.setIdentifiers(identifiers);
        sql = "SELECT " + plan.getSqlSelect() + " FROM " + plan.getSqlFrom() + plan.getSqlWhere();
        LOGGER.debug("sql getModel:{}", sql);
        ResultSet resultSet = null;
        try (JDBConnection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql);) {

            plan.setStatements(statement);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                result.add(plan.getObjectFromResultSet(resultSet));
            }
        } catch (final SysPmException e) {
            throw e;
        } catch (final Throwable e) {

            LOGGER.error("sql:" + sql + " " + e.getMessage(), e);
            throw new SysPmException("Error in generator getList :" + sql + ":" + e.toString());
        } finally {

            close(resultSet);
        }

        LOGGER.debug("getModel by  Identifier return :{}", result);

        return result;
    }

}
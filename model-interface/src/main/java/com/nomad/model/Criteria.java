package com.nomad.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.nomad.model.criteria.AggregateFunction;
import com.nomad.model.criteria.StatisticElement;
import com.nomad.model.criteria.StatisticResult;

/**
 * interface for criteria all criteria
 * @author alexgor Date: 06.01.2005 Time: 12:38:41
 */
public interface Criteria<T extends Model> extends Serializable {

    public static enum Condition {
        EQ, NE, GT, LT, GE, LE, LIKE_LEFT, LIKE_RIGHT, LIKE_ALL, IS_NULL, NOT_IS_NULL, NOT_LIKE, EQ_MASK, EQ_MASK_RIGHT, EQ_MASK_LEFT, IN
    };

    public static enum Case {
        UPPER_CASE, LOWER_CASE, NO_CASE
    };

    public static enum Concatenation {
        AND, OR
    };

    /**
     * set new List for return
     * @param retList list of Models
     */

    void setResult(StatisticResult<T> result);

    /**
     * indicate distinct select
     * @return true if distinct
     */
    boolean isDistinct();

    /**
     * set flag Distinct
     * @param distinct
     */

    void setDistinct(boolean distinct);

    /**
     * get count All row in request
     * @return
     */

    long getStartPosition();

    void setStartPosition(long startPosition);

    int getPageSize();

    void setPageSize(int countRow);

    String getModelName();

    StatisticResult<T> getResult();

    /**
     * Add String criterion
     * @param s String of value criterion
     * @param paramName name of criterion mast be specified in class
     */
    Criteria<T> addCriterion(String parameterName, Criteria.Condition condition, String s);

    /**
     * Add double criterion
     * @param s String of value criterion
     * @param paramName name of criterion mast be specified in class
     */
    Criteria<T> addCriterion(String parameterName, Criteria.Condition condition, double s);

    /**
     * Add util.Date criterion
     * @param s String of value criterion
     * @param paramName name of criterion mast be specified in class
     */
    Criteria<T> addCriterion(String parameterName, Criteria.Condition condition, Date s);

    /**
     * Add int criterion
     * @param s String of value criterion
     * @param paramName name of criterion mast be specified in class
     */
    Criteria<T> addCriterion(String parameterName, Criteria.Condition condition, int s);

    /**
     * Add long criterion
     * @param s String of value criterion
     * @param paramName name of criterion mast be specified in class
     */
    Criteria<T> addCriterion(String parameterName, Criteria.Condition condition, long s);

    Criteria<T> addCriterion(String parameterName, Criteria.Condition condition, List<Object> list);

    /**
     * add parameters in SQl "ORDER BY "
     * @param paramName name of parameter
     */
    Criteria<T> addOrderAsc(String parameterName);

    Criteria<T> addOrderAsc(String modelName, String parameterName);

    /**
     * add parameters in SQl "ORDER BY "
     * @param paramName name of parameter
     */
    Criteria<T> addOrderDesc(String parameterName);

    Criteria<T> addOrderDesc(String modelName, String parameterName);

    /**
     * count of criterion into criterion list
     * @return count of criterion
     */
    int count();

    Criteria<T> addORCriterion(CriteriaGroup e);

    Criteria<T> addANDCriterion(CriteriaGroup e);

    Criteria<T> cleanCriterion();

    boolean isBinaryLoad();

    Criteria<T> setBinaryLoad(boolean binaryLoad);

    boolean isCalculateCount();

    Criteria<T> setCalculateCount(boolean calculateCount);

    void cleanSort();

    List<SortField> getOrder();

    List<CriteriaItem> getCriteria();

    void addStatisticRequirement(String modelName, String fieldName, AggregateFunction function);

    void addStatisticRequirement(String fieldName, AggregateFunction function);

    Set<StatisticElement> getStatistics();

    StatisticElement getStatisticElement(String modelName, String fieldName, AggregateFunction function) throws Exception;

    boolean isStatisticRequest();

    boolean isGroupingRequest();

    Criteria<? extends Model> addRelationLoad(String relationName);

    boolean isRelationLoad(String relationName);

    boolean removeRelationLoad(String relationName);

    Set<String> getRelationsLoad();

    void addGroupRequirement(String fieldName, AggregateFunction function);

    void addGroupRequirement(String modelName, String fieldName, AggregateFunction function);

    Set<StatisticElement> getGroups();

    List<StatisticElement> getGroupingResult();

    void addGroupSelect(String modelName, String fieldName, AggregateFunction function);

    void addGroupSelect(String fieldName, AggregateFunction function);

}
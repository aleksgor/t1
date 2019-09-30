package com.nomad.pm.generator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

import com.nomad.model.Criteria;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.criteria.StatisticElement;
import com.nomad.pm.exception.SysPmException;

public interface SqlGenerator {

    String getSqlSelect();

    String getSqlFrom();

    String getSqlOrder();

    String getSqlWhere();

    String getSqlParameters();

  //  void deleteModel(Identifier identifier) throws SysPmException;

    void updateModel(Model model) throws SysPmException;

    void setIdentifiers(Collection<Identifier> id) throws SysPmException;

    void addModel(Model model) throws SysPmException;

    void setCriteria(Criteria<? extends Model> criteria) throws SysPmException;

    void setCriteriaForId(Criteria<? extends Model> criteria) throws SysPmException;

    void setStatements(PreparedStatement statement);

    <T extends Model> T getObjectFromResultSet(ResultSet resultSet) throws SysPmException;

    Identifier getIdentifierFromResultSet(ResultSet resultSet) throws SysPmException ;

    String getErrorMessage();

    boolean isError();

    String getStatisticSelect();

    String getGroupSelect();

    List<StatisticElement> getStatisticElements();

    String getGroupBy();

    int getGroupCount();

    String getFullDataBaseName(String modelName, String fieldName);

    List<StatisticElement> getGroupElements();

    String getGroupOrderBy();

    List<ParameterField> getParameters();

    void setValues(Model model,  PreparedStatement statement) throws SysPmException;

    void setValues(Collection<Identifier> identifiers, PreparedStatement statement) throws SysPmException;


}

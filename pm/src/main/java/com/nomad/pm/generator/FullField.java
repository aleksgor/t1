package com.nomad.pm.generator;

import java.util.LinkedList;
import java.util.List;

import com.nomad.model.Field;
import com.nomad.model.ModelDescription;

public class FullField {
  private ModelDescription table;
  private final List<Field> fields = new LinkedList<>();
  private final List<FullField> tables = new LinkedList<>();

  private String field = null;

  public FullField(final ModelDescription table) {
    super();
    this.table = table;
  }

  public String getField() {
    return field;
  }

  public void setField(final String field) {
    this.field = field;
  }

  public ModelDescription getTable() {
    return table;
  }

  public void setTable(final ModelDescription table) {
    this.table = table;
  }

  public List<Field> getFields() {
    return fields;
  }

  public List<FullField> getTables() {
    return tables;
  }

  @Override
  public String toString() {
    return "FullField [table=" + table + ", fields=" + fields + ", tables=" + tables + ", method=" + field + "]";
  }

}

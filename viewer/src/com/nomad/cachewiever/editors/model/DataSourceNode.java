package com.nomad.cachewiever.editors.model;

import org.eclipse.ui.views.properties.IPropertySource;

import com.nomad.cachewiever.editors.model.Node;
import com.nomad.model.DataSourceModel;

public class DataSourceNode extends Node {
  public static final String PROPERTY_CLASS="class";
  public static final String PROPERTY_NAME="name";
  public static final String PROPERTY_THREAD="thread";
  public static final String PROPERTY_PROP="$$PRPERY$$";
  
  private DataSourceModel dataSource;

  
  public DataSourceNode(DataSourceModel dataSource) {
    this.dataSource = dataSource;
  }


  public DataSourceModel getDataSource() {
    return dataSource;
  }


  public void setDataSource(DataSourceModel dataSource) {
    this.dataSource = dataSource;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Object getAdapter(Class adapter) {
    if (adapter == IPropertySource.class) {
      if (propertySource == null)
        propertySource = new DataSourcePropertySource(this);
      return propertySource;
    }
    return null;
  }

}
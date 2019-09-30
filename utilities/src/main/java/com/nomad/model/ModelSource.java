package com.nomad.model;

public class ModelSource {
  private DataSourceModelImpl dataSourceModel;
  private StoreModelImpl storeModel;

  public DataSourceModelImpl getDataSourceModel() {
    return dataSourceModel;
  }

  public void setDataSourceModel(DataSourceModelImpl dataSourceModel) {
    this.dataSourceModel = dataSourceModel;
  }

  public StoreModelImpl getStoreModel() {
    return storeModel;
  }

  public void setStoreModel(StoreModelImpl storeModel) {
    this.storeModel = storeModel;
  }
}

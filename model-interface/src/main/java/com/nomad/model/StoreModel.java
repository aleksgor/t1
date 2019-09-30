package com.nomad.model;


public interface StoreModel {

    public enum StoreType{
        OBJECT, BYTES, ZIPPED_BYTES
    }
    //     private int serverType = 7; // 1 byte- Cache , 2 byte: CM ,4- MatcherModel

    public enum ServerType{
        CACHE,CACHE_MANAGER,CACHE_CACHE_MANAGER,TRANSLATOR, ALL
    }

    ServerType getServerType();

    boolean isCache();

    boolean isCacheManager();

    boolean isTranslator();

    void setServerType(ServerType serverType);

    int getCopyCount();

    void setCopyCount(int copyCount);

    StoreType getStoreType();

    void setStoreType(StoreType storeType);

    String getClazz();

    void setClazz(String clazz);

    boolean isReadThrough();

    void setReadThrough(boolean readThrough);

    boolean isWriteThrough();

    void setWriteThrough(boolean writeThrough);

    String getModel();

    void setModel(String model);

    String getDataSource();

    void setDataSource(String dataSource);

    CacheMatcherModel getCacheMatcherModel();

    void setCacheMatcherModel(CacheMatcherModel cacheMatcherModel);

    int getMaxListSize() ;

    void setMaxListSize(int maxListSize) ;

}

package com.nomad.model;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "storeModel")
public class StoreModelImpl implements StoreModel {
    private String model;
    private String clazz;
    private boolean readThrough;
    private boolean writeThrough;
    private String dataSource;
    private int copyCount = 1;
    private ServerType serverType = ServerType.CACHE_CACHE_MANAGER; // 1 byte- Cache , 2 byte: CM ,4- MatcherModel
    private StoreType storeType = StoreType.BYTES; // 0- store model 1- store bytes as Model 2- store zip bytes as model

    private CacheMatcherModel cacheMatcherModel;
    private int maxListSize = 200;


    @Override
    public ServerType getServerType() {
        return serverType;
    }

    @Override
    public void setServerType(final ServerType serverType) {
        this.serverType = serverType;
    }

    @Override
    public int getCopyCount() {
        return copyCount;
    }

    @Override
    public void setCopyCount(final int copyCount) {
        this.copyCount = copyCount;
    }

    @Override
    public StoreType getStoreType() {
        return storeType;
    }

    @Override
    public void setStoreType(final StoreType storeType) {
        this.storeType = storeType;
    }

    @Override
    public String getClazz() {
        return clazz;
    }

    @Override
    @XmlElement(name = "class")
    public void setClazz(final String clazz) {
        this.clazz = clazz;
    }

    @Override
    public boolean isReadThrough() {
        return readThrough;
    }

    @Override
    public void setReadThrough(final boolean readThrough) {
        this.readThrough = readThrough;
    }

    @Override
    public boolean isWriteThrough() {
        return writeThrough;
    }

    @Override
    public void setWriteThrough(final boolean writeThrough) {
        this.writeThrough = writeThrough;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public void setModel(final String model) {
        this.model = model;
    }

    @Override
    public String getDataSource() {
        return dataSource;
    }

    @Override
    public void setDataSource(final String dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    @XmlElement(name = "cacheMatcher", type = CacheMatcherModelImpl.class)
    public CacheMatcherModel getCacheMatcherModel() {
        return cacheMatcherModel;
    }

    @Override
    public void setCacheMatcherModel(final CacheMatcherModel cacheMatcherModel) {
        this.cacheMatcherModel = cacheMatcherModel;
    }

    @Override
    public int getMaxListSize() {
        return maxListSize;
    }

    @Override
    public void setMaxListSize(final int maxListSize) {
        this.maxListSize = maxListSize;
    }

 
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cacheMatcherModel == null) ? 0 : cacheMatcherModel.hashCode());
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
        result = prime * result + copyCount;
        result = prime * result + ((dataSource == null) ? 0 : dataSource.hashCode());
        result = prime * result + maxListSize;
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + (readThrough ? 1231 : 1237);
        result = prime * result + ((serverType == null) ? 0 : serverType.hashCode());
        result = prime * result + ((storeType == null) ? 0 : storeType.hashCode());
        result = prime * result + (writeThrough ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StoreModelImpl other = (StoreModelImpl) obj;
        if (cacheMatcherModel == null) {
            if (other.cacheMatcherModel != null)
                return false;
        } else if (!cacheMatcherModel.equals(other.cacheMatcherModel))
            return false;
        if (clazz == null) {
            if (other.clazz != null)
                return false;
        } else if (!clazz.equals(other.clazz))
            return false;
        if (copyCount != other.copyCount)
            return false;
        if (dataSource == null) {
            if (other.dataSource != null)
                return false;
        } else if (!dataSource.equals(other.dataSource))
            return false;
        if (maxListSize != other.maxListSize)
            return false;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (readThrough != other.readThrough)
            return false;
        if (serverType != other.serverType)
            return false;
        if (storeType != other.storeType)
            return false;
        if (writeThrough != other.writeThrough)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StoreModel [model=" + model + ", clazz=" + clazz + ", readThrough=" + readThrough + ", writeThrough=" + writeThrough
                + ", dataSource=" + dataSource + ", copyCount=" + copyCount + ", serverType=" + serverType + ", storeType=" + storeType + ", cacheMatcherModel="
                + cacheMatcherModel + "]";
    }

    @Override
    public boolean isCache() {
        return (serverType==ServerType.CACHE || serverType==ServerType.CACHE_CACHE_MANAGER || serverType==ServerType.ALL);
        
    }

    @Override
    public boolean isCacheManager() {
        return (serverType==ServerType.CACHE_MANAGER || serverType==ServerType.CACHE_CACHE_MANAGER || serverType==ServerType.ALL);
    }

    @Override
    public boolean isTranslator() {
        return  (serverType==ServerType.TRANSLATOR || serverType==ServerType.ALL);
    }

}

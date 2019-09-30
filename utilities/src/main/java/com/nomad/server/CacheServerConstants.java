package com.nomad.server;

public interface CacheServerConstants {

    public interface Statistic {
        public static final String STORE_CONNECTION_POOL_STATISTIC_GROUP_NAME = "StoreConnectionPool";
        public static final String SESSION_CLIENT_GROUP_NAME = "SessionClient";
        public static final String CACHE_MANAGER_CLIENT_GROUP_NAME = "CacheManagerSessionClient";
        public static final String LISTENER_STATISTIC_GROUP_NAME = "ListenerStatistic";
        public static final String SAVE_SERVICE_GROUP_NAME = "SaveService";
        public static final String DATABASE_CONNECT_GROUP_NAME = "DataBasePool";
        public static final String CLUSTER_CLIENT_CONNECT_GROUP_NAME = "clusterClientConnectGroupName";
        public static final String MANAGEMENT_CLIENT_GROUP_NAME = "ManagementSessionClient";
        public static final String ID_GENERATOR_NAME = "idGenerator";

        public interface StoreConnectionPoolProperties {
            public static final String POOL_SIZE = "poolsize";
            public static final String MAX_POOL_USE = "maxPoolUse";
            public static final String HOST = "host";
            public static final String PORT = "port";
        }

        public interface ListenerProperties {

            public static final String HOST = "host";
            public static final String PORT = "port";
            public static final String COMMAND = "command";
            public static final String MODEL_NAME = "modelName";
        }

    }
}

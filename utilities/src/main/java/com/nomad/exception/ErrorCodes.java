package com.nomad.exception;

public class ErrorCodes {
    public static class Update{
        public static String ERROR_UPDATE_INVALID_OPERNAD="error.update.invalid.opernad";
        public static String ERROR_UPDATE_INVALID_PROPERTY_NAME="error.update.invalid.property.name";
        public static String ERROR_UPDATE_INVALID_PROPERTY_ACCESS="error.update.invalid.property.access";
        public static String ERROR_UPDATE_INVALID_PROPRTY="error.update.invalid.property";
    }
    public static class Cache{
        public static String ERROR_CACHE_MODEL_NOT_EXIST="error.cache.model.not.exist";
        public static String ERROR_CACHE_RELATION_NOT_EXIST="error.cache.relation.not.exist";
        public static String ERROR_CACHE_MODEL_UNSUPPORTED="error.cache.model.unsupported";
        public static String ERROR_CACHE_NO_ACTIVE_SERVERSES="error.cache.no.active.serverses";
    }
    public static class Block{
        public static String ERROR_CACHE_SOFT_BLOCK="error.cache.soft.block";
        public static String ERROR_CACHE_HARD_BLOCK="error.cache.hard.block";
    }
    public static class Session{
        public static String ERROR_SESSION_CREATE_SESSION="error.session.create.session";
    }
    public static class IdGenerator{
        public static String ERROR_IDGENERATOR_NO_METHOD="error.idgenerator.no.method";
    }
    public static class Connect{
        public static String ERROR_CONNECT_EOF="error.connect.eof";
    }
    public static class Model{
        public static String MODEL_NOT_SUPPORTED="error.model.not.supported";
        public static String RELATION_NOT_SUPPORTED="relation.model.not.supported";
    }
}

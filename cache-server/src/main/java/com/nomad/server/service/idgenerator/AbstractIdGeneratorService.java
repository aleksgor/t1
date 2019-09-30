package com.nomad.server.service.idgenerator;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import com.nomad.exception.SystemException;
import com.nomad.model.Field;
import com.nomad.model.Identifier;
import com.nomad.model.ModelDescription;
import com.nomad.model.idgenerator.IdGeneratorService;
import com.nomad.server.DataDefinitionService;
import com.nomad.server.ServerContext;
import com.nomad.server.idgenerator.AtomicBigInteger;
public abstract class AbstractIdGeneratorService implements IdGeneratorService {

    protected volatile ServerContext context;
    protected DataDefinitionService dataDefinition;
    private Map<String, IdInformation> informationCache = new HashMap<>();
    protected final String GENERATE_SET_METHOD_NAME = "setGeneratedId";


    public AbstractIdGeneratorService(ServerContext context) {
        this.context = context;
    }

    @Override
    public void start() throws SystemException {
        dataDefinition = context.getDataDefinitionService(null);

    }

    @SuppressWarnings("unchecked")
    protected IdInformation getIdInformation(String modelName) throws SystemException {
        IdInformation result = informationCache.get(modelName);
        if (result == null) {
            ModelDescription description = dataDefinition.getModelDescription(modelName);
            result = new IdInformation();
            result.setCounter(new AtomicBigInteger("0"));
            Class<Identifier> identifierClass;
            try {
                identifierClass = (Class<Identifier>) Class.forName(description.getClassId());
                result.setClazz(identifierClass);
                result.setMethod(identifierClass.getMethod(GENERATE_SET_METHOD_NAME, BigInteger.class));
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e) {
                throw new SystemException(e);
            }

            for (Field field : description.getPrimaryKeyFields()) {
                if (field.isNumber()) {
                    result.setFieldName(field.getName());
                    return result;
                }
            }
        }
        return result;
    }

}

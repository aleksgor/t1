package com.nomad.server.service.idgenerator;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.model.Criteria;
import com.nomad.model.DataInvoker;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.model.criteria.StatisticResult;
import com.nomad.server.ServerContext;

public class SystemDataInvoker implements DataInvoker {
    private static Logger LOGGER = LoggerFactory.getLogger(SystemDataInvoker.class);
    public final static String BASE_PATH_NAME = "basePath";

    private String basePath = "idPath";
    private File baseDirectory;
    private static final Map<String, Object> syncMap = new HashMap<>();

    @Override
    public void init(Properties properties, ServerContext context, String connectName) throws SystemException {
        if (properties != null) {
            String newValue = properties.getProperty(BASE_PATH_NAME);
            if (newValue != null) {
                basePath = newValue;
            }
        }
        baseDirectory = new File(basePath);
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs();
        }
        LOGGER.info("init SystemDataInvoker baseDirectory:" + baseDirectory.getAbsolutePath());
    }

    @Override
    public <T extends Model > StatisticResult<T> getIds(Criteria<T> criteria) throws SystemException {
        return null;
    }

    @Override
    public Collection<Model> addModel(Collection <Model> models) throws SystemException {
        List<Model> result = new ArrayList<>();
        for (Model model : models) {

            IdGeneratorModel idGeneratorModel=(IdGeneratorModel) model;
            File file = new File(baseDirectory, idGeneratorModel.getKeyName());
            try {
                writeFile(file, idGeneratorModel.getValue(), idGeneratorModel.getKeyName());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            result.add(idGeneratorModel);
        }
        return result;
    }

    private BigInteger readFile(File file, String modelName) throws IOException {
        Object sync;
        synchronized (syncMap) {
            sync = syncMap.get(modelName);
            if (sync == null) {
                sync = new Object();
                syncMap.put(modelName, sync);
            }
        }

        synchronized (sync) {
            String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
            if (content == null || content.length() == 0) {
                content = "1";
            }
            BigInteger result = new BigInteger(content);
            return result;
        }
    }

    private void writeFile(File file, BigInteger value, String modelName) throws IOException {
        Object sync;
        synchronized (syncMap) {
            sync = syncMap.get(modelName);
            if (sync == null) {
                sync = new Object();
                syncMap.put(modelName, sync);
            }
        }
        synchronized (sync) {
            if (!file.exists()) {
                file.createNewFile();
            }
            Files.write(Paths.get(file.getAbsolutePath()), value.toString().getBytes("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }


    @Override
    public Collection<Model> updateModel(Collection<Model> models) throws SystemException {
       
        Collection<Model> result = new ArrayList<>(models.size());
        for (Model model : models) {
            IdGeneratorModel idGeneratorModel= (IdGeneratorModel)model;
            File file = new File(baseDirectory, idGeneratorModel.getKeyName());
            result.add(idGeneratorModel);
            try {
                writeFile(file, idGeneratorModel.getValue(), idGeneratorModel.getKeyName());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            
        }
        return result;
    }



    @Override
    public BigInteger getNextKey(String modelName, int count) {
        Object sync;
        synchronized (syncMap) {
            sync = syncMap.get(modelName);
            if (sync == null) {
                sync = new Object();
                syncMap.put(modelName, sync);
            }
        }
        synchronized (sync) {
            IdGeneratorModelId identifier = new IdGeneratorModelId(modelName);
            BigInteger value = null;
            File file = new File(baseDirectory, (identifier).getKeyName());
            FileLock lock = null;
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
                FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.SYNC);
                lock = fileChannel.lock(0, Long.MAX_VALUE, true);
                try {
                    value = readFile(file, identifier.getKeyName());
                    value =value.add(new BigInteger("" + count));
                    writeFile(file, value, modelName);
                    return value;
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public Collection<Model> getModel(Collection<Identifier> identifiers) throws ModelNotExistException, SystemException {
        List<Model> result = new ArrayList<>();
        for (Identifier identifier : identifiers) {
            try {
                result.add(getModel(identifier));
            } catch (ModelNotExistException e) {
                ;
            }
        }
        return result;
    }

    @Override
    public IdGeneratorModel getModel(Identifier identifierIn) throws ModelNotExistException, SystemException {
        IdGeneratorModelId identifier = (IdGeneratorModelId) identifierIn;
        File f = new File(baseDirectory, identifier.getKeyName());
        if (f.exists()) {
            try {
                BigInteger value = readFile(new File(baseDirectory, (identifier).getKeyName()), identifier.getKeyName());
                IdGeneratorModel result = new IdGeneratorModel();
                result.setIdentifier(identifier);
                result.setValue(value);
                result.setKeyName(identifier.getKeyName());
                return result;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        throw new ModelNotExistException();
    }

    @Override
    public int eraseModel(Collection<Identifier> identifiers) throws ModelNotExistException, SystemException {
        int counter = 0;
        for (Identifier identifier : identifiers) {
            File file = new File(baseDirectory, ((IdGeneratorModelId) identifier).getKeyName());
            counter +=  (file.exists() && file.delete()) ? 1 : 0;

        }
        return counter;
    }


    @Override
    public int eraseModel(Criteria<? extends Model> criteria) throws SystemException {
        return 0;
    }

    @Override
    public Connection getConnection() throws SystemException {
        return null;
    }

    @Override
    public Identifier createIdentifierFromModel( Model m) throws SystemException {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public <T extends Model> StatisticResult<T> getList(Criteria<T> criteria) throws SystemException {
        return null;
    }

}

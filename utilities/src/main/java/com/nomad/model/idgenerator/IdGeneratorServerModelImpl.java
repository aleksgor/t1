package com.nomad.model.idgenerator;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.nomad.model.CommonServerModelImpl;

@XmlRootElement(name = "idGeneratorServer")
@XmlAccessorType(XmlAccessType.FIELD)
public class IdGeneratorServerModelImpl extends CommonServerModelImpl implements IdGeneratorServerModel {

    private int increment = 1;

    private int timeOut = 0;

    private final Map<String, String> modelSource = new HashMap<>();

    private String invokerClass;

    @Override
    public String getInvokerClass() {
        return invokerClass;
    }

    @Override
    public void setInvokerClass(String invokerClass) {
        this.invokerClass = invokerClass;
    }

    @Override
    public Map<String, String> getModelSource() {
        return modelSource;
    }

    @Override
    public int getIncrement() {
        return increment;
    }

    @Override
    public void setIncrement(int increment) {
        this.increment = increment;
    }

    @Override
    public int getTimeOut() {
        return timeOut;
    }

    @Override
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    @Override
    public String toString() {
        return "SessionServerModelImp [" + super.toString() + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + increment;
        result = prime * result + ((modelSource == null) ? 0 : modelSource.hashCode());
        result = prime * result + timeOut;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        IdGeneratorServerModelImpl other = (IdGeneratorServerModelImpl) obj;
        if (increment != other.increment)
            return false;
        if (modelSource == null) {
            if (other.modelSource != null)
                return false;
        } else if (!modelSource.equals(other.modelSource))
            return false;
        if (timeOut != other.timeOut)
            return false;
        return true;
    }

}

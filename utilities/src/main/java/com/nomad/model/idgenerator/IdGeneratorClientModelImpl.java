package com.nomad.model.idgenerator;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.nomad.model.CommonClientModelImpl;

@XmlRootElement(name = "idGeneratorClient")
public class IdGeneratorClientModelImpl extends CommonClientModelImpl implements IdGeneratorClientModel {

    @XmlElementWrapper(name = "modelNames")
    @XmlElement(type = String.class, name = "modelName")
    private final Set<String> modelNames = new HashSet<>();

    @Override
    public Set<String> getModelNames() {
        return modelNames;
    }

    @Override
    public String toString() {
        return "IdGeneratorClientModelImpl " + super.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((modelNames == null) ? 0 : modelNames.hashCode());
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
        IdGeneratorClientModelImpl other = (IdGeneratorClientModelImpl) obj;
        if (modelNames == null) {
            if (other.modelNames != null)
                return false;
        } else if (!modelNames.equals(other.modelNames))
            return false;
        return true;
    }

}

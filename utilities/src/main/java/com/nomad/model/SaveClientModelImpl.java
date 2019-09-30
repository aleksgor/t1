package com.nomad.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.nomad.model.saveserver.SaveClientModel;

@XmlRootElement(name="saveClient")
public class SaveClientModelImpl extends CommonClientModelImpl implements SaveClientModel {


    @Override
    public String toString() {
        return "SaveClientModelImpl [ host=" + host + ", port=" + port + ", threads=" + threads + ", timeout=" + timeout + "]";
    }


}

package com.nomad.model.saveserver;

import java.util.List;

import com.nomad.model.CommonServerModel;

public interface SaveServerModel extends CommonServerModel{

    long getSessionTimeout() ;

    void setSessionTimeout(long sessionTimeout) ;

    List<SaveClientModel> getMirrors();


}

package com.nomad.model.session;

import java.util.Collection;

import com.nomad.model.CommonServerModel;
import com.nomad.model.athorization.AuthorizationServiceModel;

public interface SessionServerModel  extends CommonServerModel{

    long getSessionTimeLive() ;

    void setSessionTimeLive(long sessionTimeLive) ;

    Collection<SessionClientModel> getMirrors() ;

    AuthorizationServiceModel getAuthorizationService();

    void setAuthorizationService(AuthorizationServiceModel authorizationModel);

}

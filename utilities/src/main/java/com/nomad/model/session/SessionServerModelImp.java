package com.nomad.model.session;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;

import com.nomad.model.CommonServerModelImpl;
import com.nomad.model.athorization.AuthorizationServiceModel;

public class SessionServerModelImp extends CommonServerModelImpl implements SessionServerModel {

    private long sessionTimeLive = 0;

    private final Collection<SessionClientModel> mirrors = new ArrayList<>();

    private AuthorizationServiceModel authorizationService;

    @Override
    public long getSessionTimeLive() {
        return sessionTimeLive;
    }

    @Override
    public void setSessionTimeLive(final long sessionTimeLive) {
        this.sessionTimeLive = sessionTimeLive;
    }

    @Override
    public Collection<SessionClientModel> getMirrors() {
        return mirrors;
    }

    @Override
    public AuthorizationServiceModel getAuthorizationService() {
        return authorizationService;
    }

    @XmlElement(name = "authorizationService", type = AuthorizationServiceModelImpl.class)
    @Override
    public void setAuthorizationService(AuthorizationServiceModel authorizationModel) {
        this.authorizationService = authorizationModel;
    }

    @Override
    public String toString() {
        return "SessionServerModelImp [sessionTimeLive=" + sessionTimeLive + ", authorizationService=" + authorizationService + "]";
    }

}

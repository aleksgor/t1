package com.nomad.server.service.session;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nomad.model.session.SessionClientModel;
import com.nomad.model.session.SessionServerModel;
import com.nomad.server.ServerContext;
import com.nomad.server.SessionService;
import com.nomad.server.service.session.server.LocalSessionService;
import com.nomad.server.service.session.server.NetworkSessionService;
import com.nomad.server.service.session.server.TrustSessionService;

public class SessionServiceFactory {
    protected static Logger LOGGER = LoggerFactory.getLogger(SessionServiceFactory.class);

    public static SessionService getSessionService(final List<SessionClientModel> clientModels, SessionServerModel serverModel, final ServerContext context,
            final boolean trustSessions) {
        if (trustSessions && (clientModels == null || clientModels.size() == 0) && serverModel == null) {
            return new TrustSessionService();
        }
        if (serverModel != null && serverModel.getPort() <= 0) {
            return new LocalSessionService(serverModel, context);
        }
        if (clientModels.size() > 0) {
            LOGGER.info("Return network session service");
            return new NetworkSessionService(clientModels, context);
        }

        LOGGER.info("Return  session service null");
        return new TrustSessionService();
    }

    public static boolean checkLocal(final List<SessionClientModel> clientModels) {
        if (clientModels == null || clientModels.size() == 0) {
            return true;
        }
        if (clientModels.size() >= 1) {
            return clientModels.iterator().next().getPort() == 0;
        }
        return false;
    }
}

package com.nomad.server.service.saveservice;

public class SaveCoordinator {

    /**
     * first the best
     */
    public static boolean compare(final long clientId1, final long clientId2) {
        return clientId1 > clientId2;
    }
}

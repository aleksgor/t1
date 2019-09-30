package com.nomad.model.core;

import java.util.Collection;

public interface SessionContainer {
    String getSessionId();

    String getMainSessionId();

    Collection<String> getSessions();
    
    boolean isEmpty();

}

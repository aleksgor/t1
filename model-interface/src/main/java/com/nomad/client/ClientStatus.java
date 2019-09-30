package com.nomad.client;

import com.nomad.model.ConnectStatus;

public interface ClientStatus {
    ConnectStatus getStatus();

    void setStatus(final ConnectStatus status);

}

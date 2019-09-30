package com.nomad.server;

import com.nomad.model.Identifier;

public interface DateIdentifier {
    Identifier getIdentifier();

    long getTime();

}

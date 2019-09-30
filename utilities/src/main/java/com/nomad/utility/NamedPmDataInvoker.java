package com.nomad.utility;

import java.util.HashMap;
import java.util.Map;

import com.nomad.server.PmDataInvoker;

public class NamedPmDataInvoker {
    private Map<String, PmDataInvoker> namedInvokers = new HashMap<String, PmDataInvoker>();

    public PmDataInvoker getPmDataInvoker(String name) {
        return namedInvokers.get(name);
    }

    public void putPmDataInvoker(String name, PmDataInvoker invoker) {
        namedInvokers.put(name, invoker);
    }

}

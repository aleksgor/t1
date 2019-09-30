package com.nomad.pm.transact;

public class SessionAssociation {

    private static ThreadLocal<TransactionMarker> threadInvoker = new ThreadLocal<>();

    public static TransactionMarker getTransactInvoker() {
        return threadInvoker.get();
    }

    public static void setTransactInvoker(final TransactionMarker a) {

        threadInvoker.set(a);
    }

    public static void clear() {
        threadInvoker.set(null);
    }
}

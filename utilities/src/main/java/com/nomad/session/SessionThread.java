package com.nomad.session;


public class SessionThread {
    public static final ThreadLocal<SessionContainer> threadLocal = new ThreadLocal<>();

    public static void set(SessionContainer user) {
        threadLocal.set(user);
    }

    public static void remove() {
        threadLocal.remove();
    }

    public static SessionContainer get() {
        return threadLocal.get();
    }

}

package com.nomad.server;

import java.util.concurrent.ExecutorService;

public interface ExecutorServiceProvider extends ServiceInterface {

    ExecutorService getExecutorService();

}

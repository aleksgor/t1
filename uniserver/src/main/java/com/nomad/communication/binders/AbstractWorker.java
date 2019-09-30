package com.nomad.communication.binders;

import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;
import com.nomad.server.CommonThreadListener;
import com.nomad.utility.PooledObject;

public interface AbstractWorker<K extends CommonMessage, T extends CommonAnswer> extends Runnable, CommonThreadListener,PooledObject  {


    int getWorkerId();

}

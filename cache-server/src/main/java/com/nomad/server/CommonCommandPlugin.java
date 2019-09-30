package com.nomad.server;

import com.nomad.utility.pool.PooledObjectImpl;

public abstract class CommonCommandPlugin extends PooledObjectImpl implements CommandPlugin{
	
	
  @Override
  public void closeObject() {
    
  }

}

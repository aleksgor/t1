package com.nomad.store.transaction;

import java.io.File;

public class FileRemoveThread implements Runnable{

  private String fileName;
  public FileRemoveThread(String fileName){
    this.fileName=fileName;
  }
  @Override
  public void run() {
   File file= new File(fileName);
   file.delete();
  }

}

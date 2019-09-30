package com.nomad.model;

import java.io.IOException;
import java.util.Date;

import com.nomad.io.MessageInputStream;
import com.nomad.io.MessageOutputStream;
import com.nomad.serializer.Serializer;

public class MemoryInfoSerializer implements Serializer<MemoryInfo> {

  public void write(MessageOutputStream out, MemoryInfo data) throws IOException {
    out.writeLong(data.getFreeMemory());
    out.writeLong(data.getMaxMemory());
    out.writeLong(data.getTotalMemory());
    out.writeLong(data.getDate().getTime());
  
  }

  public MemoryInfo read(MessageInputStream input) throws IOException {
    MemoryInfo result = new MemoryInfo();
    result.setFreeMemory(input.readLong());
    result.setMaxMemory(input.readLong());
    result.setTotalMemory(input.readLong());
    result.setDate(new Date(input.readLong()));
    return result;
  }

}

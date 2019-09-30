package com.nomad.utility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestDump {

    public static String dumpInputStream(InputStream in) throws IOException{
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream out= new ByteArrayOutputStream();
        int count = in.read(bytes);
        out.write(bytes, 0, count);
        return out.toString();

    }
}

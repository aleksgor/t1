package com.nomad.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class FakeMessageInputStream {


    private OutputStream output;
    private InputStream input;

    public FakeMessageInputStream(InputStream input, OutputStream output) {
        this.output=output;
        this.input=input;
    }


    /**
     * Currently these data types are supported.
     *
     * Integer Float Double BigDecimal Date Time Timestamp Boolean String byte[]
     * List Map
     *
     * @return
     * @throws IOException
     */
    public void readObject() throws IOException {

        byte [] buffer= new byte[512];
        int read = 0;
        int available= input.available();
        while(available>0){
            int bufferLength = Math.min(512, available - read);
            while ((read = input.read(buffer, 0, bufferLength)) > 0) {

                output.write(buffer, 0, read);
            }
            available= input.available();
        }

    }


}

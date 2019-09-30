package com.nomad.server;

import com.nomad.message.CommonAnswer;
import com.nomad.message.CommonMessage;

public class StringMessage implements CommonMessage, CommonAnswer{

    private  String data="";

    public StringMessage(final String data){
        this.data=data;
    }

    @Override
    public int getResultCode() {
        return 0;
    }

    public String getData() {
        return data;
    }

    public void setData(final String data) {
        this.data = data;
    }
    public int length(){
        if(data==null){
            return 0;
        }
        return data.length();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StringMessage other = (StringMessage) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StringMessage [data=" + data + "]";
    }


}

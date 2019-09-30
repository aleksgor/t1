package com.nomad.server.mbean;


public class StatusMBean implements StatusMXBean {

    private String status;

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

}

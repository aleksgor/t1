package com.nomad.client;


public class CountResult extends AbstractResult{
    private final long count;


    public CountResult(long count){
        this.count=count;
    }

    public long getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "CountResult [count=" + count + ", getOperationStatus()=" + getOperationStatus() + ", getAnswerCode()=" + getAnswerCode()
                + ", getAnswerMessage()=" + getAnswerMessage() + "]";
    }

}

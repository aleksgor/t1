package com.nomad.client;

import com.nomad.message.OperationStatus;

public abstract class AbstractResult {
    private OperationStatus operationStatus;
    private String answerCode;
    private String answerMessage;

    public OperationStatus getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(OperationStatus status) {
        this.operationStatus = status;
    }

    public String getAnswerCode() {
        return answerCode;
    }

    public void setAnswerCode(String answerCode) {
        this.answerCode = answerCode;
    }

    public String getAnswerMessage() {
        return answerMessage;
    }

    public void setAnswerMessage(String answerMessage) {
        this.answerMessage = answerMessage;
    }

}

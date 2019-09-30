package com.nomad.server.sessionserver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TransactionData {

  private final byte[] tranzactionId;
  private final List<TransactionData> childTransactions = new ArrayList<>();
  
  public TransactionData(byte[] tranzactionId) {
    super();
    this.tranzactionId = tranzactionId;
  }

  public byte[] getTranzactionId() {
    return tranzactionId;
  }

  public List<TransactionData> getChildTransactions() {
    return childTransactions;
  }

  @Override
  public String toString() {
    return "TransactionData [tranzactionId=" + Arrays.toString(tranzactionId) + ", childTransactions=" + childTransactions + "]";
  }
 
}

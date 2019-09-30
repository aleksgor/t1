package com.nomad.pm.transact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.nomad.exception.ModelNotExistException;
import com.nomad.exception.SystemException;
import com.nomad.message.MessageSenderReceiver;
import com.nomad.model.Identifier;
import com.nomad.model.Model;
import com.nomad.pm.exception.SysPmException;
import com.nomad.server.PmDataInvoker;
import com.nomad.utility.MessageUtil;

public class TransactionMarker implements Serializable {

    private final MessageSenderReceiver msr;

    PmDataInvoker dataInvoker = null;

    List<TransactElement> elements = new ArrayList<>();

    public TransactionMarker(final PmDataInvoker invoker, final MessageSenderReceiver msr) {
        super();
        dataInvoker = invoker;
        elements = new ArrayList<>();
        this.msr=msr;
    }

    public void setDataInvoker(final PmDataInvoker dataInvoker) {
        this.dataInvoker = dataInvoker;
    }

    public void transactBegin() {
        // Thread t=Thread.currentThread();
    }

    public void transactCommit() {
        elements = new ArrayList<>();
    }

    public void addOperation(final Model m, final TransactElement.Operation operation) throws SystemException {

        addOperation(m, operation, elements);
    }

    public void addOperation(final Model mIn, final TransactElement.Operation operation, final List<TransactElement> l) throws SystemException {


        final Model m = (Model) MessageUtil.clone(mIn, msr);

        TransactElement element = null;

        if (operation == TransactElement.Operation.INSERT_MODEL) {
            element = new TransactElement(m, m, operation);
        }
        if (operation == TransactElement.Operation.UPDATE_MODEL) {
            final Identifier identifier = m.getIdentifier();
            try {
                final Model oldModel = dataInvoker.getModel(identifier);
                element = new TransactElement(oldModel, m, operation);
            } catch (final ModelNotExistException e) {
                throw new SysPmException(e.getMessage(), e);
            }
        }
        if (operation == TransactElement.Operation.DELETE_MODEL) {
            element = new TransactElement(m, m, operation);
        }
        l.add(element);
    }

    public List<TransactElement> getElements() {
        return elements;
    }

}

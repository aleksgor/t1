package com.nomad.server.service.saveservice.model;

import java.util.ArrayList;
import java.util.Collection;

import com.nomad.message.SaveResult;
import com.nomad.model.Identifier;

public class SaveResultImpl implements SaveResult {

    private final Collection<Identifier> allowedIds = new ArrayList<>();
    // -10 -too many sessions
    private int resultCode = -1;

    public SaveResultImpl(final Collection<Identifier> readyIds) {
        super();
        if (readyIds != null) {
            allowedIds.addAll(readyIds);
        }
    }
    public SaveResultImpl(final Collection<Identifier> readyIds,final int resultCode ) {
        super();
        if (readyIds != null) {
            allowedIds.addAll(readyIds);
        }
        this.resultCode=resultCode;
    }

    @Override
    public Collection<Identifier> getAllowedIds() {
        return allowedIds;
    }

    @Override
    public void setAllowedIds(final Collection<Identifier> allowedIds) {
        this.allowedIds.clear();
        this.allowedIds.addAll(allowedIds);

    }

    @Override
    public String toString() {
        return "SaveResult [readyIds=" + allowedIds + "]";
    }

    @Override
    public void setResultCode(final int resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public int getResultCode() {

        return resultCode;
    }

}

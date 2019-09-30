package com.nomad.model.session;

public enum SessionCommand {

    CREATE_SESSION(1), KILL_SESSION(2), CHECK_SESSION(3), REGISTER_CLIENT(4), COMMIT_SESSION(5), ROLLBACK_SESSION(6), COMMIT_PHASE1(7), COMMIT_PHASE2(8), GET_STATUS(12), 
    CREATE_CHILD_SESSION(14), COMMIT(15),
    // for sync
    SYNC_REMOVE_SESSION(20), SYNC_START_NEW_SESSION(21), SYNC_START_NEW_CHILD_SESSION(22), SYNC_GET_SESSION_STATE(23), TEST(24), SYNC_GET_ALL_SESSIONS(25);
    private int code;

    private SessionCommand(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

package com.nomad.model;

/**
 * 11-20
 *
 */
public enum ServiceCommand {
    IN_LOCAL_CACHE(11), GET_FROM_CACHE(12), PUT_INTO_CACHE(13), DELETE_FROM_CACHE(14),
    ROLLBACK_IN_CACHE(15), BLOCK(16), COMMIT_PHASE1(17), COMMIT_PHASE2(18), UNBLOCK(19), CLEAN_SAVE_SERVICE(20), GET_IDS_BY_CRITERIA(21),
    TEST(22),   // test
    CHARACTERISTIC_TEST(23),HARD_BLOCK(16),;



    private int commandIndex;

    ServiceCommand(final int commandIndex) {
        this.commandIndex = commandIndex;
    }

    public int getCommandIndex() {
        return commandIndex;
    }
}

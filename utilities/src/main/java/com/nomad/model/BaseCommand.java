package com.nomad.model;
/*
 * 0-10
 */
public enum BaseCommand {
    START_NEW_SESSION(1), GET(2), PUT(3), DELETE(4), COMMIT(5), ROLLBACK(6), CLOSE_SESSION(7), UPDATE(8), IN_CACHE(9), GET_LIST_ID_BY_CRITERIA(10), DELETE_BY_CRITERIA(11),START_CHILD_SESSION(12) ;

    private int commandIndex;

    BaseCommand(final int commandIndex){
        this.commandIndex=commandIndex;
    }
    public int getCommandIndex(){
        return commandIndex;
    }
}

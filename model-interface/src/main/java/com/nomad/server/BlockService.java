package com.nomad.server;

import java.util.Collection;

import com.nomad.model.Identifier;
import com.nomad.model.core.SessionContainer;

public interface BlockService extends ServiceInterface {
    public enum BlockLevel{
        READ_LEVEL(3),UPDATE_LEVEL(7);
        int level;
        BlockLevel(int level){
            this.level=level;
        }
        public int  getLevel(){
            return level;
        }
    }
    public static int UPDATE_STABLE = 3;
    public static int READ_STABLE = 7;

    Collection<Identifier> block(Collection<Identifier> ids, SessionContainer sessions, BlockLevel stableLevel);

    void unblock(SessionContainer sessions);

    boolean checkBlockLevel(Iterable<Identifier> id, BlockLevel blockLevel, SessionContainer sessions);

}

package org.ethereum.net.swarm;

import org.ethereum.db.Database;

/**
 * Created by Admin on 18.06.2015.
 */
public class DBStore implements ChunkStore {
    Database db;

    @Override
    public void put(Chunk chunk) {

    }

    @Override
    public Chunk get(Key key) {
        return null;
    }
}

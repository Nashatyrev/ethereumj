package org.ethereum.net.swarm;

/**
 * Created by Admin on 18.06.2015.
 */
public class LocalStore implements ChunkStore {

    DBStore dbStore;
    MemStore memStore;

    @Override
    public void put(Chunk chunk) {

    }

    @Override
    public Chunk get(Key key) {
        return null;
    }
}

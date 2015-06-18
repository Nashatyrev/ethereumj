package org.ethereum.net.swarm;

/**
 * Created by Admin on 18.06.2015.
 */
public class MemStore implements ChunkStore {

    @Override
    public void put(Chunk chunk) {

    }

    @Override
    public Chunk get(Key key) {
        return null;
    }

    public static class MemTreeStore extends MemStore {}
}

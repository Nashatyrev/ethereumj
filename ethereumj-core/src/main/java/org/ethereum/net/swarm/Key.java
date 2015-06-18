package org.ethereum.net.swarm;

/**
 * Created by Admin on 18.06.2015.
 */
public class Key {
    byte[] bytes;

    public Key(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
}

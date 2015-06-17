package org.ethereum.net.swarm;

import io.netty.buffer.ByteBuf;

/**
 * Distributed Preimage Archive
 */
public abstract class DPA {

    public abstract ByteBuf read(String hash);

    public abstract String /*Hash*/ store(ByteBuf data);
}

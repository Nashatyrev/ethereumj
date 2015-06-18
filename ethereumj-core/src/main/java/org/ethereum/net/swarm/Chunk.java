package org.ethereum.net.swarm;

// Chunk serves also serves as a request object passed to ChunkStores
// in case it is a retrieval request, Data is nil and Size is 0
// Note that Size is not the size of the data chunk, which is Data.Size() see SectionReader
// but the size of the subtree encoded in the chunk
// 0 if request, to be supplied by the dpa
public class Chunk {
    Key    key;            // always
    byte[] data;         // nil if request, to be supplied by dpa
    long   size;          // size of the data covered by the subtree encoded in this chunk
//    C        chan bool      // to signal data delivery by the dpa
//    req      *requestStatus //
//    wg       *sync.WaitGroup
//    dbStored chan bool
//    source   *peer


    public Chunk(Key key, byte[] data, long size) {
        this.key = key;
        this.data = data;
        this.size = size;
    }
}

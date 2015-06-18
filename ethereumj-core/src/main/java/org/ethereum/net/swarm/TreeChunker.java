package org.ethereum.net.swarm;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

/**
 * The distributed storage implemented in this package requires fix sized chunks of content
 Chunker is the interface to a component that is responsible for disassembling and assembling larger data.

 TreeChunker implements a Chunker based on a tree structure defined as follows:

 1 each node in the tree including the root and other branching nodes are stored as a chunk.

 2 branching nodes encode data contents that includes the size of the dataslice covered by its entire subtree under the node as well as the hash keys of all its children :
 data_{i} := size(subtree_{i}) || key_{j} || key_{j+1} .... || key_{j+n-1}

 3 Leaf nodes encode an actual subslice of the input data.

 4 if data size is not more than maximum chunksize, the data is stored in a single chunk
 key = sha256(int64(size) + data)

 2 if data size is more than chunksize*Branches^l, but no more than chunksize*
 Branches^(l+1), the data vector is split into slices of chunksize*
 Branches^l length (except the last one).
 key = sha256(int64(size) + key(slice0) + key(slice1) + ...)

 */
/*
Tree chunker is a concrete implementation of data chunking.
This chunker works in a simple way, it builds a tree out of the document so that each node either
represents a chunk of real data or a chunk of data representing an branching non-leaf node of the tree.
In particular each such non-leaf chunk will represent is a concatenation of the hash of its respective children.
This scheme simultaneously guarantees data integrity as well as self addressing. Abstract nodes are
transparent since their represented size component is strictly greater than their maximum data size,
since they encode a subtree.

If all is well it is possible to implement this by simply composing readers so that no extra allocation or
buffering is necessary for the data splitting and joining. This means that in principle there
can be direct IO between : memory, file system, network socket (bzz peers storage request is
read from the socket ). In practice there may be need for several stages of internal buffering.
Unfortunately the hashing itself does use extra copies and allocation though since it does need it.
*/

public class TreeChunker implements Chunker {

    private static final MessageDigest DEFAULT_HASHER;
    private static final int DEFAULT_BRANCHES = 128;

    static {
        try {
            DEFAULT_HASHER = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);  // Can't happen.
        }
    }


    private int branches;
    private MessageDigest hasher;

    private int hashSize;
    private long chunkSize;

    public TreeChunker() {
        this(DEFAULT_BRANCHES, DEFAULT_HASHER);
    }

    public TreeChunker(int branches, MessageDigest hasher) {
        this.branches = branches;
        this.hasher = hasher;

        hashSize = hasher.getDigestLength();
        chunkSize = hashSize * branches;
    }

    @Override
    public void split(Key key, SectionReader sectionReader, Collection<Chunk> consumer) {
        int depth = 0;
        long treeSize = chunkSize;
        long size = sectionReader.getSize();

        // takes lowest depth such that chunksize*HashCount^(depth+1) > size
        // power series, will find the order of magnitude of the data size in base hashCount or numbers of levels of branching in the resulting tree.
        for (; treeSize < size; treeSize *= branches) {
            depth++;
        }

        splitImpl(depth, treeSize, sectionReader, consumer);
    }

    private Key splitImpl(int depth, long treeSize, SectionReader data, Collection<Chunk> consumer) {
        long size = data.getSize();
        Chunk newChunk;
        Key hash;

        while (depth > 0 && size < treeSize) {
            treeSize /= branches;
            depth--;
        }

        if (depth == 0) {
            // leaf nodes -> content chunks
            byte []chunkData = new byte[(int) (data.getSize() + 8)];
            ByteBuffer.wrap(chunkData).order(ByteOrder.LITTLE_ENDIAN).putLong(0, size);
            data.read(chunkData, 8);
            hash = new Key(hasher.digest(chunkData));
            newChunk = new Chunk(hash, chunkData, size);
        } else {
            // intermediate chunk containing child nodes hashes
            int branchCnt = (int) ((size + treeSize - 1) / treeSize);

            byte[] chunk = new byte[(int) (branchCnt * hashSize + 8)];
            long pos = 0;

            ByteBuffer.wrap(chunk).order(ByteOrder.LITTLE_ENDIAN).putLong(0, size);

            long secSize;
            for (int i = 0; i < branchCnt; i++) {
                // the last item can have shorter data
                if (size-pos < treeSize) {
                    secSize = size - pos;
                } else {
                    secSize = treeSize;
                }
                // take the section of the data corresponding encoded in the subTree
                SectionReader subTreeData = data.slice((int)pos, (int) (pos + secSize));
                // the hash of that data
//                Key subTreeKey = new Key(Arrays.copyOfRange(chunk, 8 + i * hashSize, 8 + (i + 1) * hashSize));

                Key subTreeKey = splitImpl(depth-1, treeSize/branches, subTreeData, consumer);

                System.arraycopy(subTreeKey.getBytes(), 0, chunk, 8 + i * hashSize, hashSize);

                pos += treeSize;
            }
            // now we got the hashes in the chunk, then hash the chunk

            hash = new Key(hasher.digest(chunk));
            newChunk = new Chunk(hash, chunk, size);
        }

        consumer.add(newChunk);
        // report hash of this chunk one level up (keys corresponds to the proper subslice of the parent chunk)x
        return hash;

    }

    @Override
    public SectionReader join(Collection<Chunk> chunks) {
        return null;
    }

    @Override
    public long keySize() {
        return hashSize;
    }
}

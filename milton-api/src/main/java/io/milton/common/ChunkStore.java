package io.milton.common;

public interface ChunkStore extends ChunkWriter {

    /** retrieve the chunk at position i (zero indexed)
     */
    byte[] getChunk(int i);
    
    /**
     * delete any chunks which might exist
     */
    void deleteChunks();
}

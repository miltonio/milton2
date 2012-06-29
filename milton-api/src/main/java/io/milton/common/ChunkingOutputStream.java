package io.milton.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ChunkingOutputStream extends  OutputStream{
        
    final ChunkWriter chunkWriter;
    final ByteArrayOutputStream chunkInProgress;
    final int chunkSize;
    
    private long count;
    private int currentChunk;
        
    public ChunkingOutputStream(ChunkWriter chunkWriter, int chunkSize) {
        this.chunkWriter = chunkWriter;
        this.chunkSize = chunkSize;
        this.chunkInProgress = new ByteArrayOutputStream(chunkSize);
    }

    @Override
    public void write(int b) throws IOException {        
        count++;
        if( chunkInProgress.size() >= chunkSize ) {
            flushChunk();
        }
        chunkInProgress.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {        
        if( chunkInProgress.size() >= chunkSize ) {
            flushChunk();
        }
        count+=len;
        chunkInProgress.write(b, off, len);
    }
    
    

    private void flushChunk() {
        if( chunkInProgress.size() == 0 ) return ;
        byte[] arr = chunkInProgress.toByteArray();
        chunkWriter.newChunk(currentChunk++,arr);
        chunkInProgress.reset();
    }

    @Override
    public void flush() throws IOException {
        flushChunk();
    }    
}

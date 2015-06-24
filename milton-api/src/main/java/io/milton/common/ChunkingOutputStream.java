/*
 *
 * Copyright 2014 McEvoy Software Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

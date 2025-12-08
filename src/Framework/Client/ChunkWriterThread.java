package Framework.Client;

import Framework.Domain.*;
import java.io.*;
import java.util.concurrent.CountDownLatch;


/**
 * Writes a model chunk to disk at a specific file position.
 * 
 * Executes concurrently with other chunk writers to assemble a complete model file
 * downloaded from the server. Uses {@link CountDownLatch} to synchronize completion.
 * 
 */
public class ChunkWriterThread implements Runnable {

    private final ModelChunk chunk;
    private final RandomAccessFile raf;
    private final CountDownLatch latch;

    /**
     * Constructs a ChunkWriterThread with chunk data and file references.
     * 
     * @param chunk the model chunk containing data and position information
     * @param raf the RandomAccessFile to write the chunk to
     * @param latch the CountDownLatch to signal when writing is complete
     */
    public ChunkWriterThread(ModelChunk chunk, RandomAccessFile raf, CountDownLatch latch) {
        this.chunk = chunk;
        this.raf = raf;
        this.latch = latch;
    }

    /**
     * Writes the model chunk data to the file at the specified position.
     * 
     * This method:
     * - Seeks to the chunk's start position in the RandomAccessFile
     * - Writes the chunk data to disk
     * - Handles any IOException that may occur during writing
     * - Signals completion via CountDownLatch to allow synchronization with other chunk writers
     */
    public void run() {

        try {
            raf.seek(chunk.getStartPosition());
            raf.write(chunk.getData());
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }
}
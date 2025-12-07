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
 * @author Isaac Terés Espallargas
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
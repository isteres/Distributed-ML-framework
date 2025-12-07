package Framework.Client;

import Framework.Domain.*;
import java.io.*;
import java.util.concurrent.CountDownLatch;

public class ChunkWriterThread implements Runnable {

    private final ModelChunk chunk;
    private final RandomAccessFile raf;
    private final CountDownLatch latch;

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
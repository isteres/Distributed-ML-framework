package Framework.Server;

import Framework.Domain.*;
import java.io.*;
import java.util.concurrent.*;

/**
 * Streams a model file to a connected client in chunks.
 *
 * The thread sends file metadata (size and chunk count) and then reads the
 * model file in parallel into chunks, placing them on a queue beefore
 * transmission to the client via the provided ObjectOutputStream.
 */
public class ModelSenderThread implements Runnable {

    private final String modelPath;
    private final ObjectOutputStream oos;
    private final int numChunks;

    /**
     * Constructs a ModelSenderThread.
     *
     * @param modelPath filesystem path to the model file to send
     * @param oos ObjectOutputStream connected to the client
     * @param numChunks number of chunks to split the file into for parallel read
     */
    public ModelSenderThread(String modelPath, ObjectOutputStream oos, int numChunks) {
        this.modelPath = modelPath;
        this.oos = oos;
        this.numChunks = numChunks;
    }

    /**
     * Reads the model file and sends its chunks to the client.
     *
     * Behavior:
     * - If the file does not exist, sends -1 as file size.
     * - Otherwise sends file size and number of chunks, then reads the file in
     *   parallel into chunk objects and transmits them, no matter the order.
     */
    public void run() {
        File modelFile = new File(modelPath);

        if (!modelFile.exists()) {
            try {
                oos.writeLong(-1);
                oos.flush();
            } catch (IOException e) { e.printStackTrace(); }
            return;
        }

        long fileSize = modelFile.length();

        try {
            // Send metadata
            oos.writeLong(fileSize);
            oos.writeInt(numChunks);
            oos.flush();

            ExecutorService executor = Executors.newFixedThreadPool(numChunks);
            long chunkSize = fileSize / numChunks;
            
            BlockingQueue<ModelChunk> chunkQueue = new ArrayBlockingQueue<>(numChunks);

            // Parallel reading
            for (int i = 0; i < numChunks; i++) {
                final int idx = i;
                final long start = i * chunkSize;
                final long end = (i == numChunks - 1) ? fileSize : (i + 1) * chunkSize;

                // Anonymous class
                executor.execute(new Runnable() {
                    public void run() {
                    try (RandomAccessFile raf = new RandomAccessFile(modelFile, "r")) {
                        byte[] buffer = new byte[(int)(end - start)];
                        raf.seek(start);
                        raf.readFully(buffer);
                        chunkQueue.put(new ModelChunk(idx, start, buffer)); 

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    }
                });
            }

            for (int i = 0; i < numChunks; i++) {
                ModelChunk chunk = chunkQueue.take(); 
                oos.writeObject(chunk);
                oos.flush();
            }

         
            executor.shutdown();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
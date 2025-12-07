package Framework.Server;

import Framework.Domain.*;
import java.io.*;
import java.util.concurrent.*;

public class ModelSenderThread implements Runnable {

    private final String modelPath;
    private final ObjectOutputStream oos;
    private final int numChunks;

    public ModelSenderThread(String modelPath, ObjectOutputStream oos, int numChunks) {
        this.modelPath = modelPath;
        this.oos = oos;
        this.numChunks = numChunks;
    }

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
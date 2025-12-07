package Framework.Client;

import Framework.Domain.ModelChunk;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

public class ModelDownloaderThread implements Runnable {

    private final String serverHost;
    private final int serverPort;
    private final String userID;
    private final String modelName;
    private final String destinationPath;

    public ModelDownloaderThread(String serverHost, int serverPort, String userID, String modelName, String destinationPath) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.userID = userID;
        this.modelName = modelName;
        this.destinationPath = destinationPath;
    }

    public void run() {
        // Create another connection for downloading
        try (Socket downloadSocket = new Socket(serverHost, serverPort);
             ObjectOutputStream oos = new ObjectOutputStream(downloadSocket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(downloadSocket.getInputStream())) {

            // Send download session request
            oos.writeBytes("DOWNLOAD_SESSION\r\n");
            oos.writeBytes(userID + "\r\n");
            oos.writeBytes(modelName + "\r\n");
            oos.flush();

            // Receive file metadata
            long fileSize = ois.readLong();
            
            if (fileSize <= 0) {
                System.out.println("\n[DOWNLOAD] Model not found on server.");
                return;
            }

            int numChunks = ois.readInt();

            // Create destination file
            File destDir = new File(destinationPath);
            if (!destDir.exists()) destDir.mkdirs();
            File destFile = new File(destDir, modelName+"_model.pkl");

            ExecutorService writerPool = Executors.newFixedThreadPool(numChunks);

            try (RandomAccessFile raf = new RandomAccessFile(destFile, "rw")) {
                raf.setLength(fileSize);

                CountDownLatch writeLatch = new CountDownLatch(numChunks);

                // Receive and write chunks in parallel
                for (int i = 0; i < numChunks; i++) {
                    ModelChunk chunk = (ModelChunk) ois.readObject();
                    writerPool.execute(new ChunkWriterThread(chunk, raf, writeLatch));
                }

                writeLatch.await();
                
            }

            writerPool.shutdown();


        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            System.out.println("\n[DOWNLOAD] Error: " + e.getMessage());
        }
    }
}
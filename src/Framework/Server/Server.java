package Framework.Server;

import Framework.Persistence.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

// In this class we find the Server that will be waiting connections. It will
// distribute every connection in a different thread in order to improve the efficiency
public class Server {

    public static final int SERVER_PORT = 16666;
    // The first version of the framework will have fixed datasets
    public static final List<String> datasets = getDatasetFiles();
    // Initialize the server database
    private static final ServerDatabase serverDatabase = ServerDatabase.getInstance();
    // Connected users
    private static final List<String> connectedUsers = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        
        ExecutorService pool = Executors.newCachedThreadPool();

        try (ServerSocket server = new ServerSocket(16666)) {
            System.out.println("Server listening in the port " + SERVER_PORT);

            while (true) {
                try {
                    Socket client = server.accept();

                    pool.execute(new ConnectionHandler(client, pool, datasets, serverDatabase, connectedUsers));

                } catch (IOException excpClient) {
                    excpClient.printStackTrace();
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }

    }

    private static List<String> getDatasetFiles() {
        // Returns a list with all the files located in the directory "Datasets"
        File dir = new File("Datasets");
        List<String> list = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) {
            System.err.println("[SERVER] Datasets directory not found!");
            return list;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    list.add(f.getName());
                }
            }
        }

        return list;
    }

}

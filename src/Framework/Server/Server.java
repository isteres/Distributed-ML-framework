package Framework.Server;

import Framework.Persistence.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

// In this class we find the Server that will be waiting connections. It will
// distribute every connection in a different thread in order to improve the efficiency
public class Server {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    public static final int SERVER_PORT = 16666;
    // The first version of the framework will have fixed datasets
    public static final List<String> datasets = Server.getDatasetFiles();
    // Initialize the server database
    private static final ServerDatabase serverDatabase = ServerDatabase.getInstance();
    // Connected users
    private static final List<String> connectedUsers = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        
        // Set locale to English for logging
        Locale.setDefault(Locale.ENGLISH);
        
        ExecutorService pool = Executors.newCachedThreadPool();
        Timer dailyTimer = new Timer();

        try (ServerSocket server = new ServerSocket(16666)) {
            LOGGER.info("[SERVER] Listening on port " + SERVER_PORT);

            dailyTimer.scheduleAtFixedRate(new DailyTrainingTask("dataset10000.xml"), 0, 124 * 60 * 60 * 1000);
            LOGGER.info("[SERVER] Daily training task scheduled.");
            
            while (true) {
                try {
                    Socket client = server.accept();
                    LOGGER.info("[SERVER] New connection from " + client.getInetAddress() + ":" + client.getPort());

                    pool.execute(new ConnectionHandler(client, pool, datasets, serverDatabase, connectedUsers));

                } catch (IOException excpClient) {
                    LOGGER.severe("[SERVER] Client connection error: " + excpClient.getMessage());
                }

            }

        } catch (IOException e) {
            LOGGER.severe("[SERVER] Error: " + e.getMessage());
        } finally {
            dailyTimer.cancel();
            pool.shutdown();
        }

    }

    public static List<String> getAvailableModels(String userID) {
        List<String> allModels = new ArrayList<>();
        
        // Always add server models
        allModels.addAll(getModelsFromDirectory("SERVER"));
        
        // Add user-specific models if different from SERVER
        if (userID != null && !userID.equals("SERVER")) {
            allModels.addAll(getModelsFromDirectory(userID));
        }
        
        return allModels;
    }

 
    private static List<String> getModelsFromDirectory(String ownerID) {
        List<String> models = new ArrayList<>();
        File modelDir = new File("TrainedModels", ownerID);
        
        if (!modelDir.exists() || !modelDir.isDirectory()) {
            return models;
        }
        
        // Filter only .pkl model files, maybe in the future we add different file types to the directories
        File[] files = modelDir.listFiles((dir, name) -> name.endsWith("_model.pkl"));
        if (files != null) {
            for (File file : files) {
                String modelName = file.getName().replace("_model.pkl", "");
                models.add(modelName);
            }
        }
        
        return models;
    }

    public static String getModelPath(String modelName, String userID) {
        if (modelName.startsWith("Server_")) {
            return "TrainedModels/SERVER/" + modelName + "_model.pkl";
        }
        return "TrainedModels/" + userID + "/" + modelName + "_model.pkl";
    }


    private static List<String> getDatasetFiles() {
        // Returns a list with all the files located in the directory "Datasets"
        File dir = new File("Datasets");
        List<String> list = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) {
            LOGGER.severe("[SERVER] Datasets directory not found!");
            return list;
        }

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isFile() && !f.getName().endsWith(".dtd")) {
                    list.add(f.getName());
                }
            }
        }

        return list;
    }

}
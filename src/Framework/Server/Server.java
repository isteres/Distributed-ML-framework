package Framework.Server;

import Framework.Persistence.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;


/**
 * Central server entry point for the Distributed ML Framework.
 *
 * Responsibilities:
 * - Listen for incoming client connections and dispatch handlers using a thread pool
 * - Maintain a list of connected users and access to the server-side database
 * - Schedule recurring tasks such as daily model training
 * - Provide utility methods to list available models, construct model paths
 *   and enumerate available datasets on disk
 *
 * The server uses a cached thread pool for handling each connection in a
 * separate thread and relies on the `ServerDatabase` singleton to persist
 * metadata about users and trained models.
 */
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

    /**
     * Returns a list of available model names for the given user.
     * Server-owned models are always included and user-specific models are
     * appended when the user is not the special SERVER account.
     *
     * @param userID owner identifier to include user-specific models (may be null)
     * @return list of available model names
     */
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

 
    /**
     * Reads the models directory for the provided owner and returns model
     * names found in that directory. Only files matching the pattern
     * "*_model.pkl" are considered and returned without the suffix.
     *
     * @param ownerID directory name representing the owner of models
     * @return list of model base names for the owner
     */
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

    /**
     * Builds and returns the filesystem path to a model file given its name
     * and the owning user. Server models are stored under the special
     * "SERVER" folder and prefixed with "Server_" when appropriate.
     *
     * @param modelName the model's base name (without suffix)
     * @param userID the owner user id used when model is not a server model
     * @return the platform-relative path to the model file
     */
    public static String getModelPath(String modelName, String userID) {
        if (modelName.startsWith("Server_")) {
            return "TrainedModels\\SERVER\\" + modelName + "_model.pkl";
        }
        return "TrainedModels\\" + userID + "\\" + modelName + "_model.pkl";
    }


    /**
     * Scans the `Datasets` directory and returns a list of dataset file names
     * excluding DTD files. If the directory is missing, an empty list is
     * returned and an error is logged.
     *
     * @return list of dataset filenames present in the `Datasets` directory
     */
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
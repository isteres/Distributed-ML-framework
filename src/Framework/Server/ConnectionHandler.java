package Framework.Server;

import Framework.Domain.*;
import Framework.Persistence.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ConnectionHandler implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ConnectionHandler.class.getName());

    private Socket client;
    private String userID;
    private ExecutorService pool;
    private List<String> datasets;
    private ServerDatabase serverDatabase;
    // The same user cannnot be connected from different devices at the same time
    private List<String> connectedUsers;

    public ConnectionHandler(Socket s, ExecutorService p, List<String> d, ServerDatabase db, List<String> connectedUsers) {
        this.client = s;
        this.userID = null;
        this.pool = p;
        this.datasets = d;
        this.serverDatabase = db;
        this.connectedUsers = connectedUsers;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;    
    }

    public void manageConnectedUser(String userID, ObjectOutputStream oos, ObjectInputStream ois) {
        if (connectedUsers.contains(userID)) {
            LOGGER.warning("[" + userID + "] User already connected from another device.");
            try {
                oos.writeBytes("User already connected from another device.\r\n");
                oos.flush();
                manageDisconnectedUser(ois, oos);
            } catch (IOException e) {
                LOGGER.severe("[" + userID + "] Error: " + e.getMessage());
            }
            return;
        }

        setUserID(userID);
        connectedUsers.add(userID);
        serverDatabase.registerUser(userID);

        try {
            oos.writeBytes("User " + userID + " signed in successfully.\r\n");
            oos.writeBytes("\r\n");
            oos.flush();
        } catch (IOException e) {
            LOGGER.severe("[" + userID + "] Error: " + e.getMessage());
        }

        LOGGER.info("[" + userID + "] Signed in successfully.");
    }

    public void manageDisconnectedUser(ObjectInputStream oin, ObjectOutputStream oout) {
        try {
            // This will manage the Socket closing too
            if (oin != null) {
                oin.close();
            }
            if (oout != null) {
                oout.close();
            }
            // Delete the user from the connected users list
            if (this.getUserID() != null) {
                connectedUsers.remove(this.getUserID());
                LOGGER.info("[" + this.getUserID() + "] Disconnected.");
            }

        } catch (IOException e) {
            LOGGER.severe("Error closing streams: " + e.getMessage());
        }
    }
    

    public void run() {

        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            
            oos = new ObjectOutputStream(client.getOutputStream());
            ois = new ObjectInputStream(client.getInputStream());

            while (!this.client.isClosed()) {

                String line = ois.readLine();
                if (line == null) {
                    LOGGER.info("[" + this.getUserID() + "] Connection closed from " + 
                        this.client.getInetAddress() + ":" + this.client.getPort());
                    break;
                }

                switch (line) {

                    case "SIGN_IN":
                        String userID = ois.readLine();
                        manageConnectedUser(userID, oos, ois);
                        break;

                    case "INSERT_DATASET":
                        // Send the list of datasets
                        oos.writeObject(datasets);
                        oos.flush();
                        // Just in case in the future the datasets change between requests
                        oos.reset();

                        // Receive the request 
                        DatasetInsertRequest direquest = (DatasetInsertRequest) ois.readObject();
                        this.pool.execute(new DatasetInserterThread(direquest));
                        LOGGER.info("[" + this.getUserID() + "] Inserting record in " + direquest.getDatasetName());

                        oos.writeBytes("Inserting record in " + direquest.getDatasetName() + "...\r\n");
                        oos.flush();
                        break;

                    case "TRAIN_MODEL":
                        // Send the list of datasets
                        oos.writeObject(datasets);
                        oos.flush();
                        oos.reset();

                        TrainingRequest tr = (TrainingRequest) ois.readObject();
                        this.pool.execute(new ModelTrainerThread(tr, this.getUserID()));
                        LOGGER.info("[" + this.getUserID() + "] Training model: " + tr.getModelName());

                        oos.writeBytes("Training model over " + tr.getDatasetUsed() + "...\r\n");
                        oos.flush();
                        break;

                    case "STUDENT_INFERENCE":
                        // Send the list of available models to the user
                        List<String> availableModels = Server.getAvailableModels(this.getUserID());
                        oos.writeObject(availableModels);
                        oos.flush();
                        oos.reset();

                        InferenceRequest pr = (InferenceRequest) ois.readObject();
                        LOGGER.info("[" + this.getUserID() + "] Inference request using: " + pr.getModelName());
                        
                        Future<Float> futureSalary = this.pool.submit(new InferenceThread(pr, this.getUserID()));   
                        
                        try {
                            Float predictedSalary = futureSalary.get(30, TimeUnit.SECONDS);
                            oos.writeBytes("Predicted Salary: " + predictedSalary + "\r\n");
                            oos.flush();    

                        } catch (TimeoutException e) {
                            futureSalary.cancel(true);
                            LOGGER.warning("[" + this.getUserID() + "] Inference timed out.");
                            oos.writeBytes("Inference timed out.\r\n");
                            oos.flush();
                        } catch(ExecutionException e) {
                            LOGGER.severe("[" + this.getUserID() + "] Inference error: " + e.getMessage());
                            oos.writeBytes("Server error inferencing your salary.\r\n");
                            oos.flush();
                        } catch(InterruptedException e) {
                            futureSalary.cancel(true);
                            LOGGER.warning("[" + this.getUserID() + "] Inference interrupted.");
                            oos.writeBytes("Inference interrupted.\r\n");
                            oos.flush();
                        }
                        break;

                    case "LIST_MODELS":
                        List<String> availables = Server.getAvailableModels(this.getUserID());
                        oos.writeObject(availables);
                        oos.flush();
                        oos.reset();
                        break;

                    case "DOWNLOAD_SESSION":
                        // Special session
                        String downloadUserID = ois.readLine();
                        String modelName = ois.readLine();
                        
                        LOGGER.info("[" + downloadUserID + "] Download request: " + modelName);
                        
                        String modelPath = Server.getModelPath(modelName, downloadUserID);
                        
                        // Send model synchronously (this connection is dedicated to download)
                        ModelSenderThread sender = new ModelSenderThread(modelPath, oos, 4);
                        try {
                            this.pool.submit(sender).get();
                            LOGGER.info("[" + downloadUserID + "] Download completed: " + modelName);
                        } catch (ExecutionException | InterruptedException e) {
                            LOGGER.severe("[" + downloadUserID + "] Download failed: " + e.getMessage());
                        }
                        // Finish the download session and close the sockets 
                        return;
                }
            }

        } catch(SocketException e) {
            LOGGER.info("[" + this.getUserID() + "] Connection closed abruptly.");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("ClassNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.severe("IOException: " + e.getMessage());
        } finally {
            manageDisconnectedUser(ois, oos);
        }
    }
}
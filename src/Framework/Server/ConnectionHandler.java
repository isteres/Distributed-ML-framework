package Framework.Server;

import Framework.Domain.*;
import Framework.Persistence.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;

public class ConnectionHandler implements Runnable {

    private Socket client;
	private String userID;
    private ExecutorService pool;
    private List<String> datasets;
    private ServerDatabase serverDatabase;
	// The same user can be connected from different devices at the same time
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
            System.out.println("[WARNING] User already connected: " + userID);
            try {
                oos.writeBytes("User already connected from another device.\r\n");
                oos.flush();
                manageDisconnectedUser(ois, oos);
            } catch (IOException e) {
                e.printStackTrace();
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
            e.printStackTrace();
        }

        System.out.println("[DATABASE] User " + userID + " signed in successfully.");

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
            }

        } catch (IOException e) {
            e.printStackTrace();
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
                    System.out.println("[INFO] " + this.getUserID() + ", connected from " + 
					this.client.getInetAddress() + ":" + this.client.getPort() + " disconnected.");
                    break;
                }

                switch (line) {

                    case "SIGN_IN":
                        String userID = ois.readLine();
						manageConnectedUser(userID, oos,ois );
				
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
                        Future<Float> futureSalary = this.pool.submit(new InferenceThread(pr, this.getUserID()));   
                        
                        try {
                            Float predictedSalary = futureSalary.get(30, TimeUnit.SECONDS);
                            oos.writeBytes("Predicted Salary: " + predictedSalary + "\r\n");
                            oos.flush();    

                        } catch (TimeoutException e) {
                            futureSalary.cancel(true);
                            oos.writeBytes("Inference timed out.\r\n");
                            oos.flush();
                        } catch(ExecutionException e) {
                            System.out.println(e.getMessage());
                            oos.writeBytes("Server error inferencing your salary.\r\n");
                            oos.flush();

                        } catch(InterruptedException e) {
                            // Send interruption to the inference thread
                            futureSalary.cancel(true);
                            oos.writeBytes("Inference interrupted.\r\n");
                            oos.flush();
                        }


                        break;
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            manageDisconnectedUser(ois, oos);
        }

    }

}

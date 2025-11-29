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

	public void manageConnectedUser(String userID) {
		setUserID(userID);
		connectedUsers.add(userID);
    	serverDatabase.registerUser(userID);
	}

    public void run() {

        ObjectOutputStream oout = null;
        ObjectInputStream oin = null;

        try {
            oout = new ObjectOutputStream(client.getOutputStream());
            oin = new ObjectInputStream(client.getInputStream());

            // Wait to  the client to close the connection(when he "exits")
            while (!this.client.isClosed()) {

                String line = oin.readLine();
                if (line == null) {
                    System.out.println("[INFO] " + this.getUserID() + ", connected from " + 
					this.client + this.client.getInetAddress() + ":" + this.client.getPort() + " disconnected.");
                    break;
                }

                switch (line) {

                    case "SIGN_IN":
                        String userID = oin.readLine();
						manageConnectedUser(userID);
				
                        break;

                    case "INSERT_DATASET":
                        // Send the list of datasets
                        oout.writeObject(this.datasets);
                        oout.flush();
                        // Not necessity of reset, it'll only be used once

                        // Receive the request 
                        DatasetInsertRequest direquest = (DatasetInsertRequest) oin.readObject();
                        this.pool.execute(new DatasetInserterThread(direquest));

                        oout.writeBytes("Inseting record in " + direquest.getDatasetName() + "...\n");
                        oout.flush();

                        break;

                    case "TRAIN_MODEL":
                        // Send the list of datasets
                        oout.writeObject(this.datasets);
                        oout.flush();

                        TrainingRequest tr = (TrainingRequest) oin.readObject();

                        this.pool.execute(new ModelTrainerThread(tr));

						oout.writeBytes("Training model over " + tr.getDatasetUsed() + "...\n");
                        oout.flush();

                        break;

                    case "STUDENT_INFERENCE":
                        // Hacerlo con Future
                        break;

					
                }
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
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

    }

}

package Framework.Server;

import Framework.Domain.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.*;

public class ConnectionHandler implements Runnable{
	private Socket client;
	private ExecutorService pool;
	private List<String> datasets;
	
	public ConnectionHandler(Socket s, ExecutorService p, List<String> d) {
		this.client=s;
		this.pool=p;		
		this.datasets=d;
	}
	
	public void run() {
		
		ObjectOutputStream oout = null;
		ObjectInputStream oin = null;
		
		try {
			oout = new ObjectOutputStream(client.getOutputStream());
			oin = new ObjectInputStream(client.getInputStream());

			// Wait to  the client to close the connection(when he "exits")
			while(!this.client.isClosed()) {
				
				String line = oin.readLine();
				if(line == null) {
					System.out.println("[INFO] Client "+ this.client.getInetAddress() + " disconnected.");
					break;
				}
				switch (line) {
				
					case "INSERT_DATASET":
						// Send the list of datasets
						oout.writeObject(this.datasets);
						oout.flush();
						// Not necessity of reset, it'll only be used once
						
						// Receive the request 
						DatasetInsertRequest direquest = (DatasetInsertRequest) oin.readObject();
						this.pool.execute(new DatasetInserterThread(direquest));

						oout.writeBytes("Inseting record in "+direquest.getDatasetName()+"...\n");
						oout.flush();
						
						break;
						
					case "TRAIN_MODEL":
						// Send the list of datasets
						oout.writeObject(this.datasets);
						oout.flush();
						
						TrainingRequest tr = (TrainingRequest) oin.readObject();
						
						this.pool.execute(new ModelTrainerThread(tr));

						break;
						
					case "STUDENT_INFERENCE":
						// Hacerlo con Future
						break;
				}	
				}
				
				
		
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			try {
				// This will manage the Socket closing too
				if(oin!=null) oin.close();
				if(oout!=null) oout.close();
				
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	
	

}

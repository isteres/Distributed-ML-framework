package Framework.Server;

import Framework.Domain.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ConnectionHandler implements Runnable{
	private Socket client;
	private ExecutorService pool;
	
	public ConnectionHandler(Socket s, ExecutorService p) {
		this.client=s;
		this.pool=p;		
	}
	
	public void run() {
		
		ObjectOutputStream oout = null;
		ObjectInputStream oin = null;
		
		try {
			oout = new ObjectOutputStream(client.getOutputStream());
			oin = new ObjectInputStream(client.getInputStream());

			// Wait to  the client to close the connection(when he "exists")
			//while(!client.isClosed()){
			while(!this.client.isClosed()) {
				
				String line = oin.readLine();
					
					switch (line) {
					
						case "INSERT_DATASET":
							// Obtain and send the list of datasets
							List<String> datasets = getDatasetFiles();
							oout.writeObject(datasets);
							oout.flush();
							// Not necessity of reset, it'll only be used once
							
							// Receive the request 
							DatasetInsertRequest direquest = (DatasetInsertRequest) oin.readObject();
							this.pool.execute(new DatasetInserterThread(direquest));

							oout.writeBytes("Inseting record in "+direquest.getDatasetName()+"...\n");
							oout.flush();
							
							break;
							
						case "MODEL_TRAINING":
							
							break;
						case "STUDENT_INFERENCE":
							
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
	
	
	private List<String> getDatasetFiles() {
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

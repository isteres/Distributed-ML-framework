package Framework.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import Framework.Domain.*;
import java.io.*;
import java.net.Socket;

public class ConnectionHandler implements Runnable{
	private Socket client;
	
	public ConnectionHandler(Socket s) {
		this.client=s;
	}
	
	public void run() {
		
		try {
			ObjectOutputStream oout = new ObjectOutputStream(this.client.getOutputStream());
			ObjectInputStream oin = new ObjectInputStream(this.client.getInputStream());
			
			String line = oin.readLine();
			
			if(line.equals("INSERT_DATASET")) {
				// Obtain and send the list of datasets
				List<String> datasets = getDatasetFiles();
				oout.writeObject(datasets);
				oout.flush();
				
				// Receive the request
				DatasetInsertRequest di = (DatasetInsertRequest) oin.readObject();
				
				
				oout.writeBytes("Inseting record in "+di.getDatasetName()+"...");
				oout.flush();
			}
			
			
			
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private List<String> getDatasetFiles() {
	    File dir = new File("Datasets");   // relative path to your datasets folder
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

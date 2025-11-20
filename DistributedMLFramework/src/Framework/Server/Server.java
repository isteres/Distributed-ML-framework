package Framework.Server;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

// In this class we find the Server that will be waiting petitions. It will
// distribute the tasks in different threads in order to improve the efficiency
public class Server {
	
	public static final int SERVER_PORT = 16666;
	
	public static void main(String[] args) {
		//Cached pool that will manage the different tasks
		ExecutorService pool = Executors.newCachedThreadPool();
		
		try(ServerSocket server = new ServerSocket(16666)){
			System.out.println("Server listening in the port "+SERVER_PORT);
			
			while(true) {
				try {
					Socket client = server.accept();
					
					// The ConnectionHandler will manage all the connections
					pool.execute(new ConnectionHandler(client,pool));
					
				}catch(IOException excpClient) {
					excpClient.printStackTrace();
				}
				
				
			}

		}catch(IOException e) {
			e.printStackTrace();
		}finally {
			pool.shutdown();
		}
		
	}

}

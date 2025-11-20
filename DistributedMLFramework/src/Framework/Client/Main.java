package Framework.Client;

public class Main {
	// The access to the functionalities of the framework
    public static void main(String[] args) {
    	System.out.println("Working directory: " + System.getProperty("user.dir"));

        Client client = new Client();
        client.initialize();
    }
    
}

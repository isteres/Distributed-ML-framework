package Framework.Client;

import java.util.Scanner;


/**
 * Entry point for the Distributed ML Framework client application.
 * 
 * Initializes the client interface, handles user authentication, and starts the main client loop.
 * 
 * @author Isaac Terés Espallargas
 */
public class Main {
    // The access to the functionalities of the framework

    public static void main(String[] args) {
        printBanner();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your user ID: ");
        String userID = scanner.nextLine().trim();
        while (userID.isEmpty()) {
            System.out.print("User ID cannot be empty. Enter userID again: ");
            userID = scanner.nextLine().trim();
        }
        Client client = new Client(userID);
        client.initialize();
    }

    /**
     * Prints the welcome banner and framework information.
     */
    private static void printBanner() {

        System.out.println("=========================================================================\r\n");
        System.out.println("      Welcome to the Console Interface of the Distributed ML Framework   \r\n");
        System.out.println("  Contribute data | Train ML models | Inference results | Download models\r\n");
        System.out.println("         Salary prediction over graduated, master and PhD students       \r\n");
        System.out.println("=========================================================================\r\n");
        System.out.println("  If you have used the framework before, you will be");
        System.out.println("  automatically signed in with your previous userID.\r\n");
        System.out.println("  If this is your first time, you can choose any userID.\r\n");

    }

}

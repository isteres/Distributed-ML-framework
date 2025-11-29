package Framework.Client;

import java.util.Scanner;

public class Main {
    // The access to the functionalities of the framework

    public static void main(String[] args) {
        printBanner();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your user ID: ");
        String userid = scanner.nextLine();

        Client client = new Client(userid);
        client.initialize();
    }

    private static void printBanner() {

        System.out.println("====================================================================\n");
        System.out.println("  Welcome to the Console Interface of the Distributed ML Framework  \n");
        System.out.println("        Contribute data | Train ML models | Inference results       \n");
        System.out.println("      Salary prediction over graduated, master and PhD students     \n");
        System.out.println("====================================================================\n");
        System.out.println(" GitHub: isaactesp\n\n");

    }

}

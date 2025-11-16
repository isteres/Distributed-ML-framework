package Framework.Client;

import Framework.Domain.*;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class Client {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 16666;

    private Scanner sc;

    public Client() {
        this.sc = new Scanner(System.in);
    }

    public void initialize() {

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
        	 // Highlight the OOS initialized before the IIS to avoid deadlocks
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            boolean exit = false;

            while (!exit) {
                printMenu();
                String option = sc.nextLine().trim();

                switch (option) {
                    case "1":
                        StudentWithSalary student = fillStudentForm();
                        sendRecordToServer(oos, ois, student);
                        break;

                    case "2":
                        System.out.println("[INFO] Train model (TODO)");
                        break;

                    case "3":
                        System.out.println("[INFO] Predict (TODO)");
                        break;

                    case "0":
                        System.out.println("[INFO] Closing client...");
                        exit = true;
                        break;

                    default:
                        System.out.println("[WARNING] Invalid option. Try again.");
                }
            }

        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void printMenu() {
        System.out.println("\n========== CLIENT MENU ==========");
        System.out.println("1) Fill a form with your experience and help us to improve our datasets.");
        System.out.println("2) Train model        [TODO]");
        System.out.println("3) Make prediction    [TODO]");
        System.out.println("0) Exit");
        System.out.print("Select an option: ");
    }



    // ============================================================
    //                   INSERT DATASET (TEXT INPUT)
    // ============================================================
  
    private StudentWithSalary fillStudentForm() {
        System.out.println("\n--- Fill StudentWithSalary Form ---");

        System.out.print("Country (Brazil, China, Spain, Pakistan, USA, India, Vietnam, Nigeria): ");
        String country = sc.nextLine().trim();

        System.out.print("Gender (Male, Female, Other): ");
        String gender = sc.nextLine().trim();

        System.out.print("Educational Level (Bachelor, Master, PhD, Diploma): ");
        String educationalLevel = sc.nextLine().trim();

        System.out.print("Field of Study (Arts, Engineering, IT, Health, Social Sciences, Business): ");
        String fieldOfStudy = sc.nextLine().trim();

        System.out.print("English Proficiency (Basic, Intermediate, Advanced, Fluent): ");
        String englishProficiency = sc.nextLine().trim();

        System.out.print("Internship Experience (Yes, No): ");
        String internshipExperience = sc.nextLine().trim();

        System.out.print("GPA (0–10): ");
        float gpa = readFloat();

        System.out.print("Age: ");
        int age = readInt();

        System.out.print("Salary: ");
        int salary = readInt();

        // Construct the student 
        return new StudentWithSalary(
    		country,
            gender,
            educationalLevel,
            fieldOfStudy,
            englishProficiency,
            internshipExperience,
            gpa,
            age,
            salary
        );
    }



    // ============================================================
    //                    CLIENT-SERVER COMMUNICATION
    // ============================================================

    private void sendRecordToServer(ObjectOutputStream oos, ObjectInputStream ois, StudentWithSalary student){
    	try {
	        // Send command
	        oos.writeBytes("INSERT_DATASET\n");
	        oos.flush();
	        
	        System.out.println("Enter the name of the dataset you want to update: ");
	        System.out.println("(Datasets available)");
	        
	        // Check datasets available
	        List<String> datasets = (List<String>) ois.readObject();
	        for(String dataset : datasets) {
	        	System.out.println(dataset);
	        }
	        
	        String datasetName = sc.nextLine().trim().toLowerCase();
            DatasetInsertRequest di = new DatasetInsertRequest(datasetName,student);
	
	        // Send the DatasetInsertRequest object
	        oos.writeObject(di);
	        oos.flush();
	
	        // Read server response
	        String response = ois.readLine();
	        System.out.println("[SERVER] " + response);
	    
    	}catch(IOException e) {
    		e.printStackTrace();
    	}catch(ClassNotFoundException e) {
    		e.printStackTrace();
    	}
    	
    }


    // Validation methods to read integers and floats properly
    private float readFloat() {
        while (true) {
            try {
                return Float.parseFloat(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.print("Invalid number. Try again: ");
            }
        }
    }

    private int readInt() {
        while (true) {
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.print("Invalid integer. Try again: ");
            }
        }
    }
}

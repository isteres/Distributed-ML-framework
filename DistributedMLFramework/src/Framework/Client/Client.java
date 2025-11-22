package Framework.Client;

import Framework.Domain.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 16666;

    private Scanner sc;

    public Client() {
        this.sc = new Scanner(System.in);
    }

    public void initialize() {
        // Highlight the OOS initialized before the OIS to avoid deadlocks
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT); 
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); 
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());) {

            boolean exit = false;

            while (!exit) {
                printMenu();
                String option = sc.nextLine().trim();

                switch (option) {
                    case "1":
                        WorkerWithStudies student = fillStudentForm();
                        sendRecordToServer(oos, ois, student);
                        break;

                    case "2":
                        TrainingRequest tr = fillTrainingRequest();
                        sendTrainingRequestToServer(oos, ois, tr);

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
            System.err.println("[ERROR] " + e.getMessage() + "(the server might be down).");

        } finally {
            System.out.println("[INFO] Have a good one!");
        }
    }

    private void printMenu() {
        System.out.println("\n========== CLIENT MENU ==========");
        System.out.println("1) Fill a form with your experience and help us to improve our datasets.");
        System.out.println("2) Train machine learning model over chosen dataset and hyperparameters.");
        System.out.println("3) Do inference with one of our trained models.");
        System.out.println("0) Exit.");
        System.out.print("Select an option: ");
    }

    // ============================================================
    // TRAIN MODEL
    // ============================================================
    private TrainingRequest fillTrainingRequest() {
        System.out.println("\n--- Training Request Form ---");
        
        // Model selection with switch case
        System.out.println("Select the machine learning algorithm:");
        System.out.println("1) Random Forest Regressor");
        System.out.println("2) Gradient Boosting Regressor");
        System.out.println("3) Linear Regression");
        System.out.print("Enter option (1-3): ");
        
        
        String algorithm = "";
        boolean validOption = false;
        while(!validOption) {
        	String modelOption = sc.nextLine().trim();
            switch (modelOption) {
                case "1":
                    algorithm = "RandomForest";
                    validOption = true;
                    break;
                case "2":
                    algorithm = "GradientBoosting";
                    validOption = true;
                    break;
                case "3":
                    algorithm = "LinearRegression";
                    validOption = true;
                    break;
                default:
                    System.out.println("[WARNING] Invalid option. Please enter a valid option (1-3).");
            }
        }
        System.out.print("Enter model name to save the model: ");
        String modelName = sc.nextLine().trim();
        
        // Training parameters
        Map<String, String> hyperparameters = new HashMap<>();
        hyperparameters.put("algorithm", algorithm);
        
        // Test size
        System.out.print("Enter test size (0.0-1.0, default 0.2): ");
        String testSizeInput = sc.nextLine().trim();
        if (!testSizeInput.isEmpty()) {
            try {
                float testSize = Float.parseFloat(testSizeInput);
                if (testSize > 0 && testSize < 1) {
                    hyperparameters.put("test_size", testSizeInput);
                } else {
                    System.out.println("[WARNING] Invalid test size. Using default 0.2");
                }
            } catch (NumberFormatException e) {
                System.out.println("[WARNING] Invalid input. Using default test size 0.2");
            }
        }
        
        // Hyperparameters for ensemble models
        if (algorithm.equals("RandomForest") || algorithm.equals("GradientBoosting")) {
            System.out.println("\n--- Hyperparameters Configuration ---");
            
            System.out.print("Enter number of estimators (default 700): ");
            String nEstimators = sc.nextLine().trim();
            if (!nEstimators.isEmpty()) {
                try {
                    int n = Integer.parseInt(nEstimators);
                    if (n > 0) {
                        hyperparameters.put("n_estimators", nEstimators);
                    } else {
                        System.out.println("[WARNING] Invalid value. Using default 700");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Invalid input. Using default 700");
                }
            }
            
            System.out.print("Enter max depth (press Enter for None): ");
            String maxDepth = sc.nextLine().trim();
            if (!maxDepth.isEmpty()) {
                try {
                    int depth = Integer.parseInt(maxDepth);
                    if (depth > 0) {
                        hyperparameters.put("max_depth", maxDepth);
                    } else {
                        System.out.println("[WARNING] Invalid value. Using None");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Invalid input. Using None");
                }
            }
           
        } else {
            System.out.println("[INFO] Linear Regression has no hyperparameters to configure.");
        }
        
        return new TrainingRequest(modelName, hyperparameters);
    }

    private void sendTrainingRequestToServer(ObjectOutputStream oos, ObjectInputStream ois, TrainingRequest tr) {
        try {
            oos.writeBytes("TRAIN_MODEL\n");
            oos.flush();

            System.out.println("Over which dataset do you want to train the model?");
            System.out.println("(Datasets available)");
            List<String> datasets = (List<String>) ois.readObject();
            for (String dataset : datasets) {
                System.out.println(dataset);
            }
            String datasetName = sc.nextLine().trim().toLowerCase();
            tr.setDatasetUsed(datasetName);
            // The request is ready to be sent

            oos.writeObject(tr);
            oos.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // INSERT DATASET (TEXT INPUT)
    // ============================================================
    private WorkerWithStudies fillStudentForm() {
        WorkerWithStudies student = null;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.println("\n--- Please fill the next form with your data ---");

                System.out
                        .print("Insert your country (Brazil, China, Spain, Pakistan, USA, India, Vietnam, Nigeria): ");
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

                student = new WorkerWithStudies(
                        country,
                        gender,
                        educationalLevel,
                        fieldOfStudy,
                        englishProficiency,
                        internshipExperience,
                        gpa,
                        age,
                        salary);
                // At this point student has been created successfully
                validInput = true;

            } catch (IllegalArgumentException e) {
                System.out.println("[ERROR] You entered an invalid parameter. Please, fill the form again.\n");
            }
        }

        return student;
    }

    private void sendRecordToServer(ObjectOutputStream oos, ObjectInputStream ois, WorkerWithStudies student) {

        try {
            // Send command
            oos.writeBytes("INSERT_DATASET\n");
            oos.flush();

            System.out.println("Enter the name of the dataset you want to update: ");
            System.out.println("(Datasets available)");

            // Check datasets available
            List<String> datasets = (List<String>) ois.readObject();
            for (String dataset : datasets) {
                System.out.println(dataset);
            }

            String datasetName = sc.nextLine().trim().toLowerCase();
            DatasetInsertRequest di = new DatasetInsertRequest(datasetName, student);

            // Send the DatasetInsertRequest object and reset the stream
            oos.writeObject(di);
            oos.flush();
            oos.reset();

            // Read server response
            String response = ois.readLine();
            System.out.println("[SERVER] " + response);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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

package Framework.Client;

import Framework.Domain.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 16666;

    private Scanner sc;
    private String userID;

    public Client(String userID) {
        this.sc = new Scanner(System.in);
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public void initialize() {
        // Highlight the OOS initialized before the OIS to avoid deadlocks
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT); 
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); 
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());) {

            boolean exit = false;
            signIn(oos,ois);  

            while (!exit ) {
                printMenu();
                String option = sc.nextLine().trim();

                switch (option) {
                    case "1":
                        WorkerWithStudies worker = fillStudentForm();
                        sendRecordToServer(oos, ois, worker);
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

        }catch(RuntimeException e){
            System.err.println("[ERROR] " + e.getMessage());
        } 
        catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage() + "(the server might be down).");
        } finally {
            System.out.println("[INFO] Have a good one!");
        }
    }

    private void printMenu() {
        System.out.println("\n================================ CLIENT MENU ================================");
        System.out.println("1) Fill a form with your experience and help us to improve our datasets.");
        System.out.println("2) Train machine learning model over chosen dataset and hyperparameters.");
        System.out.println("3) Do inference with one of our trained models.");
        System.out.println("0) Exit.");
        System.out.print("Select an option: ");
    }

    private void signIn(ObjectOutputStream oos, ObjectInputStream ois) throws RuntimeException {
        try {
            oos.writeBytes("SIGN_IN\n");
            oos.writeBytes(this.getUserID() + "\n");
            oos.flush();
            System.out.println(ois.readLine());
            // Read the extra newline sent by server, would be null if the user tries to sign in from two devices
            if(ois.readLine() == null) {
                throw new RuntimeException("User already signed in from another device.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // TRAIN MODEL
    // ============================================================

    private TrainingRequest fillTrainingRequest() {
        System.out.println("\n--- Training Request Form ---");
        final String DEFAULT_TEST_SIZE = "0.2";
        final String DEFAULT_N_EST = "700";
        final String DEFAULT_MAX_DEPTH = "None";
        final String DEFAULT_HIDDEN = "100,50";
        final String DEFAULT_ACTIVATION = "relu";
        final String DEFAULT_MAX_ITER = "400";
        // Model selection
        System.out.println("Select the machine learning algorithm:");
        System.out.println("1) Random Forest Regressor");
        System.out.println("2) Gradient Boosting Regressor");
        System.out.println("3) Linear Regression");
        System.out.println("4) Neural Network");
        System.out.print("Enter option (1-4): ");

        String algorithm = "";
        boolean validOption = false;
        while (!validOption) {
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
                case "4":
                    algorithm = "NeuralNetwork";
                    validOption = true;
                    break;
                default:
                    System.out.println("[WARNING] Invalid option. Please enter a valid option (1-4).");
            }
        }

        System.out.print("Enter model name to save the model: ");
        String modelName = sc.nextLine().trim();

        Map<String, String> hyperparameters = new HashMap<>();
        hyperparameters.put("algorithm", algorithm);

        // Test size (default 0.2)
        System.out.print("Enter test size (0.0-1.0) [default " + DEFAULT_TEST_SIZE + "]: ");
        String testSizeInput = sc.nextLine().trim();
        if (testSizeInput.isEmpty()) {
            System.out.println("[INFO] Using default test size: " + DEFAULT_TEST_SIZE);
            hyperparameters.put("test_size", DEFAULT_TEST_SIZE);
        } else {
            try {
                float testSize = Float.parseFloat(testSizeInput);
                if (testSize > 0 && testSize < 1) {
                    hyperparameters.put("test_size", testSizeInput);
                } else {
                    System.out.println("[WARNING] Invalid test size. Using default " + DEFAULT_TEST_SIZE);
                    hyperparameters.put("test_size", DEFAULT_TEST_SIZE);
                }
            } catch (NumberFormatException e) {
                System.out.println("[WARNING] Invalid input. Using default test size " + DEFAULT_TEST_SIZE);
                hyperparameters.put("test_size", DEFAULT_TEST_SIZE);
            }
        }

        // Ensemble hyperparameters
        if (algorithm.equals("RandomForest") || algorithm.equals("GradientBoosting")) {
            System.out.println("\n--- Hyperparameters Configuration ---");
            System.out.print("Enter number of estimators [default " + DEFAULT_N_EST + "]: ");
            String nEstimators = sc.nextLine().trim();
            if (nEstimators.isEmpty()) {
                System.out.println("[INFO] Using default n_estimators: " + DEFAULT_N_EST);
                hyperparameters.put("n_estimators", DEFAULT_N_EST);
            } else {
                try {
                    int n = Integer.parseInt(nEstimators);
                    if (n > 0) hyperparameters.put("n_estimators", nEstimators);
                    else { System.out.println("[WARNING] Invalid value. Using default " + DEFAULT_N_EST); hyperparameters.put("n_estimators", DEFAULT_N_EST); }
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Invalid input. Using default " + DEFAULT_N_EST);
                    hyperparameters.put("n_estimators", DEFAULT_N_EST);
                }
            }

            System.out.print("Enter max depth (press Enter for None): ");
            String maxDepth = sc.nextLine().trim();
            if (!maxDepth.isEmpty()) {
                try {
                    int depth = Integer.parseInt(maxDepth);
                    if (depth > 0) hyperparameters.put("max_depth", maxDepth);
                    else { System.out.println("[WARNING] Invalid value. Using "+DEFAULT_MAX_DEPTH); }
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Invalid input. Using "+DEFAULT_MAX_DEPTH);
                }
            } else {
                System.out.println("[INFO] Using default max_depth: " + DEFAULT_MAX_DEPTH);
            }

        } else if (algorithm.equals("NeuralNetwork")) {
            System.out.println("\n--- Neural Network Hyperparameters ---");

            System.out.print("Enter hidden layers (comma-separated) [default " + DEFAULT_HIDDEN + "]: ");
            String hiddenLayers = sc.nextLine().trim();
            if (hiddenLayers.isEmpty()) {
                System.out.println("[INFO] Using default hidden layers: " + DEFAULT_HIDDEN);
                hyperparameters.put("hidden_layers", DEFAULT_HIDDEN);
            } else {
                hyperparameters.put("hidden_layers", hiddenLayers);
            }

            System.out.println("Select activation function:");
            System.out.println("1) Identity");
            System.out.println("2) Logistic");
            System.out.println("3) Tanh");
            System.out.println("4) Relu");
            System.out.print("Enter option (1-4) [default 4]: ");
            String activation = "relu";
            String activationOption = sc.nextLine().trim();
            switch (activationOption) {
                case "1": activation = "identity"; break;
                case "2": activation = "logistic"; break;
                case "3": activation = "tanh"; break;
                case "4": activation = "relu"; break;
                default: System.out.println("[WARNING] Invalid option. Using default "+DEFAULT_ACTIVATION); break;
            }
            hyperparameters.put("activation", activation);

            System.out.print("Enter epochs [default " + DEFAULT_MAX_ITER + "]: ");
            String maxIter = sc.nextLine().trim();
            if (maxIter.isEmpty()) {
                System.out.println("[INFO] Using default epochs: " + DEFAULT_MAX_ITER);
                hyperparameters.put("max_iter", DEFAULT_MAX_ITER);
            } else {
                try {
                    int mi = Integer.parseInt(maxIter);
                    if (mi > 0) hyperparameters.put("max_iter", maxIter);
                    else { System.out.println("[WARNING] Invalid value. Using default " + DEFAULT_MAX_ITER); hyperparameters.put("max_iter", DEFAULT_MAX_ITER); }
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Invalid input. Using default " + DEFAULT_MAX_ITER);
                    hyperparameters.put("max_iter", DEFAULT_MAX_ITER);
                }
            }
        } else {
            System.out.println("[INFO] Linear Regression has no hyperparameters to configure.\n");
        }

        return new TrainingRequest(modelName, hyperparameters);
    }

    private void sendTrainingRequestToServer(ObjectOutputStream oos, ObjectInputStream ois, TrainingRequest tr) {
        try {
            oos.writeBytes("TRAIN_MODEL\n");
            oos.flush();

            System.out.println("\nOver which dataset do you want to train the model?");
            System.out.println("Take into account that the dataset name means approximately the amount of records it contains.");
            System.out.println("(Datasets available)");
            List<String> datasets = (List<String>) ois.readObject();
            for (String dataset : datasets) {
                System.out.println(dataset);
            }
            String ds;
            while (true) {
                System.out.print("\nInsert dataset name: ");
                String in = sc.nextLine().trim();
                if (!in.isEmpty() && datasets.stream().anyMatch(d -> d.equalsIgnoreCase(in))) {
                    ds = datasets.stream().filter(d -> d.equalsIgnoreCase(in)).findFirst().get();
                    break;
                }
                System.out.println("Invalid name, try again.");
            }
            tr.setDatasetUsed(ds);

            oos.writeObject(tr);
            oos.flush();

            // Read server response
            String response = ois.readLine();
            System.out.println("[SERVER] " + response + "\n");



        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // ============================================================
    // INSERT DATASET 
    // ============================================================
    private WorkerWithStudies fillStudentForm() {
        WorkerWithStudies student = null;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.println("\n--- Please fill the next form with your data ---\n");

                System.out.print("Insert your country (Brazil, China, Spain, Pakistan, USA, India, Vietnam, Nigeria): ");
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

                System.out.print("GPA (0-10): ");
                float gpa = readFloat();

                System.out.print("Age: ");
                int age = readInt();

                System.out.print("Salary: ");
                int salary = readInt();

                student = new WorkerWithStudies(country, gender, educationalLevel, fieldOfStudy,
                        englishProficiency, internshipExperience, gpa, age, salary);
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
            System.out.println("\nIn which dataset do you want to insert your record?");
            System.out.println("Take into account that the dataset name means approximately the amount of records it contains.");
            System.out.println("(Datasets available)");

            // Check datasets available
            List<String> datasets = (List<String>) ois.readObject();
            for (String dataset : datasets) {
                System.out.println(dataset);
            }

            String ds;
            while (true) {
                System.out.println("\nInsert dataset name: ");
                String in = sc.nextLine().trim();
                if (!in.isEmpty() && datasets.stream().anyMatch(d -> d.equalsIgnoreCase(in))) {
                    ds = datasets.stream().filter(d -> d.equalsIgnoreCase(in)).findFirst().get();
                    break;
                }
                System.out.println("Invalid name, try again.");
            }
            DatasetInsertRequest di = new DatasetInsertRequest(ds, student);

            // Send the DatasetInsertRequest object and reset the stream
            oos.writeObject(di);
            oos.flush();

            // Read server response
            String response = ois.readLine();
            System.out.println("[SERVER] " + response + "\n");

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

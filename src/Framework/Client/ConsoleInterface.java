package Framework.Client;

import Framework.Domain.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConsoleInterface {

    private final Scanner sc;

    public ConsoleInterface(Scanner sc) {
        this.sc = sc;
    }

    public void printMenu() {
        System.out.println("\r\n================================ CLIENT MENU ================================");
        System.out.println("1) Fill a form with your first salary and help us to improve our datasets.");
        System.out.println("2) Train machine learning model over chosen dataset and hyperparameters.");
        System.out.println("3) Predict your first salary with a trained model.");
        System.out.println("4) Disconnect.");
        System.out.print("Select an option: ");
    }

    public WorkerWithStudies fillStudentForm(boolean withSalary) {
        WorkerWithStudies student = null;
        boolean validInput = false;

        while (!validInput) {
            try {
                System.out.println("\r\n--- Please fill the next form with your data ---\r\n");

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

                Integer salary = null;
                if (withSalary) {
                    System.out.print("Salary: ");
                    salary = readInt();
                }

                student = new WorkerWithStudies(country, gender, educationalLevel, fieldOfStudy,
                        englishProficiency, internshipExperience, gpa, age, salary);
                validInput = true;

            } catch (IllegalArgumentException e) {
                System.out.println("[ERROR] You entered an invalid parameter. Please, fill the form again.\r\n");
            }
        }

        return student;
    }

    public TrainingRequest fillTrainingRequest() {
        System.out.println("\r\n--- Training Request Form ---");
        final String DEFAULT_TEST_SIZE = "0.2";
        final String DEFAULT_N_EST = "700";
        final String DEFAULT_MAX_DEPTH = "None";
        final String DEFAULT_HIDDEN = "100,50";
        final String DEFAULT_ACTIVATION = "relu";
        final String DEFAULT_MAX_ITER = "400";

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

        if (algorithm.equals("RandomForest") || algorithm.equals("GradientBoosting")) {
            System.out.println("\r\n--- Hyperparameters Configuration ---");
            System.out.print("Enter number of estimators [default " + DEFAULT_N_EST + "]: ");
            String nEstimators = sc.nextLine().trim();
            if (nEstimators.isEmpty()) {
                System.out.println("[INFO] Using default n_estimators: " + DEFAULT_N_EST);
                hyperparameters.put("n_estimators", DEFAULT_N_EST);
            } else {
                try {
                    int n = Integer.parseInt(nEstimators);
                    if (n > 0) {
                        hyperparameters.put("n_estimators", nEstimators);
                    } else {
                        System.out.println("[WARNING] Invalid value. Using default " + DEFAULT_N_EST);
                        hyperparameters.put("n_estimators", DEFAULT_N_EST);
                    }
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
                    if (depth > 0) {
                        hyperparameters.put("max_depth", maxDepth);
                    } else {
                        System.out.println("[WARNING] Invalid value. Using " + DEFAULT_MAX_DEPTH);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Invalid input. Using " + DEFAULT_MAX_DEPTH);
                }
            } else {
                System.out.println("[INFO] Using default max_depth: " + DEFAULT_MAX_DEPTH);
            }

        } else if (algorithm.equals("NeuralNetwork")) {
            System.out.println("\r\n--- Neural Network Hyperparameters ---");

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
                case "1":
                    activation = "identity";
                    break;
                case "2":
                    activation = "logistic";
                    break;
                case "3":
                    activation = "tanh";
                    break;
                case "4":
                    activation = "relu";
                    break;
                default:
                    System.out.println("[WARNING] Invalid option. Using default " + DEFAULT_ACTIVATION);
                    break;
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
                    if (mi > 0) {
                        hyperparameters.put("max_iter", maxIter);
                    } else {
                        System.out.println("[WARNING] Invalid value. Using default " + DEFAULT_MAX_ITER);
                        hyperparameters.put("max_iter", DEFAULT_MAX_ITER);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("[WARNING] Invalid input. Using default " + DEFAULT_MAX_ITER);
                    hyperparameters.put("max_iter", DEFAULT_MAX_ITER);
                }
            }
        } else {
            System.out.println("[INFO] Linear Regression has no hyperparameters to configure.\r\n");
        }

        return new TrainingRequest(modelName, hyperparameters);
    }

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
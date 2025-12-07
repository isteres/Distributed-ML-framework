package Framework.Client;

import Framework.Domain.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class Client {

    private final String SERVER_HOST = "localhost";
    private final int SERVER_PORT = 16666;
    private static final int INACTIVITY_TIMEOUT = 60 * 1000;

    private Scanner sc;
    private String userID;
    private final ConsoleInterface console;
    private InactivityWatcher inactivityWatcher;

    public Client(String userID) {
        this.sc = new Scanner(System.in);
        this.userID = userID;
        this.console = new ConsoleInterface(sc);
    }

    public String getUserID() {
        return userID;
    }

    public void initialize() {
        // Highlight the OOS initialized before the OIS to avoid deadlocks
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT); 
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream()); 
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());) {

            inactivityWatcher = new InactivityWatcher(INACTIVITY_TIMEOUT);
            inactivityWatcher.start();

            boolean exit = false;
            signIn(oos,ois);  

            while (!exit && !inactivityWatcher.hasTimedOut()) {
                console.printMenu();
                String option = sc.nextLine().trim();
                inactivityWatcher.resetTimer();

                if (inactivityWatcher.hasTimedOut()) {
                    System.out.println("[INFO] Sorry! Client closed due to inactivity.");
                    break;
                }

                // Assumed that when the user selects an option, he is active 
                switch (option) {
                    case "1":
                        WorkerWithStudies worker = console.fillStudentForm(true);
                        sendRecordToServer(oos, ois, worker);
                        inactivityWatcher.resetTimer();
                        break;

                    case "2":
                        TrainingRequest tr = console.fillTrainingRequest();
                        sendTrainingRequestToServer(oos, ois, tr);
                        inactivityWatcher.resetTimer();
                        break;

                    case "3":
                        WorkerWithStudies studentWithoutSalary = console.fillStudentForm(false);
                        InferenceRequest ir = new InferenceRequest(studentWithoutSalary, null);
                        sendInferenceRequestToServer(oos, ois, ir);
                        inactivityWatcher.resetTimer();
                        break;

                    case "4":
                        downloadModelFromServer(oos, ois);
                        inactivityWatcher.resetTimer();
                        break;

                    case "5":
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

            if(inactivityWatcher != null) {
                inactivityWatcher.stopWatcher();
            }   
            System.out.println("[INFO] Have a good one!");
        }
    }


    private void signIn(ObjectOutputStream oos, ObjectInputStream ois) throws RuntimeException {
        try {
            oos.writeBytes("SIGN_IN\r\n");
            oos.writeBytes(this.getUserID() + "\r\n");
            oos.flush();
            System.out.println("[SERVER] " + ois.readLine());
            // Read the extra newline sent by server, would be null if the user tries to sign in from two devices
            if(ois.readLine() == null) {
                throw new RuntimeException("User already signed in from another device.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRecordToServer(ObjectOutputStream oos, ObjectInputStream ois, WorkerWithStudies student) {

        try {
            // Send command
            oos.writeBytes("INSERT_DATASET\r\n");
            oos.flush();
            System.out.println("\r\nIn which dataset do you want to insert your record?");
            System.out.println("Take into account that the dataset name means the amount of records it contains.");
            System.out.println("(Datasets available)");

            // Check datasets available
            List<String> datasets = (List<String>) ois.readObject();
            for (String dataset : datasets) {
                System.out.println(dataset);
            }

            String ds;
            while (true) {
                System.out.println("\r\nInsert dataset name: ");
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
            System.out.println("[SERVER] " + response + "\r\n");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void sendTrainingRequestToServer(ObjectOutputStream oos, ObjectInputStream ois, TrainingRequest tr) {
        try {
            oos.writeBytes("TRAIN_MODEL\r\n");
            oos.flush();

            System.out.println("\r\nOver which dataset do you want to train the model?");
            System.out.println("Take into account that the dataset name means the amount of records it contains.");
            System.out.println("(Datasets available)");
            List<String> datasets = (List<String>) ois.readObject();
            
            for (String dataset : datasets) {
                System.out.println(dataset);
            }
            String ds;
            while (true) {
                System.out.print("\r\nInsert dataset name: ");
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
            System.out.println("[SERVER] " + response + "\r\n");



        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String selectModelFromList(List<String> models, String prompt) {
        System.out.println("\nAvailable models:");
        
        // Print server models first
        boolean serverHeaderPrinted = false;
        boolean userHeaderPrinted = false;
        
        for (int i = 0; i < models.size(); i++) {
            String model = models.get(i);
            if (model.startsWith("Server_")) {
                if (!serverHeaderPrinted) {
                    System.out.println("  [SERVER MODELS]");
                    serverHeaderPrinted = true;
                }
            } else {
                if (!userHeaderPrinted) {
                    System.out.println("  [YOUR MODELS]");
                    userHeaderPrinted = true;
                }
            }
            System.out.println("    " + (i + 1) + ". " + model);
        }

        while (true) {
            System.out.print(prompt);
            try {
                int choice = Integer.parseInt(sc.nextLine().trim());
                if (choice > 0 && choice <= models.size()) {
                    return models.get(choice - 1);
                }
                System.out.println("Invalid selection.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private void sendInferenceRequestToServer(ObjectOutputStream oos, ObjectInputStream ois, InferenceRequest ir) {
        try {
            oos.writeBytes("STUDENT_INFERENCE\r\n");
            oos.flush();

            List<String> availableModels = (List<String>) ois.readObject();
            
            if (availableModels.isEmpty()) {
                System.out.println("[INFO] No models available for inference.");
                return;
            }

            String selectedModel = selectModelFromList(availableModels, "Select model for inference: ");
            ir.setModelName(selectedModel);

            oos.writeObject(ir);
            oos.flush();

            String response = ois.readLine();
            System.out.println("[SERVER] " + response + "$/year\r\n");
            
            try {
                Thread.sleep(1000 * 3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void downloadModelFromServer(ObjectOutputStream oos, ObjectInputStream ois) {
        try {
            oos.writeBytes("LIST_MODELS\r\n");
            oos.flush();

            List<String> models = (List<String>) ois.readObject();
            
            if (models.isEmpty()) {
                System.out.println("[INFO] No models available.");
                return;
            }

            String selected = selectModelFromList(models, "Select model to download: ");

            Thread downloadThread = new Thread(
                new ModelDownloaderThread(SERVER_HOST, SERVER_PORT, userID, selected, "DownloadedModels")
            );
            downloadThread.start();
            
            System.out.println("[INFO] Download started in background.");

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }
}

   
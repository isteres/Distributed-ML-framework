package Framework.Server;

import Framework.Domain.*;
import Framework.Persistence.ServerDatabase;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ModelTrainerThread implements Runnable {

    private TrainingRequest trainingRequest;
    private String userID;
    private CyclicBarrier startBarrier;
    private CountDownLatch finishLatch;

    public ModelTrainerThread(TrainingRequest tr, String userID) {
        this.trainingRequest = tr;
        this.userID = userID;
        this.startBarrier = null;
        this.finishLatch = null;
    }

    public ModelTrainerThread(TrainingRequest tr, String userID, CyclicBarrier startBarrier, CountDownLatch finishLatch) {
        this.trainingRequest = tr;
        this.userID = userID;
        this.startBarrier = startBarrier;
        this.finishLatch = finishLatch;
    }

    public void run() {
        try {


            // Synchronize the start if we want to paralelize the training
            if (startBarrier != null) {
                startBarrier.await();
                System.out.println("[TRAINING] Starting training for: " + trainingRequest.getModelName());
            }

            String script = ".\\src\\python_scripts\\main.py";
            String python = ".\\.venv\\Scripts\\python.exe";

            ProcessBuilder pb = new ProcessBuilder(python, script);

            String datasetPath = new File("Datasets", this.trainingRequest.getDatasetUsed()).getAbsolutePath();

            File userDir = new File("TrainedModels", this.userID);

            if (!userDir.exists()) {
                userDir.mkdirs();
            }
            if (!userDir.exists()) {
                System.err.println("[TRAINING] ERROR creating directory: " + userDir.getAbsolutePath());
            }
            String outputPath = new File(userDir, this.trainingRequest.getModelName() + "_model.pkl").getAbsolutePath();

            pb.command().add("--dataset");
            pb.command().add(datasetPath);

            pb.command().add("--output");
            pb.command().add(outputPath);

            Map<String, String> hyperparamMap = this.trainingRequest.getHyperparameters();
            for (Map.Entry<String, String> entry : hyperparamMap.entrySet()) {
                pb.command().add("--" + entry.getKey());
                pb.command().add(entry.getValue());
            }

            pb.redirectErrorStream(true);
            System.out.println("[TRAINING] Command: " + String.join(" ", pb.command()));

            Process process = pb.start();

    
            float r2Score = 0;
            float mae = 0;

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[PYTHON] " + line);
                    
                    if (line.contains("R2")) {
                        String [] split = line.split("=");
                        if (split.length == 2) {
                            r2Score = Float.parseFloat(split[1].trim());
                        }
                    }
                    if (line.contains("MAE")) {
                        String [] split = line.split("=");
                        if (split.length == 2) {
                            mae = Float.parseFloat(split[1].trim());
                        }
                    }
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Python exited with code: " + exitCode);

            if (exitCode == 0) {
                System.out.println("[TRAINING] Training completed for: " + trainingRequest.getModelName());
                
                if(!userID.equals("SERVER")){
                    String algorithm = hyperparamMap.get("algorithm");
                    ServerDatabase.getInstance().registerTrainedModel(userID, trainingRequest.getModelName(), 
                    algorithm, trainingRequest.getDatasetUsed(), r2Score, mae
                    );
                }

            }else {
                System.err.println("[TRAINING] Training failed for: " + trainingRequest.getModelName());
            }


        } catch (InterruptedException | BrokenBarrierException e) {
            System.err.println("[TRAINING] Synchronization failed: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Count downthe latch to synchronize the end if needed
            if (finishLatch != null) {
                finishLatch.countDown();
            }
        }
    }
}
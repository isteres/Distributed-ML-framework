package Framework.Server;

import Framework.Domain.*;
import Framework.Persistence.ServerDatabase;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;


/**
 * Executes a model training job by invoking the project's Python training script.
 *
 * This runnable prepares the command line, launches the Python process, parses
 * basic training metrics (R2, MAE) from the process output and registers the
 * trained model metadata in the `ServerDatabase` when appropriate. It supports
 * optional synchronization using a {@link CyclicBarrier} to align simultaneous
 * starts and a {@link CountDownLatch} to signal completion.
 */
public class ModelTrainerThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(ModelTrainerThread.class.getName());

    private TrainingRequest trainingRequest;
    private String userID;
    private CyclicBarrier startBarrier;
    private CountDownLatch finishLatch;

    /**
     * Constructs a ModelTrainerThread for the given training request and user.
     * This constructor does not use synchronization primitives.
     *
     * @param tr the training request containing model name, dataset and hyperparameters
     * @param userID the user id that will own the trained model
     */
    public ModelTrainerThread(TrainingRequest tr, String userID) {
        this.trainingRequest = tr;
        this.userID = userID;
        this.startBarrier = null;
        this.finishLatch = null;
    }

    /**
     * Constructs a ModelTrainerThread that participates in optional synchronization.
     * The {@code startBarrier} will be awaited before training begins, so 
     * all the threads with that barrier will run in parallel and the
     * {@code finishLatch} will be decremented when training completes.
     *
     * @param tr the training request containing model name, dataset and hyperparameters
     * @param userID the user id that will own the trained model
     * @param startBarrier barrier to synchronize start of training across threads (may be null)
     * @param finishLatch latch to signal completion to other components (may be null)
     */
    public ModelTrainerThread(TrainingRequest tr, String userID, CyclicBarrier startBarrier, CountDownLatch finishLatch) {
        this.trainingRequest = tr;
        this.userID = userID;
        this.startBarrier = startBarrier;
        this.finishLatch = finishLatch;
    }

    /**
     * Executes the training process by launching the Python script defined by
     * the project. Parses training output for metrics, waits for process
     * termination and registers model metadata on success. Any synchronization
     * primitives provided at construction are respected.
     */
    public void run() {
        try {

            // Synchronize the start if we want to paralelize the training
            if (startBarrier != null) {
                startBarrier.await();
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
                LOGGER.severe("[" + userID + "] ERROR creating directory: " + userDir.getAbsolutePath());
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
            LOGGER.info("[" + userID + "] Command: " + String.join(" ", pb.command()));

            Process process = pb.start();

    
            float r2Score = 0;
            float mae = 0;

            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    
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
            LOGGER.info("[" + userID + "] Python exited with code: " + exitCode);

            if (exitCode == 0) {
                LOGGER.info("[" + userID + "] Training completed for: " + trainingRequest.getModelName());
                
                if(!userID.equals("SERVER")){
                    String algorithm = hyperparamMap.get("algorithm");
                    ServerDatabase.getInstance().registerTrainedModel(userID, trainingRequest.getModelName(), 
                    algorithm, trainingRequest.getDatasetUsed(), r2Score, mae
                    );
                }

            } else {
                LOGGER.severe("[" + userID + "] Training failed for: " + trainingRequest.getModelName());
            }


        } catch (InterruptedException | BrokenBarrierException e) {
            LOGGER.severe("[" + userID + "] Synchronization failed: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.severe("[" + userID + "] IO error during training: " + e.getMessage());
        } finally {
            // Count down the latch to synchronize the end if needed
            if (finishLatch != null) {
                finishLatch.countDown();
            }
        }
    }
}
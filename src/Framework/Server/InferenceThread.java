package Framework.Server;

import Framework.Domain.*;
import java.io.*;
import java.util.concurrent.Callable;
import java.util.logging.Logger;


public class InferenceThread implements Callable<Float> {

    private static final Logger LOGGER = Logger.getLogger(InferenceThread.class.getName());

    private InferenceRequest predictionRequest;
    private String userID;

    public InferenceThread(InferenceRequest pr, String userID) {
        this.predictionRequest = pr;
        this.userID = userID;
    }

    public Float call() throws Exception {
        String script = ".\\src\\python_scripts\\predict.py";
        String python = ".\\.venv\\Scripts\\python.exe";

        ProcessBuilder pb = new ProcessBuilder(python, script);
        
        // Find model file
        String modelName = this.predictionRequest.getModelName();
        String ownerID;
        if (modelName.startsWith("Server_")) {
            ownerID = "SERVER";
        } else {
            ownerID = this.userID;
        }

        File userDir = new File("TrainedModels", ownerID);
        File modelFile = new File(userDir, this.predictionRequest.getModelName() + "_model.pkl");

        if (!modelFile.exists()) {
            LOGGER.severe("[" + userID + "] Model not found: " + modelFile.getAbsolutePath());
            throw new FileNotFoundException("Model not found: " + modelFile.getAbsolutePath());
        }

        WorkerWithStudies student = this.predictionRequest.getStudent();

        // Add arguments
        pb.command().add("--model");
        pb.command().add(modelFile.getAbsolutePath());

        pb.command().add("--country");
        pb.command().add(student.getCountry().name());

        pb.command().add("--gender");
        pb.command().add(student.getGender().name());

        pb.command().add("--educational_level");
        pb.command().add(student.getEducationalLevel().name());

        pb.command().add("--field_of_study");
        pb.command().add(student.getFieldOfStudy().name());

        pb.command().add("--english_proficiency");
        pb.command().add(student.getEnglishProficiency().name());

        pb.command().add("--internship_experience");
        pb.command().add(student.getInternshipExperience().name());

        pb.command().add("--gpa");
        pb.command().add(String.valueOf(student.getGpa()));

        pb.command().add("--age");
        pb.command().add(String.valueOf(student.getAge()));

        pb.redirectErrorStream(true);
        LOGGER.info("[" + userID + "] Starting inference with model: " + modelName);

        Process process = null;
        Float prediction = null;
        
        try {

            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Thread was interrupted before starting prediction");
            }

            process = pb.start();

            // Read Python script output and check for interruptions
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Prediction was cancelled due to timeout");
                    }
                    
                    if (line.startsWith("PREDICTION:")) {
                        prediction = Float.parseFloat(line.substring("PREDICTION:".length()));
                    }
                }
            }

            // Check for interruption before waiting for process completion
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Prediction was cancelled");
            }

            int exitCode = process.waitFor();

            // Validate prediction result
            if (exitCode != 0 || prediction == null) {
                LOGGER.severe("[" + userID + "] Prediction failed with exit code: " + exitCode);
                throw new RuntimeException("Prediction failed with exit code: " + exitCode);
            }

            LOGGER.info("[" + userID + "] Inference completed. Result: " + prediction);
            return prediction;

        } catch (InterruptedException e) {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
            LOGGER.warning("[" + userID + "] Inference interrupted.");
            // Propagate to let ConnectionHandler handle it
            throw e;
            
        } catch (IOException e) {
            // Handle IO execution errors
            if (process != null && process.isAlive()) {
                process.destroy();
            }
            LOGGER.severe("[" + userID + "] IO error during inference: " + e.getMessage());
            throw new IOException("Error executing prediction script: " + e.getMessage(), e);
            
        } 
    }
}
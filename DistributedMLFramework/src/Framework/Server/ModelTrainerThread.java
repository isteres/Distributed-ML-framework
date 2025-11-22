package Framework.Server;
import Framework.Domain.*;
import java.io.*;
import java.util.*;

public class ModelTrainerThread implements Runnable{
	private TrainingRequest trainingRequest;
	private Exception exc;
	
	public ModelTrainerThread(TrainingRequest tr) {
		this.trainingRequest = tr;
		
	}
	
	public void run() {
		
		try {
			String script = "DistributedMLFramework\\src\\python_scripts\\model_trainer.py";
            String python = "DistributedMLFramework\\.venv\\Scripts\\python.exe";

            ProcessBuilder pb = new ProcessBuilder(python, script);
            
			// Add the flags with the hyperparameters
			String datasetPath = new File("Datasets/" + this.trainingRequest.getDatasetUsed())
                          			.getAbsolutePath();
			String outputPath  = new File("TrainedModels/" + this.trainingRequest.getModelName()+ "_model.pkl")
									.getAbsolutePath();

			pb.command().add("--dataset");
			pb.command().add(datasetPath);

			pb.command().add("--output");
			pb.command().add(outputPath);



			Map<String,String> hyperparamMap = this.trainingRequest.getHyperparameters();
			for(Map.Entry<String, String> entry : hyperparamMap.entrySet()) {
				pb.command().add("--"+entry.getKey());
				pb.command().add(entry.getValue());		
			}

            pb.redirectErrorStream(true);
			System.out.println("[TRAINING] Command: " + String.join(" ", pb.command()));
			
			Process process = pb.start();
			// Read the output from the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[PYTHON] " + line);
            }

            int exitCode = process.waitFor();
            System.out.println("Python exited with code: " + exitCode);

    	}catch(IOException | InterruptedException e) {
			e.printStackTrace();				
		}
			
	}
}



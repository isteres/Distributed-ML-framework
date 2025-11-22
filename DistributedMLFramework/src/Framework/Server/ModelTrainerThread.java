package Framework.Server;
import Framework.Domain.*;

public class ModelTrainerThread implements Runnable{
	private TrainingRequest trainingRequest;
	private Exception exc;
	
	public ModelTrainerThread(TrainingRequest tr) {
		this.trainingRequest = tr;
		
	}
	
	public void run() {
		
		
			String script = "DistributedMLFramework/src/python_scripts/model_trainer.py";
            String python = "DistributedMLFramework/.venv/bin/python.exe";

            // Ejecutar el script dentro de WSL con python3
            ProcessBuilder pb = new ProcessBuilder(python, script);
            
            
            
            
            
		
	}

}

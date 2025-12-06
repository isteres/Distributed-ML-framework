package Framework.Server;

import Framework.Domain.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.*;

public class DailyTrainingTask extends TimerTask {

    private String dataset;

    public DailyTrainingTask(String dataset) {
        this.dataset = dataset;
    }

    @Override
    public void run() {
        
        System.out.println("[DAILY TRAINING] Starting automatic daily model training...");

        ExecutorService pool = Executors.newFixedThreadPool(4);
        CyclicBarrier syncStart = new CyclicBarrier(5);
        CountDownLatch doneSignal = new CountDownLatch(4);

        try {
            
            // Train one model of each algorithm
            TrainingRequest rf = createTrainingRequest("Server_RandomForest", "RandomForest");
            pool.execute(new ModelTrainerThread(rf, "SERVER", syncStart, doneSignal));

            TrainingRequest gb = createTrainingRequest("Server_GradientBoosting", "GradientBoosting");
            pool.execute(new ModelTrainerThread(gb, "SERVER", syncStart, doneSignal));

            TrainingRequest lr = createTrainingRequest("Server_LinearRegression", "LinearRegression");
            pool.execute(new ModelTrainerThread(lr, "SERVER", syncStart, doneSignal));

            TrainingRequest nn = createTrainingRequest("Server_NeuralNetwork", "NeuralNetwork");
            pool.execute(new ModelTrainerThread(nn, "SERVER", syncStart, doneSignal));

            syncStart.await();
            System.out.println("[DAILY TRAINING] Scheduled 4 training tasks.");
            doneSignal.await();
            System.out.println("[DAILY TRAINING] All 4 training tasks completed.");

        } catch (InterruptedException e) {
            System.err.println("[DAILY TRAINING] Training tasks interrupted.");
            Thread.currentThread().interrupt();
        } catch(BrokenBarrierException e) {
            System.err.println("[DAILY TRAINING] Barrier broken.");
        } finally {
            pool.shutdown();

        }
    }

    private TrainingRequest createTrainingRequest(String modelName, String algorithm) {
        Map<String, String> hyperparameters = new HashMap<>();
        hyperparameters.put("algorithm", algorithm);
        hyperparameters.put("test_size", "0.2");

        if (algorithm.equals("RandomForest") || algorithm.equals("GradientBoosting")) {
            hyperparameters.put("n_estimators", "700");
        } else if (algorithm.equals("NeuralNetwork")) {
            hyperparameters.put("hidden_layers", "100,50");
            hyperparameters.put("activation", "relu");
            hyperparameters.put("max_iter", "200");
        }

        TrainingRequest tr = new TrainingRequest(modelName, hyperparameters);
        tr.setDatasetUsed(dataset);
        return tr;
    }
}
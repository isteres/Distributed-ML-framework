package Framework.Server;

import Framework.Domain.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Scheduled task that triggers automatic training of server models.
 *
 * The task launches multiple {@link ModelTrainerThread} jobs in parallel to
 * train a set of server-owned models (one per supported algorithm). It uses
 * a {@link CyclicBarrier} to synchronize the simultaneous start of workers
 * and a {@link CountDownLatch} to wait for completion before shutting down
 * the internal executor.
 */
public class DailyTrainingTask extends TimerTask {

    private static final Logger LOGGER = Logger.getLogger(DailyTrainingTask.class.getName());

    private String dataset;

    /**
     * Constructs a DailyTrainingTask that will train models using the given
     * dataset file name.
     *
     * @param dataset the dataset file name to use for scheduled training
     */
    public DailyTrainingTask(String dataset) {
        this.dataset = dataset;
    }

    /**
     * Executes the scheduled training workflow: creates a fixed thread pool,
     * schedules training jobs for different algorithms, starts them in a
     * parallel manner and waits for all to finish. Logs progress and
     * ensures proper shutdown on interruption or errors.
     */
    public void run() {
        
        LOGGER.info("[SERVER] Starting automatic daily model training...");

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
            LOGGER.info("[SERVER] Scheduled 4 training tasks.");
            doneSignal.await();
            LOGGER.info("[SERVER] All 4 training tasks completed.");

        } catch (InterruptedException e) {
            LOGGER.severe("[SERVER] Training tasks interrupted.");
            Thread.currentThread().interrupt();
        } catch(BrokenBarrierException e) {
            LOGGER.severe("[SERVER] Barrier broken.");
        } finally {
            pool.shutdown();

        }
    }

    /**
     * Helper that builds a {@link TrainingRequest} pre-populated with sensible
     * defaults for the given algorithm and model name. The returned request
     * will have its dataset set to the task's dataset.
     *
     * @param modelName the name to assign to the trained model
     * @param algorithm the algorithm identifier (e.g. "RandomForest")
     * @return a configured TrainingRequest instance
     */
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
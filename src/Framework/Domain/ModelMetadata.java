package Framework.Domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

public class ModelMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private String modelName;              
    private String modelPath;   // Path to .pkl file
    private String datasetUsed;             

    private Map<String, String> hyperparameters;   
    private Map<String, Double> metrics;           

    private LocalDateTime trainedAt;       
    // private String modelType; 

    // ===== Constructors =====
    // Java would add it anyways
    public ModelMetadata() {}

    public ModelMetadata(String modelName,
                         String modelPath,
                         String datasetUsed,
                         Map<String, String> hyperparameters,
                         Map<String, Double> metrics,
                         String modelType) {

        this.modelName = modelName;
        this.modelPath = modelPath;
        this.datasetUsed = datasetUsed;
        this.hyperparameters = hyperparameters;
        this.metrics = metrics;
        this.trainedAt = LocalDateTime.now();
    }

    // ===== Getters =====
    public String getModelName() { return modelName; }
    public String getModelPath() { return modelPath; }
    public String getDatasetUsed() { return datasetUsed; }
    public Map<String, String> getHyperparameters() { return hyperparameters; }
    public Map<String, Double> getMetrics() { return metrics; }
    public LocalDateTime getTrainedAt() { return trainedAt; }
    // public String getModelType() { return modelType; }

    // ===== Setters =====
    public void setModelName(String modelName) { this.modelName = modelName; }
    public void setModelPath(String modelPath) { this.modelPath = modelPath; }
    public void setDatasetUsed(String datasetUsed) { this.datasetUsed = datasetUsed; }
    public void setHyperparameters(Map<String, String> hyperparameters) { this.hyperparameters = hyperparameters; }
    public void setMetrics(Map<String, Double> metrics) { this.metrics = metrics; }
    public void setTrainedAt(LocalDateTime trainedAt) { this.trainedAt = trainedAt; }
    // public void setModelType(String modelType) { this.modelType = modelType; }

    // ===== toString =====
    @Override
    public String toString() {
        return "ModelMetadata{" +
                "modelName='" + modelName + '\'' +
                ", modelPath='" + modelPath + '\'' +
                ", datasetUsed='" + datasetUsed + '\'' +
                ", hyperparameters=" + hyperparameters +
                ", metrics=" + metrics +
                ", trainedAt=" + trainedAt +
                // ", modelType='" + modelType + '\'' +
                '}';
    }
}
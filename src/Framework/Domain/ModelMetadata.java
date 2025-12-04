package Framework.Domain;

import java.io.Serializable;
import java.time.LocalDateTime;


public class ModelMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private String modelName;              
    private String datasetUsed; 
    private String r2Score;
    private String meanAbsoluteError;            
    private LocalDateTime trainedAt;       

    public ModelMetadata() {}

    public ModelMetadata(String modelName, String datasetUsed, String r2Score, 
        String meanAbsoluteError, LocalDateTime trainedAt) {
        this.modelName = modelName;
        this.datasetUsed = datasetUsed;
        this.r2Score = r2Score;
        this.meanAbsoluteError = meanAbsoluteError;
        this.trainedAt = trainedAt;
    }
    public String getModelName() { return modelName; }
    public String getDatasetUsed() { return datasetUsed; }
    public String getR2Score() { return r2Score; }
    public String getMeanAbsoluteError() { return meanAbsoluteError; }
    public LocalDateTime getTrainedAt() { return trainedAt; }

    // ===== Setters =====
    public void setModelName(String modelName) { this.modelName = modelName; }
    public void setDatasetUsed(String datasetUsed) { this.datasetUsed = datasetUsed; }
    public void setR2Score(String r2Score) { this.r2Score = r2Score; }
    public void setMeanAbsoluteError(String meanAbsoluteError) { this.meanAbsoluteError = meanAbsoluteError; }
    public void setTrainedAt(LocalDateTime trainedAt) { this.trainedAt = trainedAt; }
}
package Framework.Domain;
import java.io.*;
import java.util.Map;
public class TrainingRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String modelName;
    private String datasetUsed;
    private Map<String, String> hyperparameters;
    
    public TrainingRequest(String modelName, String datasetUsed, Map<String, String> hyperparameters) {
        this.modelName = modelName;
        this.datasetUsed = datasetUsed;
        this.hyperparameters = hyperparameters;
    }

    public String getModelName() {
        return modelName;
    }
    public String getDatasetUsed() {
        return datasetUsed;
    }
    public Map<String, String> getHyperparameters() {
        return hyperparameters;
    }
    
}

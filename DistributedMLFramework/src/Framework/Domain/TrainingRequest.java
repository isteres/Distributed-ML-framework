package Framework.Domain;
import java.io.*;
import java.util.Map;
public class TrainingRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String modelName;
    private String datasetUsed;
    private Map<String, Integer> hyperparameters;
    
    // At the beginning the Request will be built without specifying the dataset 
    public TrainingRequest(String modelName, Map<String, Integer> hyperparameters) {
        this.modelName = modelName;
        this.datasetUsed = null;
        this.hyperparameters = hyperparameters;
    }

    public String getModelName() {
        return modelName;
    }
    public String getDatasetUsed() {
        return datasetUsed;
    }
    public Map<String, Integer> getHyperparameters() {
        return hyperparameters;
    }
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    public void setDatasetUsed(String datasetUsed) {
        this.datasetUsed = datasetUsed;
    }   
    public void setHyperparameters(Map<String,Integer> hyperparameters) {
        this.hyperparameters = hyperparameters;
    }
    
}

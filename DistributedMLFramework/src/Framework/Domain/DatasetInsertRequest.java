package Framework.Domain;
import java.io.Serializable;

// Class used to send the request of inserting a WorkerWithStudies in a certain dataset
public class DatasetInsertRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String datasetName;
    private WorkerWithStudies studentWithSalary;

    public DatasetInsertRequest(String datasetName, WorkerWithStudies student) {
        this.datasetName = datasetName;
        this.studentWithSalary = student;
    }

    public String getDatasetName() {
        return this.datasetName;
    }

    public WorkerWithStudies getStudent() {
        return this.studentWithSalary;
    }
}
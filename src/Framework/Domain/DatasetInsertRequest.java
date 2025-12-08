package Framework.Domain;
import java.io.Serializable;

/**
 * Request object for inserting a student/worker record into a dataset.
 * 
 * Contains the dataset name and the worker data to be inserted.
 * 
 */
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
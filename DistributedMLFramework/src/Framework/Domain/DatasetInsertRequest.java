package Framework.Domain;
import java.io.Serializable;

// Class used to send the request of inserting a StudentWithSalary in a certain dataset
public class DatasetInsertRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String datasetName;
    private StudentWithSalary studentWithSalary;

    public DatasetInsertRequest(String datasetName, StudentWithSalary student) {
        this.datasetName = datasetName;
        this.studentWithSalary = student;
    }

    public String getDatasetName() {
        return this.datasetName;
    }

    public StudentWithSalary getStudent() {
        return this.studentWithSalary;
    }
}
package Framework.Domain;

import java.io.Serializable;

public class InferenceRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private WorkerWithStudies student;
    private String modelName;


    public InferenceRequest(WorkerWithStudies student, String modelName) {
        this.student = student;
        this.modelName = modelName;
    }

    public WorkerWithStudies getStudent() {
        return student;
    }

    public void setStudent(WorkerWithStudies student) {
        this.student = student;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
   
}

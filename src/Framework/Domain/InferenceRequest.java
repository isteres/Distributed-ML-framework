package Framework.Domain;

import java.io.Serializable;


/**
 * Request object for making salary predictions on a student (still 
 * WorkerWithsStudies, but with null salary).
 * 
 * Contains the student data and the model name to use for inference.
 * 
 * @author Isaac Terés Espallargas
 */
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

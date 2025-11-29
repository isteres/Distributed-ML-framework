package Framework.Domain;

import Framework.Domain.Enums.StudentEnums.*;
import Framework.Domain.Enums.StudentEnums.Country;
import Framework.Domain.Enums.StudentEnums.EducationalLevel;
import Framework.Domain.Enums.StudentEnums.EnglishProficiency;
import Framework.Domain.Enums.StudentEnums.FieldOfStudy;
import Framework.Domain.Enums.StudentEnums.Gender;
import Framework.Domain.Enums.StudentEnums.InternshipExperience;
import java.io.Serializable;

public class WorkerWithStudies implements Serializable {

    private static final long serialVersionUID = 1L;

    private Country country;
    private Gender gender;
    private EducationalLevel educationalLevel;
    private FieldOfStudy fieldOfStudy;
    private EnglishProficiency englishProficiency;
    private InternshipExperience internshipExperience;

    private float gpa;
    private int age;
    private int salary;

    
    public WorkerWithStudies(
    		String country,
            String gender,
            String educationalLevel,
            String fieldOfStudy,
            String englishProficiency,
            String internshipExperience,
            float gpa,
            int age,
            int salary) {

    	this.country = Country.valueOf(normalizeInput(country));
    	this.gender = Gender.valueOf(normalizeInput(gender));
    	this.educationalLevel = EducationalLevel.valueOf(normalizeInput(educationalLevel));
    	this.fieldOfStudy = FieldOfStudy.valueOf(normalizeInput(fieldOfStudy));
    	this.englishProficiency = EnglishProficiency.valueOf(normalizeInput(englishProficiency));
    	this.internshipExperience = InternshipExperience.valueOf(normalizeInput(internshipExperience));

        this.gpa = gpa;
        this.age = age;
        this.salary = salary;
    }
    
    private String normalizeInput(String str) {
    	// To normalize the string, so we mantain the string format in the dataset
        if (str == null || str.isEmpty()) return str;
        str = str.trim().toLowerCase();
        if(str.equals("phd")) {return "PhD";}
        if(str.equals("it")) {return "IT";}
        if (str.replaceAll("\\s+", "_").equals("social_sciences")) {return "Social_Sciences";}
        if(str.equals("usa")){return "USA";}
        str = str.trim().toLowerCase();
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    // ===== GETTERS =====
    public Country getCountry() { return this.country; }
    public Gender getGender() { return this.gender; }
    public EducationalLevel getEducationalLevel() { return this.educationalLevel; }
    public FieldOfStudy getFieldOfStudy() { return fieldOfStudy; }
    public EnglishProficiency getEnglishProficiency() { return englishProficiency; }
    public InternshipExperience getInternshipExperience() { return internshipExperience; }
    public float getGpa() { return gpa; }
    public int getAge() { return age; }
    public int getSalary() { return salary; }

    // ===== SETTERS =====
    public void setCountry(Country country) { this.country = country; }
    public void setGender(Gender gender) { this.gender = gender; }
    public void setEducationalLevel(EducationalLevel educationalLevel) { this.educationalLevel = educationalLevel; }
    public void setFieldOfStudy(FieldOfStudy fieldOfStudy) { this.fieldOfStudy = fieldOfStudy; }
    public void setEnglishProficiency(EnglishProficiency englishProficiency) { this.englishProficiency = englishProficiency; }
    public void setInternshipExperience(InternshipExperience internshipExperience) { this.internshipExperience = internshipExperience; }
    public void setGpa(float gpa) { this.gpa = gpa; }
    public void setAge(int age) { this.age = age; }
    public void setSalary(int salary) { this.salary = salary; }

    // ===== toString =====
    @Override
    public String toString() {
        return "WorkerWithStudies{" +
                "country=" + country +
                ", gender=" + gender +
                ", educationalLevel=" + educationalLevel +
                ", fieldOfStudy=" + fieldOfStudy +
                ", englishProficiency=" + englishProficiency +
                ", internshipExperience=" + internshipExperience +
                ", gpa=" + gpa +
                ", age=" + age +
                ", salary=" + salary +
                '}';
    }
}

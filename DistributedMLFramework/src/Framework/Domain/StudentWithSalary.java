package Framework.Domain;

import java.io.Serializable;
import Framework.Domain.Enums.StudentEnums.*;

public class StudentWithSalary implements Serializable {

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

    
    // ===== CONSTRUCTOR =====
    public StudentWithSalary(String country,
                   String gender,
                   String educationalLevel,
                   String fieldOfStudy,
                   String englishProficiency,
                   String internshipExperience,
                   float gpa,
                   int age,
                   int salary) {

        // Convert Strings to enums (case-insensitive)
        this.country = Country.valueOf(country.toUpperCase());
        this.gender = Gender.valueOf(gender.toUpperCase());
        this.educationalLevel = EducationalLevel.valueOf(educationalLevel.toUpperCase());
        this.fieldOfStudy = FieldOfStudy.valueOf(fieldOfStudy.toUpperCase());
        this.englishProficiency = EnglishProficiency.valueOf(englishProficiency.toUpperCase());
        this.internshipExperience = InternshipExperience.valueOf(internshipExperience.toUpperCase());

        this.gpa = gpa;
        this.age = age;
        this.salary = salary;
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
        return "StudentWithSalary{" +
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

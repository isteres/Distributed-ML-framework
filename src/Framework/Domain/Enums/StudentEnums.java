package Framework.Domain.Enums;


/**
 * Enumeration classes representing student and worker attributes.
 * 
 * Contains all possible values for personal, educational, and professional characteristics
 * used throughout the distributed ML framework for model training and inference.
 * 
 * The main goal of this class was to reduce the incossistencies when dealing with categorical data.
 * 
 */
public class StudentEnums {

    public enum Country {
        Brazil,
        China,
        Spain,
        Pakistan,
        USA,
        India,
        Vietnam,
        Nigeria
    }

    public enum Gender {
        Male,
        Female,
        Other
    }

    public enum EducationalLevel {
        Bachelor,
        Master,
        PhD,
        Diploma
    }


    public enum FieldOfStudy {
        Arts,
        Engineering,
        IT,
        Health,
        Social_Sciences,
        Business
    }


    public enum EnglishProficiency {
        Basic,
        Intermediate,
        Advanced,
        Fluent
    }

    public enum InternshipExperience {
        Yes,
        No
    }
}

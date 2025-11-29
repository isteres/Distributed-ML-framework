package Framework.Server;
import Framework.Domain.DatasetInsertRequest;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class DatasetInserterThread implements Runnable{
	
	private DatasetInsertRequest inRequest;

	// To manage the synchronized access to the datasets files
	private static final ConcurrentHashMap<String, Object> fileLocks = new ConcurrentHashMap<>();

	public DatasetInserterThread(DatasetInsertRequest inRequest) {
		this.inRequest = inRequest;
	}
	
	public void run() {

		String fileName = this.inRequest.getDatasetName();
		Object lock = fileLocks.get(fileName);
		// If the lock does not exist, create it and put it in the map
		if (lock == null) {
			lock = new Object();
			// Thread-safe operation thanks to ConcurrentHashMap
			Object existingLock = fileLocks.putIfAbsent(fileName, lock);
			if (existingLock != null) {
				lock = existingLock;
			}
		}

		synchronized(lock) {
			try {
				File dataset = new File("Datasets/" +this.inRequest.getDatasetName());
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document datasetXML = db.parse(dataset);
				
				Element root = datasetXML.getDocumentElement();
				Element newRecord = datasetXML.createElement("record");
				
				Element country = datasetXML.createElement("Country_of_Origin");
				country.setTextContent(this.inRequest.getStudent().getCountry().name());
				newRecord.appendChild(country);
				
				Element education = datasetXML.createElement("Education_Level");
				education.setTextContent(this.inRequest.getStudent().getEducationalLevel().name());
				newRecord.appendChild(education);
				
				Element field = datasetXML.createElement("Field_of_Study");
				field.setTextContent(this.inRequest.getStudent().getFieldOfStudy().name());
				newRecord.appendChild(field);
				
				Element language = datasetXML.createElement("Language_Proficiency");
				language.setTextContent(this.inRequest.getStudent().getEnglishProficiency().name());
				newRecord.appendChild(language);
				
				Element  gender = datasetXML.createElement("Gender");
				gender.setTextContent(this.inRequest.getStudent().getGender().name());
				newRecord.appendChild(gender);
				
				Element age = datasetXML.createElement("Age");
				age.setTextContent(String.valueOf(this.inRequest.getStudent().getAge()));
				newRecord.appendChild(age);
				
				Element internship = datasetXML.createElement("Internship_Experience");
				internship.setTextContent(String.valueOf(this.inRequest.getStudent().getInternshipExperience()));
				newRecord.appendChild(internship);
				
				Element salary = datasetXML.createElement("Salary");
				salary.setTextContent(String.valueOf(this.inRequest.getStudent().getSalary()));
				newRecord.appendChild(salary);
				
				Element gpa = datasetXML.createElement("GPA_10");
				gpa.setTextContent(String.valueOf(this.inRequest.getStudent().getGpa()));
				newRecord.appendChild(gpa);
				
				root.appendChild(newRecord);
				
				// Save the updated 
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				DOMSource source = new DOMSource(datasetXML);
				StreamResult result = new StreamResult(dataset);
				transformer.transform(source, result);
				
				
			}catch(ParserConfigurationException e) {
				e.printStackTrace();
			}catch(IOException e) {
				e.printStackTrace();
			}catch(SAXException e) {
				e.printStackTrace();
			}catch(TransformerConfigurationException e) {
				e.printStackTrace();
			}catch(TransformerException e) {
				e.printStackTrace();
			}
		}

	}
	
	
	

}

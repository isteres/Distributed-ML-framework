package Framework.Persistence;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

public class ServerDatabase {

    private static final String FILE_PATH = "src/Framework/Persistence/database.xml";
    private static final ServerDatabase instance = new ServerDatabase(FILE_PATH);
    private final File XMLfile;

    private ServerDatabase(String filePath) {
        this.XMLfile = new File(filePath);
        initializeDatabase();
    }

    public static ServerDatabase getInstance() {
        return instance;
    }

    private void initializeDatabase() {
        if (!XMLfile.exists()) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.newDocument();

                Element root = doc.createElement("ServerDatabase");
                doc.appendChild(root);

                saveDocument(doc);

                System.out.println("[DATABASE] File created: " + XMLfile.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to initialize database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public synchronized void registerUser(String userIdentification) {
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            System.err.println("[ERROR] Username cannot be null or empty");
            return;
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(XMLfile);

            if (userExists(doc, userIdentification)) {
                System.out.println("[DATABASE] User already exists: " + userIdentification);
            } else {
                Element root = doc.getDocumentElement();
                Element models = doc.createElement("TrainedModels");
                Element user = doc.createElement("User");
                user.setAttribute("UserID", userIdentification);
                user.setAttribute("SignInDate", getCurrentTimeStamp());
                user.appendChild(models);
                root.appendChild(user);
                saveDocument(doc);
                System.out.println("[DATABASE] User registered: " + userIdentification);

            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to register user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void registerTrainedModel(String userID, String modelName, String algorithm, 
                                                   String dataset, float r2Score, float mae) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(XMLfile);

            Element userElement = findUserElement(doc, userID);
            if (userElement == null) {
                System.err.println("[ERROR] User not found: " + userID);
                return;
            }

            NodeList trainedModelsNodes = userElement.getElementsByTagName("TrainedModels");
            Element trainedModelsElement;
            
            if (trainedModelsNodes.getLength() > 0) {
                trainedModelsElement = (Element) trainedModelsNodes.item(0);
            } else {
                trainedModelsElement = doc.createElement("TrainedModels");
                userElement.appendChild(trainedModelsElement);
            }

            // Create Model wrapper element
            Element modelElement = doc.createElement("Model");
            
            // Create child elements
            Element nameElement = doc.createElement("Name");
            nameElement.setAttribute("TrainingDate", getCurrentTimeStamp());
            nameElement.setTextContent(modelName);

            Element algorithmElement = doc.createElement("Algorithm");
            algorithmElement.setTextContent(algorithm);

            Element datasetElement = doc.createElement("Dataset");
            datasetElement.setTextContent(dataset);

            Element metricsElement = doc.createElement("Metrics");
            
            Element r2Element = doc.createElement("R2Score");
            r2Element.setTextContent(Float.toString(r2Score));
            
            Element maeElement = doc.createElement("MeanAbsoluteError");
            maeElement.setTextContent(Float.toString(mae));

            metricsElement.appendChild(r2Element);
            metricsElement.appendChild(maeElement);
            
            modelElement.appendChild(nameElement);
            modelElement.appendChild(algorithmElement);
            modelElement.appendChild(datasetElement);
            modelElement.appendChild(metricsElement);

            trainedModelsElement.appendChild(modelElement);

            saveDocument(doc);
            System.out.println("[DATABASE] Model registered: " + modelName + " for user " + userID);

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to register model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Element findUserElement(Document doc, String userID) {
        NodeList users = doc.getElementsByTagName("User");
        for (int i = 0; i < users.getLength(); i++) {
            Element user = (Element) users.item(i);
            if (userID.equals(user.getAttribute("UserID"))) {
                return user;
            }
        }
        return null;
    }

    private boolean userExists(Document doc, String userIdentification) {
        NodeList users = doc.getElementsByTagName("User");
        for (int i = 0; i < users.getLength(); i++) {
            Element user = (Element) users.item(i);
            if (userIdentification.equals(user.getAttribute("UserID"))) {
                return true;
            }
        }
        return false;
    }

    private static String getCurrentTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"));
    }

    private void saveDocument(Document doc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "database.dtd");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(doc), new StreamResult(XMLfile));
        } catch (TransformerException e) {
            System.err.println("[ERROR] Failed to save document: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

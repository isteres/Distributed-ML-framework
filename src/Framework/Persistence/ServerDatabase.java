package Framework.Persistence;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ServerDatabase {

    private static final Logger LOGGER = Logger.getLogger(ServerDatabase.class.getName());

    private static final String FILE_PATH = "src\\Framework\\Persistence\\database.xml";
    private static final ServerDatabase instance = new ServerDatabase(FILE_PATH);
    private final File XMLfile;

    private ServerDatabase(String filePath) {
        this.XMLfile = new File(filePath);
        initializeDatabase();
    }

    /**
     * Returns the singleton instance of the ServerDatabase.
     *
     * @return the single ServerDatabase instance used by the application
     */
    public static ServerDatabase getInstance() {
        return instance;
    }

    /**
     * Ensures the XML database file exists and creates a minimal document structure
     * when the file is not present.
     */
    private void initializeDatabase() {
        if (!XMLfile.exists()) {
            try {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.newDocument();

                Element root = doc.createElement("ServerDatabase");
                doc.appendChild(root);

                saveDocument(doc);

                LOGGER.info("[DATABASE] File created: " + XMLfile.getAbsolutePath());
            } catch (Exception e) {
                LOGGER.severe("[DATABASE] Failed to initialize database: " + e.getMessage());
            }
        }
    }

    /**
     * Registers a new user in the XML database. If the user already exists this
     * method returns {@code true}. On success the new user element is appended to
     * the root and the document is saved.
     *
     * @param userIdentification the unique identifier for the user
     * @return {@code true} if the user already exists or registration succeeded,
     *         {@code false} on invalid input or failure
     */
    public synchronized boolean registerUser(String userIdentification) {
        if (userIdentification == null || userIdentification.trim().isEmpty()) {
            LOGGER.severe("[DATABASE] Username cannot be null or empty");
            return false;
        }
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(XMLfile);

            if (userExists(doc, userIdentification)) {
                LOGGER.info("[DATABASE] User already exists: " + userIdentification);
                return true;
            } else {
                Element root = doc.getDocumentElement();
                Element models = doc.createElement("TrainedModels");
                Element user = doc.createElement("User");
                user.setAttribute("UserID", userIdentification);
                user.setAttribute("SignInDate", getCurrentTimeStamp());
                user.appendChild(models);
                root.appendChild(user);
                saveDocument(doc);
                LOGGER.info("[DATABASE] User registered: " + userIdentification);
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            LOGGER.severe("[DATABASE] Failed to register user: " + e.getMessage());
        }
        return false;
    }

    /**
     * Registers metadata for a trained model under the specified user element.
     * The method appends a new <Model> element containing name, algorithm,
     * dataset and performance metrics, then saves the document.
     *
     * @param userID the owner user ID
     * @param modelName the name of the trained model
     * @param algorithm the algorithm used to train the model
     * @param dataset the dataset used for training
     * @param r2Score model R^2 score
     * @param mae model mean absolute error
     */
    public synchronized void registerTrainedModel(String userID, String modelName, String algorithm, 
                                                   String dataset, float r2Score, float mae) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(XMLfile);

            Element userElement = findUserElement(doc, userID);
            if (userElement == null) {
                LOGGER.severe("[DATABASE] User not found: " + userID);
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
            LOGGER.info("[DATABASE] Model registered: " + modelName + " for user " + userID);

        } catch (Exception e) {
            LOGGER.severe("[DATABASE] Failed to register model: " + e.getMessage());
        }
    }

    /**
     * Finds and returns the <User> element matching the given userID.
     *
     * @param doc the parsed XML document
     * @param userID the user identifier to search for
     * @return the matching User element, or {@code null} if not found
     */
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

    /**
     * Checks whether a user with the given identification already exists in the
     * provided document.
     *
     * @param doc the parsed XML document
     * @param userIdentification the user identifier to check
     * @return {@code true} if the user exists, {@code false} otherwise
     */
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

    /**
     * Returns the current timestamp formatted as "HH:mm:ss dd-MM-yyyy".
     *
     * @return formatted current timestamp string
     */
    private static String getCurrentTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy"));
    }

    /**
     * Persists the provided DOM Document to the configured XML file using a
     * Transformer. Configures indentation and attaches the DTD reference.
     *
     * @param doc the DOM Document to save
     */
    private void saveDocument(Document doc) {
        try {
            Node root = doc.getDocumentElement();
            if (root != null) {
                removeEmptyTextNodes(root);
            }

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();

            transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "database.dtd");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(new DOMSource(doc), new StreamResult(XMLfile));
        } catch (TransformerException e) {
            LOGGER.severe("[DATABASE] Failed to save document: " + e.getMessage());
        }
    }

    /**
     * Recursively removes text nodes that contain only whitespace from the DOM.
     * This prevents transformer pretty-printing from producing extra blank lines.
     * Helps to mantain a solid XML structure.
     * 
     * @param node node to clean
     */
    private void removeEmptyTextNodes(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().isEmpty()) {
                    node.removeChild(child);
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                removeEmptyTextNodes(child);
            }
        }
    }
}
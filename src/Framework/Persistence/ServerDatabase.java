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

    //public synchronized void registerTrainedModel(String userID, )

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
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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

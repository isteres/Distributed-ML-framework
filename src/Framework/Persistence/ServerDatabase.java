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

    private static final String FILE_PATH = "database.xml";
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

                Element root = doc.createElement("ServerUsers");
                doc.appendChild(root);

                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.transform(new DOMSource(doc), new StreamResult(XMLfile));

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
                Element user = doc.createElement("User");
                user.setAttribute("UserID", userIdentification);
                user.setAttribute("SignInDate", getCurrentTimeStamp());
                root.appendChild(user);
                saveDocument(doc);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to register user: " + e.getMessage());
            e.printStackTrace();
        }
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
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void saveDocument(Document doc) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(XMLfile));
        } catch (TransformerException e) {
            System.err.println("[ERROR] Failed to save document: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

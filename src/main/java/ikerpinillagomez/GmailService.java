package ikerpinillagomez;

import javax.mail.*;
import java.util.Properties;

public class GmailService {
    private final String USER = "";
    private final String PASSWORD = "";

    public Store conectarGmail() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imaps.host", "imap.gmail.com");
        props.put("mail.imaps.port", "993");

        Session session = Session.getDefaultInstance(props);
        Store store = session.getStore("imaps");
        store.connect(USER, PASSWORD);
        return store;
    }
}

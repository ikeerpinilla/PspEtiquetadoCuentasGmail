package ikerpinillagomez;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EtiquetaManager {
    private GmailService gmailService;
    private Map<String, List<Message>> mensajes;

    public EtiquetaManager(GmailService gmailService, Map<String, List<Message>> mensajes) {
        this.gmailService = gmailService;
        this.mensajes = mensajes;
    }

    public void cargarCorreosInbox(DefaultListModel<String> model) {
        try {
            Store store = gmailService.conectarGmail();
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            for (Message msg : inbox.getMessages()) {
                model.addElement(msg.getSubject() + " - " + msg.getFrom()[0]);
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error cargando inbox: " + e.getMessage());
        }
    }

    public void procesarEtiquetado() throws MessagingException {
        Store store = gmailService.conectarGmail();
        crearEtiquetasSiNoExisten(store);
        etiquetarCorreos(store);
        cargarMensajesEtiquetados(store);
        store.close();
    }

    private void crearEtiquetasSiNoExisten(Store store) throws MessagingException {
        String[] etiquetas = {"Hecho", "En progreso", "Por hacer"};
        for (String etiqueta : etiquetas) {
            Folder carpeta = store.getFolder(etiqueta);
            if (!carpeta.exists()) {
                carpeta.create(Folder.HOLDS_MESSAGES);
            }
        }
    }

    private void etiquetarCorreos(Store store) throws MessagingException {
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);
        Message[] mensajes = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

        if (mensajes.length < 7) {
            JOptionPane.showMessageDialog(null, "Se necesitan exactamente 7 correos no leídos");
            inbox.close(false);
            return;
        }

        Arrays.sort(mensajes, Comparator.comparing(m -> {
            try {
                return m.getSentDate();
            } catch (MessagingException e) {
                return new Date(0);
            }
        }, Comparator.reverseOrder()));

        int[] conteoEtiquetas = {3, 1, 3};
        String[] etiquetas = {"Hecho", "En progreso", "Por hacer"};
        int indiceActual = 0;

        for (int i = 0; i < etiquetas.length; i++) {
            Folder carpetaEtiqueta = store.getFolder(etiquetas[i]);
            Message[] paraEtiquetar = Arrays.copyOfRange(mensajes, indiceActual, indiceActual + conteoEtiquetas[i]);
            inbox.copyMessages(paraEtiquetar, carpetaEtiqueta);
            indiceActual += conteoEtiquetas[i];
        }
        inbox.close(false);
    }

    private void cargarMensajesEtiquetados(Store store) throws MessagingException {
        String[] etiquetas = {"Hecho", "En progreso", "Por hacer"};
        for (String etiqueta : etiquetas) {
            Folder carpeta = store.getFolder(etiqueta);
            carpeta.open(Folder.READ_ONLY);

            List<Message> mensajesValidos = new ArrayList<>();
            for (Message msg : carpeta.getMessages()) {
                try {
                    msg.getSubject();
                    msg.getFrom();
                    mensajesValidos.add(msg);
                } catch (MessagingException ignored) {}
            }
            mensajes.put(etiqueta, mensajesValidos);
            carpeta.close(false);
        }
    }

    public JScrollPane crearPanelEtiquetas() {
        JPanel mainPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        String[] etiquetas = {"Hecho", "En progreso", "Por hacer"};

        for (String etiqueta : etiquetas) {
            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(new TitledBorder(etiqueta));

            DefaultListModel<String> modelo = new DefaultListModel<>();
            if (mensajes.containsKey(etiqueta)) {
                for (Message msg : mensajes.get(etiqueta)) {
                    try {
                        String from = Arrays.toString(msg.getFrom()).replaceAll("[\\[\\]]", "");
                        modelo.addElement(msg.getSubject() + " - " + from);
                    } catch (MessagingException e) {
                        modelo.addElement("Error cargando mensaje - Formato inválido");
                    }
                }
            }

            panel.add(new JScrollPane(new JList<>(modelo)));
            mainPanel.add(panel);
        }
        return new JScrollPane(mainPanel);
    }
}

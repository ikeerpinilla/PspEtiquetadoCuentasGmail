package ikerpinillagomez;

import javax.mail.*;
import javax.mail.search.FlagTerm;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

public class VistaPrincipal extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardPanel = new JPanel(cardLayout);
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private Map<String, List<Message>> mensajes = new HashMap<>();
    private GmailService gmailService = new GmailService();
    private EtiquetaManager etiquetaManager;

    public VistaPrincipal() {
        super("Gestor de Etiquetas Gmail");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        etiquetaManager = new EtiquetaManager(gmailService, mensajes);
        inicializar();
    }

    private void inicializar() {
        cardPanel.add(crearPanel(), "inbox");
        cardPanel.add(new JPanel(), "labeled");
        add(cardPanel);
        cardLayout.show(cardPanel, "inbox");
    }

    private JPanel crearPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JList<String> emailList = new JList<>(listModel);
        JButton btnEtiquetar = new JButton("Etiquetar Correos");

        etiquetaManager.cargarCorreosInbox(listModel);

        btnEtiquetar.addActionListener(e -> {
            try {
                etiquetaManager.procesarEtiquetado();
                mostrarVistaEtiquetada();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        panel.add(new JScrollPane(emailList), BorderLayout.CENTER);
        panel.add(btnEtiquetar, BorderLayout.SOUTH);
        return panel;
    }

    private void mostrarVistaEtiquetada() {
        cardPanel.remove(1);
        cardPanel.add(etiquetaManager.crearPanelEtiquetas(), "labeled");
        cardLayout.show(cardPanel, "labeled");
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new VistaPrincipal().setVisible(true));
    }
}

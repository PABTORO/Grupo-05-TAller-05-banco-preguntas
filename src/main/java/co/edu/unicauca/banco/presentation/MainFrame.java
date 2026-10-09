package co.edu.unicauca.banco.presentation;

import co.edu.unicauca.banco.kernel.QuestionMicrokernel;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Ventana principal con tres pestañas: crear preguntas, ver el banco y gestionar plugins. */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public MainFrame(QuestionMicrokernel kernel) {
        super("Banco de Preguntas Saber PRO");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(860, 640);
        setLocationRelativeTo(null);

        BankPanel bankPanel = new BankPanel(kernel);
        PluginsPanel pluginsPanel = new PluginsPanel(kernel);
        CreateQuestionPanel createPanel = new CreateQuestionPanel(kernel, bankPanel::refresh);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Crear pregunta", createPanel);
        tabs.addTab("Banco de preguntas", bankPanel);
        tabs.addTab("Plugins", pluginsPanel);
        tabs.addChangeListener(e -> {
            bankPanel.refresh();
            pluginsPanel.refresh();
        });
        add(tabs);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                kernel.shutdown();
                dispose();
                System.exit(0);
            }
        });
    }
}

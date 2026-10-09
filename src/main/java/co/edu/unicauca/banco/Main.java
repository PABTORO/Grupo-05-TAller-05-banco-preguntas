package co.edu.unicauca.banco;

import co.edu.unicauca.banco.kernel.QuestionMicrokernel;
import co.edu.unicauca.banco.presentation.MainFrame;

import javax.swing.SwingUtilities;
import java.util.List;

/** Punto de entrada: arranca el microkernel (carga los plugins) y abre la ventana. */
public class Main {

    public static void main(String[] args) {
        QuestionMicrokernel kernel = new QuestionMicrokernel();
        List<String> loaded = kernel.start();
        System.out.println("Plugins cargados: " + loaded);

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(kernel);
            frame.setVisible(true);
        });
    }
}

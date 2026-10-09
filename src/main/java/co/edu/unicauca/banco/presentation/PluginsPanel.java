package co.edu.unicauca.banco.presentation;

import co.edu.unicauca.banco.kernel.PluginStatus;
import co.edu.unicauca.banco.kernel.QuestionMicrokernel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/** Muestra los plugins registrados y permite activarlos o desactivarlos en caliente. */
class PluginsPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final QuestionMicrokernel kernel;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {"Plugin", "Clase", "Estado"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private List<PluginStatus> shown = new ArrayList<>();

    PluginsPanel(QuestionMicrokernel kernel) {
        this.kernel = kernel;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton enable = new JButton("Activar");
        enable.addActionListener(e -> change(true));
        JButton disable = new JButton("Desactivar");
        disable.addActionListener(e -> change(false));
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(enable);
        buttons.add(disable);
        add(buttons, BorderLayout.SOUTH);

        refresh();
    }

    void refresh() {
        shown = kernel.getPlugins();
        model.setRowCount(0);
        for (PluginStatus s : shown) {
            model.addRow(new Object[] {s.name(), s.className(), s.state()});
        }
    }

    private void change(boolean enable) {
        int row = table.getSelectedRow();
        if (row < 0 || row >= shown.size()) {
            return;
        }
        String name = shown.get(row).name();
        if (enable) {
            kernel.enablePlugin(name);
        } else {
            kernel.disablePlugin(name);
        }
        refresh();
        table.getSelectionModel().setSelectionInterval(row, row);
    }
}

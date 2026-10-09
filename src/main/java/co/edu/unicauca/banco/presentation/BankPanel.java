package co.edu.unicauca.banco.presentation;

import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.kernel.QuestionMicrokernel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Lista las preguntas del banco y muestra el detalle de la seleccionada. */
class BankPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final QuestionMicrokernel kernel;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {"ID", "Tipo", "Título", "Clasificación", "Dificultad"}, 0) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JTextArea detail = new JTextArea();
    private List<Question> shown = new ArrayList<>();

    BankPanel(QuestionMicrokernel kernel) {
        this.kernel = kernel;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetail();
            }
        });
        detail.setEditable(false);
        detail.setLineWrap(true);
        detail.setWrapStyleWord(true);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(600, 200));
        JScrollPane detailScroll = new JScrollPane(detail);
        detailScroll.setPreferredSize(new Dimension(600, 280));
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, detailScroll);
        split.setResizeWeight(0.4);
        add(split, BorderLayout.CENTER);

        JButton delete = new JButton("Eliminar seleccionada");
        delete.addActionListener(e -> deleteSelected());
        JButton reload = new JButton("Actualizar");
        reload.addActionListener(e -> refresh());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(delete);
        buttons.add(reload);
        add(buttons, BorderLayout.SOUTH);
    }

    void refresh() {
        shown = new ArrayList<>(kernel.getAllQuestions());
        model.setRowCount(0);
        for (Question q : shown) {
            model.addRow(new Object[] {
                    q.getId().substring(0, 8), q.getType(), q.getTitle(),
                    nullToDash(q.getClassification()), nullToDash(q.getDifficulty())});
        }
        detail.setText("");
    }

    private void showDetail() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= shown.size()) {
            detail.setText("");
            return;
        }
        Question q = shown.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("ID: ").append(q.getId()).append('\n');
        sb.append("Tipo: ").append(q.getType()).append('\n');
        sb.append("Título: ").append(q.getTitle()).append("\n\n");
        sb.append(q.getContent()).append('\n');
        char letter = 'A';
        for (String option : q.getOptions()) {
            boolean correct = option.equals(q.getCorrectAnswer());
            sb.append('\n').append(letter++).append(") ").append(option).append(correct ? "   ✔ correcta" : "");
        }
        for (Map.Entry<String, String> extra : q.getExtras().entrySet()) {
            sb.append("\n\n").append(extra.getKey()).append(": ").append(extra.getValue());
        }
        detail.setText(sb.toString());
        detail.setCaretPosition(0);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row >= 0 && row < shown.size()) {
            kernel.removeQuestion(shown.get(row).getId());
            refresh();
        }
    }

    private static String nullToDash(String s) {
        return s == null ? "-" : s;
    }
}

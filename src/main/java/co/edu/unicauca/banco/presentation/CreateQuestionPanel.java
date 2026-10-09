package co.edu.unicauca.banco.presentation;

import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.domain.QuestionRequest;
import co.edu.unicauca.banco.domain.QuestionTypes;
import co.edu.unicauca.banco.kernel.PluginLoadException;
import co.edu.unicauca.banco.kernel.QuestionMicrokernel;
import co.edu.unicauca.banco.pipeline.ClassificationFilter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/** Formulario para pedirle al núcleo que genere una pregunta del tipo elegido. */
class CreateQuestionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private static final String AUTO = "(automática)";
    private static final int LABEL_WIDTH = 170;
    private static final String[] LETTERS = {"A", "B", "C", "D"};

    private final QuestionMicrokernel kernel;
    private final Runnable onCreated;

    private final JComboBox<String> typeCombo = new JComboBox<>(QuestionTypes.ALL.toArray(new String[0]));
    private final JTextField titleField = new JTextField();
    private final JTextArea contentArea = new JTextArea(4, 40);

    private final JTextField[] optionFields = {new JTextField(), new JTextField(), new JTextField(), new JTextField()};
    private final JComboBox<String> correctCombo = new JComboBox<>(LETTERS);
    private final JComboBox<String> classificationCombo;
    private final JComboBox<String> difficultyCombo;

    private final JTextArea caseArea = new JTextArea(5, 40);
    private final JComboBox<String> resourceTypeCombo = new JComboBox<>(new String[] {"IMAGE", "AUDIO", "VIDEO"});
    private final JTextField resourceUrlField = new JTextField();

    private final JPanel cards = new JPanel(new CardLayout());
    private final JLabel status = new JLabel(" ");

    CreateQuestionPanel(QuestionMicrokernel kernel, Runnable onCreated) {
        this.kernel = kernel;
        this.onCreated = onCreated;

        List<String> classifications = new ArrayList<>();
        classifications.add(AUTO);
        classifications.addAll(ClassificationFilter.classifications());
        classificationCombo = new JComboBox<>(classifications.toArray(new String[0]));

        List<String> difficulties = new ArrayList<>();
        difficulties.add(AUTO);
        difficulties.addAll(ClassificationFilter.DIFFICULTIES);
        difficultyCombo = new JComboBox<>(difficulties.toArray(new String[0]));

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(buildCommonFields(), BorderLayout.NORTH);
        cards.add(buildMultipleChoiceCard(), QuestionTypes.MULTIPLE_CHOICE);
        cards.add(buildCaseCard(), QuestionTypes.CASE_BASED);
        cards.add(buildMultimediaCard(), QuestionTypes.MULTIMEDIA);
        add(cards, BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        typeCombo.addActionListener(e ->
                ((CardLayout) cards.getLayout()).show(cards, (String) typeCombo.getSelectedItem()));
    }

    // ------------------------------------------------------------ construcción

    private JPanel buildCommonFields() {
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        JPanel p = new JPanel(new GridBagLayout());
        addRow(p, 0, "Tipo de pregunta:", typeCombo);
        addRow(p, 1, "Título:", titleField);
        addRow(p, 2, "Enunciado:", new JScrollPane(contentArea));
        return p;
    }

    private JPanel buildMultipleChoiceCard() {
        JPanel p = new JPanel(new GridBagLayout());
        for (int i = 0; i < optionFields.length; i++) {
            addRow(p, i, "Opción " + LETTERS[i] + ":", optionFields[i]);
        }
        addRow(p, 4, "Respuesta correcta:", correctCombo);
        addRow(p, 5, "Clasificación:", classificationCombo);
        addRow(p, 6, "Dificultad:", difficultyCombo);
        return wrapTop(p);
    }

    private JPanel buildCaseCard() {
        caseArea.setLineWrap(true);
        caseArea.setWrapStyleWord(true);
        JPanel p = new JPanel(new GridBagLayout());
        addRow(p, 0, "Descripción del caso:", new JScrollPane(caseArea));
        return wrapTop(p);
    }

    private JPanel buildMultimediaCard() {
        JPanel p = new JPanel(new GridBagLayout());
        addRow(p, 0, "Tipo de recurso:", resourceTypeCombo);
        addRow(p, 1, "URL o ruta del recurso:", resourceUrlField);
        return wrapTop(p);
    }

    private JPanel buildFooter() {
        JButton generate = new JButton("Generar pregunta");
        generate.addActionListener(e -> generate());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.add(generate);
        p.add(status);
        return p;
    }

    private static JPanel wrapTop(JPanel inner) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.add(inner, BorderLayout.NORTH);
        return outer;
    }

    private static void addRow(JPanel p, int row, String label, java.awt.Component field) {
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0;
        l.gridy = row;
        l.anchor = GridBagConstraints.NORTHWEST;
        l.insets = new Insets(4, 4, 4, 8);
        JLabel caption = new JLabel(label);
        caption.setPreferredSize(new Dimension(LABEL_WIDTH, caption.getPreferredSize().height));
        p.add(caption, l);

        GridBagConstraints f = new GridBagConstraints();
        f.gridx = 1;
        f.gridy = row;
        f.weightx = 1;
        f.fill = GridBagConstraints.HORIZONTAL;
        f.insets = new Insets(4, 0, 4, 4);
        p.add(field, f);
    }

    // ------------------------------------------------------------------ acción

    private void generate() {
        QuestionRequest request = buildRequest();
        try {
            Question q = kernel.generateQuestion(request);
            status.setText("Pregunta creada: " + q.getId().substring(0, 8));
            clearForm();
            onCreated.run();
        } catch (InvalidQuestionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Pregunta rechazada", JOptionPane.WARNING_MESSAGE);
            status.setText("Rechazada por " + e.getSource());
        } catch (PluginLoadException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Plugin no disponible", JOptionPane.ERROR_MESSAGE);
            status.setText("Sin plugin disponible");
        }
    }

    private QuestionRequest buildRequest() {
        String type = (String) typeCombo.getSelectedItem();
        QuestionRequest request = new QuestionRequest(type, titleField.getText(), contentArea.getText());

        switch (type) {
            case QuestionTypes.MULTIPLE_CHOICE -> {
                List<String> options = new ArrayList<>();
                for (JTextField field : optionFields) {
                    options.add(field.getText());
                }
                request.setOptions(options)
                        .setCorrectAnswer(options.get(correctCombo.getSelectedIndex()))
                        .setClassification(optionalValue(classificationCombo))
                        .setDifficulty(optionalValue(difficultyCombo));
            }
            case QuestionTypes.CASE_BASED ->
                    request.putExtra(QuestionRequest.CASE_DESCRIPTION, caseArea.getText());
            case QuestionTypes.MULTIMEDIA -> {
                request.putExtra(QuestionRequest.RESOURCE_TYPE, (String) resourceTypeCombo.getSelectedItem());
                request.putExtra(QuestionRequest.RESOURCE_URL, resourceUrlField.getText());
            }
            default -> { }
        }
        return request;
    }

    private static String optionalValue(JComboBox<String> combo) {
        String value = (String) combo.getSelectedItem();
        return AUTO.equals(value) ? "" : value;
    }

    private void clearForm() {
        titleField.setText("");
        contentArea.setText("");
        for (JTextField f : optionFields) {
            f.setText("");
        }
        caseArea.setText("");
        resourceUrlField.setText("");
    }
}

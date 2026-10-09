package co.edu.unicauca.banco.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Datos de entrada para que un plugin genere una pregunta. Es lo que recorre
 * el pipeline: cada filtro recibe una solicitud y entrega otra (posiblemente
 * enriquecida), sin modificar la original.
 */
public class QuestionRequest {

    public static final String CASE_DESCRIPTION = "caseDescription";
    public static final String RESOURCE_TYPE = "resourceType";
    public static final String RESOURCE_URL = "resourceUrl";

    private String type;
    private String title;
    private String content;
    private List<String> options = new ArrayList<>();
    private String correctAnswer;
    private String classification;
    private String difficulty;
    private final Map<String, String> extras = new LinkedHashMap<>();

    public QuestionRequest(String type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
    }

    /** Copia independiente de esta solicitud. */
    public QuestionRequest copy() {
        QuestionRequest c = new QuestionRequest(type, title, content);
        c.options = new ArrayList<>(options);
        c.correctAnswer = correctAnswer;
        c.classification = classification;
        c.difficulty = difficulty;
        c.extras.putAll(extras);
        return c;
    }

    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }

    public List<String> getOptions() { return Collections.unmodifiableList(options); }
    public QuestionRequest setOptions(List<String> options) {
        this.options = options == null ? new ArrayList<>() : new ArrayList<>(options);
        return this;
    }

    public String getCorrectAnswer() { return correctAnswer; }
    public QuestionRequest setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
        return this;
    }

    public String getClassification() { return classification; }
    public QuestionRequest setClassification(String classification) {
        this.classification = classification;
        return this;
    }

    public String getDifficulty() { return difficulty; }
    public QuestionRequest setDifficulty(String difficulty) {
        this.difficulty = difficulty;
        return this;
    }

    public Map<String, String> getExtras() { return Collections.unmodifiableMap(extras); }
    public String getExtra(String key) { return extras.get(key); }
    public QuestionRequest putExtra(String key, String value) {
        extras.put(key, value);
        return this;
    }
}

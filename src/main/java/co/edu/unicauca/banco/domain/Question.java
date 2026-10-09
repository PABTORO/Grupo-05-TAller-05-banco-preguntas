package co.edu.unicauca.banco.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pregunta almacenada en el banco. Los cuatro primeros atributos son el diseño
 * base del taller; el resto son datos específicos que llenan los plugins
 * (opciones, clasificación, recurso multimedia, caso, etc.).
 */
public class Question {

    private String id;
    private String title;
    private String content;
    private String type;

    private List<String> options = new ArrayList<>();
    private String correctAnswer;
    private String classification;
    private String difficulty;
    private final Map<String, String> extras = new LinkedHashMap<>();

    public Question(String id, String title, String content, String type) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getOptions() { return Collections.unmodifiableList(options); }
    public void setOptions(List<String> options) {
        this.options = options == null ? new ArrayList<>() : new ArrayList<>(options);
    }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public Map<String, String> getExtras() { return Collections.unmodifiableMap(extras); }
    public void putExtra(String key, String value) { extras.put(key, value); }

    @Override
    public String toString() {
        return "Question{id=" + id + ", type=" + type + ", title=" + title + "}";
    }
}

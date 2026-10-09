package co.edu.unicauca.banco.plugins;

import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.domain.QuestionRequest;
import co.edu.unicauca.banco.domain.QuestionTypes;
import co.edu.unicauca.banco.kernel.QuestionPlugin;

import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/** Genera preguntas que incluyen un recurso de imagen, audio o video. */
public class MultimediaQuestionPlugin implements QuestionPlugin {

    private static final Map<String, List<String>> EXTENSIONS = Map.of(
            "IMAGE", List.of(".png", ".jpg", ".jpeg", ".gif", ".webp"),
            "AUDIO", List.of(".mp3", ".wav", ".ogg"),
            "VIDEO", List.of(".mp4", ".webm", ".mov", ".avi"));

    @Override
    public String getName() {
        return "multimedia";
    }

    @Override
    public boolean supports(String type) {
        return QuestionTypes.MULTIMEDIA.equalsIgnoreCase(type);
    }

    @Override
    public Question generate(QuestionRequest request) {
        if (isBlank(request.getTitle()) || isBlank(request.getContent())) {
            throw new InvalidQuestionException(getName(), "El título y el enunciado son obligatorios");
        }

        String resourceType = request.getExtra(QuestionRequest.RESOURCE_TYPE);
        String resourceUrl = request.getExtra(QuestionRequest.RESOURCE_URL);

        if (isBlank(resourceType) || !EXTENSIONS.containsKey(resourceType.trim().toUpperCase(Locale.ROOT))) {
            throw new InvalidQuestionException(getName(),
                    "El tipo de recurso debe ser uno de: " + EXTENSIONS.keySet());
        }
        String kind = resourceType.trim().toUpperCase(Locale.ROOT);

        if (isBlank(resourceUrl)) {
            throw new InvalidQuestionException(getName(), "Debe indicar la URL o ruta del recurso");
        }
        validateResource(kind, resourceUrl.trim());

        Question question = new Question(UUID.randomUUID().toString(),
                request.getTitle().trim(), request.getContent().trim(), QuestionTypes.MULTIMEDIA);
        question.putExtra(QuestionRequest.RESOURCE_TYPE, kind);
        question.putExtra(QuestionRequest.RESOURCE_URL, resourceUrl.trim());
        return question;
    }

    private void validateResource(String kind, String url) {
        URI uri;
        try {
            uri = URI.create(url);
        } catch (IllegalArgumentException e) {
            throw new InvalidQuestionException(getName(), "La URL o ruta del recurso no es válida: " + url);
        }
        String lower = url.toLowerCase(Locale.ROOT);

        boolean streamingVideo = "VIDEO".equals(kind)
                && uri.getHost() != null
                && (uri.getHost().contains("youtube.com") || uri.getHost().contains("youtu.be"));
        boolean validExtension = EXTENSIONS.get(kind).stream().anyMatch(lower::endsWith);

        if (!streamingVideo && !validExtension) {
            throw new InvalidQuestionException(getName(),
                    "El recurso no corresponde a un " + kind + "; extensiones válidas: " + EXTENSIONS.get(kind));
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

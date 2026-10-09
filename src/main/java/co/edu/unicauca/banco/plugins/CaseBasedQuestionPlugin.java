package co.edu.unicauca.banco.plugins;

import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.domain.QuestionRequest;
import co.edu.unicauca.banco.domain.QuestionTypes;
import co.edu.unicauca.banco.kernel.QuestionPlugin;

import java.util.UUID;

/** Genera preguntas de análisis de casos: un caso descrito más una pregunta sobre él. */
public class CaseBasedQuestionPlugin implements QuestionPlugin {

    static final int MIN_CASE_LENGTH = 30;

    @Override
    public String getName() {
        return "case-based";
    }

    @Override
    public boolean supports(String type) {
        return QuestionTypes.CASE_BASED.equalsIgnoreCase(type);
    }

    @Override
    public Question generate(QuestionRequest request) {
        String title = request.getTitle();
        String content = request.getContent();
        String description = request.getExtra(QuestionRequest.CASE_DESCRIPTION);

        if (isBlank(title) || isBlank(content)) {
            throw new InvalidQuestionException(getName(), "El título y la pregunta sobre el caso son obligatorios");
        }
        if (isBlank(description) || description.trim().length() < MIN_CASE_LENGTH) {
            throw new InvalidQuestionException(getName(),
                    "La descripción del caso es obligatoria y debe tener al menos " + MIN_CASE_LENGTH + " caracteres");
        }

        Question question = new Question(UUID.randomUUID().toString(),
                title.trim(), content.trim(), QuestionTypes.CASE_BASED);
        question.putExtra(QuestionRequest.CASE_DESCRIPTION, description.trim());
        return question;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}

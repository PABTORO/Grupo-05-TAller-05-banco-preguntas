package co.edu.unicauca.banco.pipeline;

import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.QuestionRequest;

import java.util.List;

/** Verifica que exista una respuesta correcta y que sea una de las opciones. */
public class CorrectAnswerValidationFilter implements Filter {

    @Override
    public QuestionRequest process(QuestionRequest in) {
        String answer = in.getCorrectAnswer();
        List<String> options = in.getOptions();

        if (TextUtils.isBlank(answer)) {
            throw new InvalidQuestionException(getName(), "Debe indicar la respuesta correcta");
        }
        if (options.isEmpty()) {
            throw new InvalidQuestionException(getName(), "No hay opciones contra las cuales validar la respuesta");
        }
        for (String option : options) {
            if (TextUtils.normalize(option).equals(TextUtils.normalize(answer))) {
                // Se guarda el texto exacto de la opción para mantener los datos consistentes.
                return in.copy().setCorrectAnswer(option);
            }
        }
        throw new InvalidQuestionException(getName(),
                "La respuesta correcta '" + answer.trim() + "' no coincide con ninguna opción");
    }
}

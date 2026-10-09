package co.edu.unicauca.banco.pipeline;

import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.QuestionRequest;

/** Verifica que el título y el enunciado existan, tengan longitud mínima y texto real. */
public class ContentValidationFilter implements Filter {

    static final int MIN_TITLE = 5;
    static final int MAX_TITLE = 150;
    static final int MIN_CONTENT = 20;
    static final int MAX_CONTENT = 2000;

    @Override
    public QuestionRequest process(QuestionRequest in) {
        String title = in.getTitle();
        String content = in.getContent();

        if (TextUtils.isBlank(title)) {
            throw reject("El título no puede estar vacío");
        }
        if (title.trim().length() < MIN_TITLE || title.trim().length() > MAX_TITLE) {
            throw reject("El título debe tener entre " + MIN_TITLE + " y " + MAX_TITLE + " caracteres");
        }
        if (TextUtils.isBlank(content)) {
            throw reject("El enunciado no puede estar vacío");
        }
        if (content.trim().length() < MIN_CONTENT) {
            throw reject("El enunciado debe tener al menos " + MIN_CONTENT + " caracteres");
        }
        if (content.trim().length() > MAX_CONTENT) {
            throw reject("El enunciado no puede superar " + MAX_CONTENT + " caracteres");
        }
        if (content.chars().noneMatch(Character::isLetter)) {
            throw reject("El enunciado debe contener texto, no solo números o símbolos");
        }
        return in;
    }

    private InvalidQuestionException reject(String message) {
        return new InvalidQuestionException(getName(), message);
    }
}

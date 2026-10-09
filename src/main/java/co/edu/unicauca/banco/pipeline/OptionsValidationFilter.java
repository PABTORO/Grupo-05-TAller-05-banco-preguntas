package co.edu.unicauca.banco.pipeline;

import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.QuestionRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Verifica que haya exactamente cuatro opciones, ninguna vacía y ninguna repetida. */
public class OptionsValidationFilter implements Filter {

    public static final int REQUIRED_OPTIONS = 4;

    private final int required;

    public OptionsValidationFilter() {
        this(REQUIRED_OPTIONS);
    }

    public OptionsValidationFilter(int required) {
        this.required = required;
    }

    @Override
    public QuestionRequest process(QuestionRequest in) {
        List<String> options = in.getOptions();

        if (options.size() != required) {
            throw reject("Se requieren exactamente " + required + " opciones y llegaron " + options.size());
        }
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            if (TextUtils.isBlank(option)) {
                throw reject("La opción " + (i + 1) + " está vacía");
            }
            if (!seen.add(TextUtils.normalize(option))) {
                throw reject("La opción '" + option.trim() + "' está repetida");
            }
        }
        return in;
    }

    private InvalidQuestionException reject(String message) {
        return new InvalidQuestionException(getName(), message);
    }
}

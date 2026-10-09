package co.edu.unicauca.banco.pipeline;

import co.edu.unicauca.banco.domain.QuestionRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Tubería que pasa la solicitud por sus filtros, en orden, y se detiene en el primer rechazo. */
public class Pipeline {

    private final List<Filter> filters = new ArrayList<>();

    public Pipeline addFilter(Filter filter) {
        filters.add(filter);
        return this;
    }

    public QuestionRequest execute(QuestionRequest input) {
        QuestionRequest current = input;
        for (Filter filter : filters) {
            current = filter.process(current);
        }
        return current;
    }

    public List<String> getFilterNames() {
        List<String> names = new ArrayList<>();
        for (Filter f : filters) {
            names.add(f.getName());
        }
        return Collections.unmodifiableList(names);
    }

    /** Pipeline del taller: contenido, opciones, clasificación y respuesta correcta. */
    public static Pipeline standard() {
        return new Pipeline()
                .addFilter(new ContentValidationFilter())
                .addFilter(new OptionsValidationFilter())
                .addFilter(new ClassificationFilter())
                .addFilter(new CorrectAnswerValidationFilter());
    }
}

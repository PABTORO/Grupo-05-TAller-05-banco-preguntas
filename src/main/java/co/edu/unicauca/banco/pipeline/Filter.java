package co.edu.unicauca.banco.pipeline;

import co.edu.unicauca.banco.domain.QuestionRequest;

/**
 * Filtro del pipeline: recibe una solicitud, la valida o enriquece y entrega
 * el resultado al siguiente filtro. Si la solicitud es inválida lanza
 * {@link co.edu.unicauca.banco.domain.InvalidQuestionException}.
 */
@FunctionalInterface
public interface Filter {

    QuestionRequest process(QuestionRequest input);

    default String getName() {
        return getClass().getSimpleName();
    }
}

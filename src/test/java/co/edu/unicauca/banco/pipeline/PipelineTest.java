package co.edu.unicauca.banco.pipeline;

import co.edu.unicauca.banco.TestData;
import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.QuestionRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PipelineTest {

    @Test
    void standardPipelineHasTheFourFiltersInOrder() {
        assertEquals(List.of("ContentValidationFilter", "OptionsValidationFilter",
                        "ClassificationFilter", "CorrectAnswerValidationFilter"),
                Pipeline.standard().getFilterNames());
    }

    @Test
    void standardPipelineProcessesAValidQuestion() {
        QuestionRequest out = Pipeline.standard().execute(TestData.validMultipleChoice());

        assertEquals("Arquitectura de software", out.getClassification());
        assertEquals("BAJA", out.getDifficulty());
        assertEquals("Permite agregar funcionalidad mediante plugins", out.getCorrectAnswer());
    }

    @Test
    void standardPipelineRejectsInvalidQuestionAtTheRightFilter() {
        QuestionRequest bad = TestData.validMultipleChoice().setOptions(List.of("a", "b", "c"));

        InvalidQuestionException e = assertThrows(InvalidQuestionException.class,
                () -> Pipeline.standard().execute(bad));

        assertEquals("OptionsValidationFilter", e.getSource());
    }

    @Test
    void filtersRunInOrderAndEachOneReceivesThePreviousOutput() {
        List<String> trace = new ArrayList<>();
        Pipeline pipeline = new Pipeline()
                .addFilter(in -> {
                    trace.add("uno");
                    return in.copy().setClassification("marcado-por-uno");
                })
                .addFilter(in -> {
                    trace.add("dos:" + in.getClassification());
                    return in;
                });

        pipeline.execute(TestData.validMultipleChoice());

        assertEquals(List.of("uno", "dos:marcado-por-uno"), trace);
    }

    @Test
    void pipelineStopsAtTheFirstRejection() {
        List<String> trace = new ArrayList<>();
        Pipeline pipeline = new Pipeline()
                .addFilter(in -> {
                    throw new InvalidQuestionException("falla", "corte");
                })
                .addFilter(in -> {
                    trace.add("no debería ejecutarse");
                    return in;
                });

        assertThrows(InvalidQuestionException.class, () -> pipeline.execute(TestData.validMultipleChoice()));
        assertEquals(0, trace.size());
    }

    @Test
    void emptyPipelineReturnsTheInputUntouched() {
        QuestionRequest in = TestData.validMultipleChoice();

        assertEquals(in, new Pipeline().execute(in));
    }
}

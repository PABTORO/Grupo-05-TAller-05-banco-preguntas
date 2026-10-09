package co.edu.unicauca.banco.pipeline;

import co.edu.unicauca.banco.TestData;
import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.QuestionRequest;
import co.edu.unicauca.banco.domain.QuestionTypes;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FiltersTest {

    private static QuestionRequest request(String title, String content) {
        return new QuestionRequest(QuestionTypes.MULTIPLE_CHOICE, title, content);
    }

    private static String repeat(char c, int times) {
        return String.valueOf(c).repeat(times);
    }

    // ------------------------------------------------ ContentValidationFilter

    @Test
    void contentFilterAcceptsValidQuestion() {
        QuestionRequest in = TestData.validMultipleChoice();
        assertSame(in, new ContentValidationFilter().process(in));
    }

    @Test
    void contentFilterRejectsBlankOrShortTitle() {
        ContentValidationFilter filter = new ContentValidationFilter();
        String content = "Un enunciado suficientemente largo para pasar";

        assertThrows(InvalidQuestionException.class, () -> filter.process(request(null, content)));
        assertThrows(InvalidQuestionException.class, () -> filter.process(request("   ", content)));
        assertThrows(InvalidQuestionException.class, () -> filter.process(request("abc", content)));
        assertThrows(InvalidQuestionException.class, () -> filter.process(request(repeat('t', 151), content)));
    }

    @Test
    void contentFilterRejectsBlankShortOrLongContent() {
        ContentValidationFilter filter = new ContentValidationFilter();

        assertThrows(InvalidQuestionException.class, () -> filter.process(request("Titulo valido", null)));
        assertThrows(InvalidQuestionException.class, () -> filter.process(request("Titulo valido", "   ")));
        assertThrows(InvalidQuestionException.class, () -> filter.process(request("Titulo valido", "muy corto")));
        assertThrows(InvalidQuestionException.class,
                () -> filter.process(request("Titulo valido", repeat('a', 2001))));
    }

    @Test
    void contentFilterRejectsContentWithoutLetters() {
        ContentValidationFilter filter = new ContentValidationFilter();

        assertThrows(InvalidQuestionException.class,
                () -> filter.process(request("Titulo valido", "1234567890 !!!! ???? 12345")));
    }

    @Test
    void rejectionNamesTheFilterThatFailed() {
        InvalidQuestionException e = assertThrows(InvalidQuestionException.class,
                () -> new ContentValidationFilter().process(request("", "x")));

        assertEquals("ContentValidationFilter", e.getSource());
    }

    // ------------------------------------------------ OptionsValidationFilter

    @Test
    void optionsFilterAcceptsFourDistinctOptions() {
        QuestionRequest in = TestData.validMultipleChoice();
        assertSame(in, new OptionsValidationFilter().process(in));
    }

    @Test
    void optionsFilterRejectsWrongNumberOfOptions() {
        OptionsValidationFilter filter = new OptionsValidationFilter();
        QuestionRequest three = TestData.validMultipleChoice().setOptions(List.of("a", "b", "c"));
        QuestionRequest five = TestData.validMultipleChoice().setOptions(List.of("a", "b", "c", "d", "e"));

        assertThrows(InvalidQuestionException.class, () -> filter.process(three));
        assertThrows(InvalidQuestionException.class, () -> filter.process(five));
        assertThrows(InvalidQuestionException.class, () -> filter.process(request("Titulo valido", "x")));
    }

    @Test
    void optionsFilterRejectsBlankOption() {
        QuestionRequest in = TestData.validMultipleChoice().setOptions(List.of("a", "b", "  ", "d"));

        assertThrows(InvalidQuestionException.class, () -> new OptionsValidationFilter().process(in));
    }

    @Test
    void optionsFilterRejectsDuplicatesIgnoringCaseAccentsAndSpaces() {
        QuestionRequest in = TestData.validMultipleChoice().setOptions(List.of("Sí", "otra", " si ", "última"));

        assertThrows(InvalidQuestionException.class, () -> new OptionsValidationFilter().process(in));
    }

    @Test
    void optionsFilterCanBeConfiguredWithAnotherCount() {
        QuestionRequest two = TestData.validMultipleChoice().setOptions(List.of("a", "b"));

        assertSame(two, new OptionsValidationFilter(2).process(two));
    }

    // -------------------------------------------------- ClassificationFilter

    @Test
    void classificationIsInferredFromKeywords() {
        QuestionRequest in = request("Patrón Microkernel", "Explique la arquitectura basada en un núcleo y plugins.");

        assertEquals("Arquitectura de software", new ClassificationFilter().process(in).getClassification());
    }

    @Test
    void classificationPicksTheCompetencyWithMostKeywordHits() {
        QuestionRequest in = request("Pruebas unitarias",
                "Con JUnit se escribe una prueba para medir la cobertura de un método en una clase.");

        assertEquals("Pruebas de software", new ClassificationFilter().process(in).getClassification());
    }

    @Test
    void explicitClassificationIsNormalizedToTheCatalogSpelling() {
        QuestionRequest in = TestData.validMultipleChoice().setClassification("  INGENIERIA de requisitos ");

        assertEquals("Ingeniería de requisitos", new ClassificationFilter().process(in).getClassification());
    }

    @Test
    void invalidExplicitClassificationIsRejected() {
        QuestionRequest in = TestData.validMultipleChoice().setClassification("Astrología");

        assertThrows(InvalidQuestionException.class, () -> new ClassificationFilter().process(in));
    }

    @Test
    void classificationFailsWhenItCannotBeInferred() {
        QuestionRequest in = request("Cocina", "Describa cómo se prepara un buen ajiaco santafereño.");

        assertThrows(InvalidQuestionException.class, () -> new ClassificationFilter().process(in));
    }

    @Test
    void difficultyIsAssignedByContentLength() {
        ClassificationFilter filter = new ClassificationFilter();
        String base = "arquitectura ";

        assertEquals("BAJA", filter.process(request("Titulo", base + repeat('x', 20))).getDifficulty());
        assertEquals("MEDIA", filter.process(request("Titulo", base + repeat('x', 150))).getDifficulty());
        assertEquals("ALTA", filter.process(request("Titulo", base + repeat('x', 300))).getDifficulty());
    }

    @Test
    void explicitDifficultyIsNormalizedAndValidated() {
        ClassificationFilter filter = new ClassificationFilter();

        assertEquals("ALTA", filter.process(TestData.validMultipleChoice().setDifficulty(" alta ")).getDifficulty());
        assertThrows(InvalidQuestionException.class,
                () -> filter.process(TestData.validMultipleChoice().setDifficulty("imposible")));
    }

    @Test
    void classificationFilterDoesNotModifyItsInput() {
        QuestionRequest in = TestData.validMultipleChoice();

        QuestionRequest out = new ClassificationFilter().process(in);

        assertNull(in.getClassification());
        assertNull(in.getDifficulty());
        assertEquals("Arquitectura de software", out.getClassification());
    }

    // -------------------------------------------- CorrectAnswerValidationFilter

    @Test
    void correctAnswerFilterAcceptsAnswerAmongOptions() {
        QuestionRequest in = TestData.validMultipleChoice();

        assertEquals(in.getCorrectAnswer(), new CorrectAnswerValidationFilter().process(in).getCorrectAnswer());
    }

    @Test
    void correctAnswerIsNormalizedToTheExactOptionText() {
        QuestionRequest in = TestData.validMultipleChoice()
                .setCorrectAnswer("  PERMITE AGREGAR funcionalidad mediante PLUGINS ");

        assertEquals("Permite agregar funcionalidad mediante plugins",
                new CorrectAnswerValidationFilter().process(in).getCorrectAnswer());
    }

    @Test
    void correctAnswerFilterRejectsBlankMissingOrForeignAnswer() {
        CorrectAnswerValidationFilter filter = new CorrectAnswerValidationFilter();

        assertThrows(InvalidQuestionException.class,
                () -> filter.process(TestData.validMultipleChoice().setCorrectAnswer(null)));
        assertThrows(InvalidQuestionException.class,
                () -> filter.process(TestData.validMultipleChoice().setCorrectAnswer("  ")));
        assertThrows(InvalidQuestionException.class,
                () -> filter.process(TestData.validMultipleChoice().setCorrectAnswer("otra cosa")));
    }

    @Test
    void correctAnswerFilterRejectsRequestWithoutOptions() {
        QuestionRequest in = request("Titulo valido", "Un enunciado suficientemente largo").setCorrectAnswer("a");

        assertThrows(InvalidQuestionException.class, () -> new CorrectAnswerValidationFilter().process(in));
    }
}

package co.edu.unicauca.banco.plugins;

import co.edu.unicauca.banco.TestData;
import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.domain.QuestionRequest;
import co.edu.unicauca.banco.domain.QuestionTypes;
import co.edu.unicauca.banco.pipeline.Pipeline;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginsTest {

    // ---------------------------------------------- MultipleChoiceQuestionPlugin

    private final MultipleChoiceQuestionPlugin multipleChoice = new MultipleChoiceQuestionPlugin();

    @Test
    void multipleChoiceSupportsOnlyItsType() {
        assertEquals("multiple-choice", multipleChoice.getName());
        assertTrue(multipleChoice.supports("MULTIPLE_CHOICE"));
        assertTrue(multipleChoice.supports("multiple_choice"));
        assertFalse(multipleChoice.supports("CASE_BASED"));
        assertFalse(multipleChoice.supports(null));
    }

    @Test
    void multipleChoiceBuildsQuestionWithPipelineResults() {
        Question q = multipleChoice.generate(TestData.validMultipleChoice());

        assertNotNull(q.getId());
        assertEquals(QuestionTypes.MULTIPLE_CHOICE, q.getType());
        assertEquals(4, q.getOptions().size());
        assertEquals("Permite agregar funcionalidad mediante plugins", q.getCorrectAnswer());
        assertEquals("Arquitectura de software", q.getClassification());
        assertEquals("BAJA", q.getDifficulty());
    }

    @Test
    void multipleChoiceGeneratesADifferentIdEachTime() {
        Question a = multipleChoice.generate(TestData.validMultipleChoice());
        Question b = multipleChoice.generate(TestData.validMultipleChoice());

        assertFalse(a.getId().equals(b.getId()));
    }

    @Test
    void multipleChoiceRefusesToCreateQuestionsThatFailTheValidation() {
        QuestionRequest duplicated = TestData.validMultipleChoice()
                .setOptions(List.of("igual", "Igual", "otra", "más"));

        InvalidQuestionException e = assertThrows(InvalidQuestionException.class,
                () -> multipleChoice.generate(duplicated));
        assertEquals("OptionsValidationFilter", e.getSource());
    }

    @Test
    void multipleChoiceAcceptsAnInjectedPipeline() {
        MultipleChoiceQuestionPlugin lenient = new MultipleChoiceQuestionPlugin(new Pipeline());
        QuestionRequest in = TestData.validMultipleChoice().setCorrectAnswer("cualquier cosa");

        assertEquals("cualquier cosa", lenient.generate(in).getCorrectAnswer());
    }

    // ------------------------------------------------- CaseBasedQuestionPlugin

    private final CaseBasedQuestionPlugin caseBased = new CaseBasedQuestionPlugin();

    @Test
    void caseBasedSupportsOnlyItsType() {
        assertEquals("case-based", caseBased.getName());
        assertTrue(caseBased.supports("case_based"));
        assertFalse(caseBased.supports("MULTIMEDIA"));
    }

    @Test
    void caseBasedStoresTheCaseDescription() {
        Question q = caseBased.generate(TestData.validCase());

        assertEquals(QuestionTypes.CASE_BASED, q.getType());
        assertTrue(q.getExtras().get(QuestionRequest.CASE_DESCRIPTION).startsWith("Una universidad"));
    }

    @Test
    void caseBasedRequiresACaseOfMinimumLength() {
        QuestionRequest none = new QuestionRequest(QuestionTypes.CASE_BASED, "Titulo", "¿Pregunta?");
        QuestionRequest shortCase = new QuestionRequest(QuestionTypes.CASE_BASED, "Titulo", "¿Pregunta?")
                .putExtra(QuestionRequest.CASE_DESCRIPTION, "muy corto");

        assertThrows(InvalidQuestionException.class, () -> caseBased.generate(none));
        assertThrows(InvalidQuestionException.class, () -> caseBased.generate(shortCase));
    }

    @Test
    void caseBasedRequiresTitleAndQuestion() {
        String description = "Una descripción de caso lo bastante larga para ser aceptada.";
        QuestionRequest noTitle = new QuestionRequest(QuestionTypes.CASE_BASED, " ", "¿Pregunta?")
                .putExtra(QuestionRequest.CASE_DESCRIPTION, description);
        QuestionRequest noContent = new QuestionRequest(QuestionTypes.CASE_BASED, "Titulo", null)
                .putExtra(QuestionRequest.CASE_DESCRIPTION, description);

        assertThrows(InvalidQuestionException.class, () -> caseBased.generate(noTitle));
        assertThrows(InvalidQuestionException.class, () -> caseBased.generate(noContent));
    }

    // -------------------------------------------- MultimediaQuestionPlugin

    private final MultimediaQuestionPlugin multimedia = new MultimediaQuestionPlugin();

    private static QuestionRequest media(String type, String url) {
        QuestionRequest r = new QuestionRequest(QuestionTypes.MULTIMEDIA, "Titulo", "Observe el recurso y responda.");
        if (type != null) {
            r.putExtra(QuestionRequest.RESOURCE_TYPE, type);
        }
        if (url != null) {
            r.putExtra(QuestionRequest.RESOURCE_URL, url);
        }
        return r;
    }

    @Test
    void multimediaSupportsOnlyItsType() {
        assertEquals("multimedia", multimedia.getName());
        assertTrue(multimedia.supports("MULTIMEDIA"));
        assertFalse(multimedia.supports("CASE_BASED"));
    }

    @Test
    void multimediaStoresTheResource() {
        Question q = multimedia.generate(TestData.validMultimedia());

        assertEquals("IMAGE", q.getExtras().get(QuestionRequest.RESOURCE_TYPE));
        assertEquals("https://ejemplo.edu.co/recursos/diagrama.png", q.getExtras().get(QuestionRequest.RESOURCE_URL));
    }

    @Test
    void multimediaAcceptsAudioVideoAndStreamingLinks() {
        assertEquals("AUDIO", multimedia.generate(media("audio", "clips/entrevista.MP3"))
                .getExtras().get(QuestionRequest.RESOURCE_TYPE));
        assertEquals("VIDEO", multimedia.generate(media("VIDEO", "https://www.youtube.com/watch?v=abc123"))
                .getExtras().get(QuestionRequest.RESOURCE_TYPE));
        assertEquals("VIDEO", multimedia.generate(media("VIDEO", "videos/clase.mp4"))
                .getExtras().get(QuestionRequest.RESOURCE_TYPE));
    }

    @Test
    void multimediaRejectsUnknownOrMissingResourceType() {
        assertThrows(InvalidQuestionException.class, () -> multimedia.generate(media(null, "a.png")));
        assertThrows(InvalidQuestionException.class, () -> multimedia.generate(media("HOLOGRAM", "a.png")));
    }

    @Test
    void multimediaRejectsMissingUrl() {
        assertThrows(InvalidQuestionException.class, () -> multimedia.generate(media("IMAGE", null)));
        assertThrows(InvalidQuestionException.class, () -> multimedia.generate(media("IMAGE", "  ")));
    }

    @Test
    void multimediaRejectsExtensionThatDoesNotMatchTheType() {
        assertThrows(InvalidQuestionException.class, () -> multimedia.generate(media("AUDIO", "foto.png")));
        assertThrows(InvalidQuestionException.class, () -> multimedia.generate(media("IMAGE", "documento.pdf")));
        assertThrows(InvalidQuestionException.class,
                () -> multimedia.generate(media("IMAGE", "https://www.youtube.com/watch?v=abc")));
    }

    @Test
    void multimediaRejectsMalformedUrl() {
        assertThrows(InvalidQuestionException.class, () -> multimedia.generate(media("IMAGE", "http://a b/c.png")));
    }

    @Test
    void multimediaRequiresTitleAndStatement() {
        QuestionRequest noTitle = new QuestionRequest(QuestionTypes.MULTIMEDIA, "", "Enunciado")
                .putExtra(QuestionRequest.RESOURCE_TYPE, "IMAGE")
                .putExtra(QuestionRequest.RESOURCE_URL, "a.png");

        assertThrows(InvalidQuestionException.class, () -> multimedia.generate(noTitle));
    }
}

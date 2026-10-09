package co.edu.unicauca.banco.kernel;

import co.edu.unicauca.banco.TestData;
import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.domain.QuestionRequest;
import co.edu.unicauca.banco.domain.QuestionTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionMicrokernelTest {

    private QuestionMicrokernel kernel;

    @BeforeEach
    void setUp() {
        kernel = new QuestionMicrokernel();
        kernel.start(); // lee el /plugins.properties real del proyecto
    }

    @Test
    void startLoadsThePluginsDeclaredInPluginsProperties() {
        List<String> names = kernel.getPlugins().stream().map(PluginStatus::name).toList();

        assertEquals(3, names.size());
        assertTrue(names.containsAll(List.of("multiple-choice", "case-based", "multimedia")));
    }

    @Test
    void generatedQuestionIsStoredInTheBank() {
        Question q = kernel.generateQuestion(TestData.validMultipleChoice());

        assertEquals(1, kernel.questionCount());
        assertEquals(q, kernel.getQuestion(q.getId()).orElseThrow());
    }

    @Test
    void kernelRoutesEachTypeToItsOwnPlugin() {
        Question mc = kernel.generateQuestion(TestData.validMultipleChoice());
        Question cs = kernel.generateQuestion(TestData.validCase());
        Question mm = kernel.generateQuestion(TestData.validMultimedia());

        assertEquals(QuestionTypes.MULTIPLE_CHOICE, mc.getType());
        assertEquals(QuestionTypes.CASE_BASED, cs.getType());
        assertEquals(QuestionTypes.MULTIMEDIA, mm.getType());
        assertEquals(3, kernel.getAllQuestions().size());
    }

    @Test
    void typeMatchingIgnoresCase() {
        QuestionRequest request = TestData.validMultipleChoice();
        QuestionRequest lower = new QuestionRequest("multiple_choice", request.getTitle(), request.getContent())
                .setOptions(request.getOptions())
                .setCorrectAnswer(request.getCorrectAnswer());

        assertEquals(QuestionTypes.MULTIPLE_CHOICE, kernel.generateQuestion(lower).getType());
    }

    @Test
    void unknownTypeFailsWithoutStoringAnything() {
        QuestionRequest request = new QuestionRequest("HOLOGRAM", "Titulo", "Contenido de prueba largo");

        assertThrows(PluginLoadException.class, () -> kernel.generateQuestion(request));
        assertEquals(0, kernel.questionCount());
    }

    @Test
    void invalidQuestionIsRejectedAndNotStored() {
        QuestionRequest bad = TestData.validMultipleChoice().setCorrectAnswer("no es ninguna opción");

        assertThrows(InvalidQuestionException.class, () -> kernel.generateQuestion(bad));
        assertEquals(0, kernel.questionCount());
    }

    @Test
    void disabledPluginCannotGenerateUntilReactivated() {
        kernel.disablePlugin("multiple-choice");
        assertThrows(PluginLoadException.class, () -> kernel.generateQuestion(TestData.validMultipleChoice()));

        kernel.enablePlugin("multiple-choice");
        assertEquals(QuestionTypes.MULTIPLE_CHOICE,
                kernel.generateQuestion(TestData.validMultipleChoice()).getType());
    }

    @Test
    void newPluginCanBeRegisteredWithoutTouchingTheKernel() {
        kernel.registerPlugin(new RecordingPlugin());

        Question q = kernel.generateQuestion(new QuestionRequest("RECORDING", "Titulo", "Contenido"));

        assertEquals("RECORDING", q.getType());
    }

    @Test
    void unloadedPluginNoLongerServesItsType() {
        kernel.unloadPlugin("case-based");

        assertThrows(PluginLoadException.class, () -> kernel.generateQuestion(TestData.validCase()));
    }

    @Test
    void removeQuestionDeletesItFromTheBank() {
        Question q = kernel.generateQuestion(TestData.validCase());

        assertTrue(kernel.removeQuestion(q.getId()));
        assertFalse(kernel.removeQuestion(q.getId()));
        assertTrue(kernel.getQuestion(q.getId()).isEmpty());
    }

    @Test
    void shutdownDisablesAllPlugins() {
        kernel.shutdown();

        assertTrue(kernel.getPlugins().stream().allMatch(p -> p.state() == PluginState.DISABLED));
    }
}

package co.edu.unicauca.banco;

import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.domain.QuestionRequest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Pruebas de las clases de dominio: copias defensivas e inmutabilidad hacia afuera. */
class QuestionRequestTest {

    @Test
    void copyIsIndependentFromTheOriginal() {
        QuestionRequest original = TestData.validMultipleChoice().setClassification("A");

        QuestionRequest copy = original.copy().setClassification("B");

        assertEquals("A", original.getClassification());
        assertEquals("B", copy.getClassification());
        assertEquals(original.getOptions(), copy.getOptions());
    }

    @Test
    void requestKeepsItsOwnCopyOfTheOptions() {
        List<String> options = new ArrayList<>(List.of("a", "b", "c", "d"));
        QuestionRequest request = TestData.validMultipleChoice().setOptions(options);

        options.clear();

        assertEquals(4, request.getOptions().size());
    }

    @Test
    void exposedCollectionsCannotBeModified() {
        QuestionRequest request = TestData.validCase();
        Question question = new Question("id", "t", "c", "T");

        assertThrows(UnsupportedOperationException.class, () -> request.getOptions().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> request.getExtras().put("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> question.getOptions().add("x"));
        assertThrows(UnsupportedOperationException.class, () -> question.getExtras().put("k", "v"));
    }

    @Test
    void nullOptionsBecomeAnEmptyList() {
        assertEquals(0, TestData.validMultipleChoice().setOptions(null).getOptions().size());
        Question q = new Question("id", "t", "c", "T");
        q.setOptions(null);
        assertEquals(0, q.getOptions().size());
    }
}

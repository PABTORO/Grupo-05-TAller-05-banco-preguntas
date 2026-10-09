package co.edu.unicauca.banco;

import co.edu.unicauca.banco.domain.QuestionRequest;
import co.edu.unicauca.banco.domain.QuestionTypes;

import java.util.List;

/** Solicitudes de ejemplo compartidas por las pruebas. */
public final class TestData {

    private TestData() { }

    public static QuestionRequest validMultipleChoice() {
        return new QuestionRequest(QuestionTypes.MULTIPLE_CHOICE,
                "Patrón Microkernel",
                "¿Cuál es la principal ventaja de la arquitectura Microkernel en un sistema extensible?")
                .setOptions(List.of(
                        "Permite agregar funcionalidad mediante plugins",
                        "Elimina la necesidad de pruebas",
                        "Obliga a usar una sola base de datos",
                        "Impide el acoplamiento débil"))
                .setCorrectAnswer("Permite agregar funcionalidad mediante plugins");
    }

    public static QuestionRequest validCase() {
        return new QuestionRequest(QuestionTypes.CASE_BASED,
                "Caso de la universidad",
                "¿Qué patrón arquitectónico recomendaría para el sistema descrito?")
                .putExtra(QuestionRequest.CASE_DESCRIPTION,
                        "Una universidad necesita incorporar nuevos tipos de preguntas sin modificar el núcleo.");
    }

    public static QuestionRequest validMultimedia() {
        return new QuestionRequest(QuestionTypes.MULTIMEDIA,
                "Diagrama de componentes",
                "Observe el diagrama y responda qué componente actúa como núcleo.")
                .putExtra(QuestionRequest.RESOURCE_TYPE, "IMAGE")
                .putExtra(QuestionRequest.RESOURCE_URL, "https://ejemplo.edu.co/recursos/diagrama.png");
    }
}

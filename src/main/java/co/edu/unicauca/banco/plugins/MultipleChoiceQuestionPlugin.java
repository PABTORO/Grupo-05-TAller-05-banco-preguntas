package co.edu.unicauca.banco.plugins;

import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.domain.QuestionRequest;
import co.edu.unicauca.banco.domain.QuestionTypes;
import co.edu.unicauca.banco.kernel.QuestionPlugin;
import co.edu.unicauca.banco.pipeline.Pipeline;

import java.util.UUID;

/**
 * Genera preguntas de selección múltiple. Es el plugin que implementa el
 * pipeline del taller: nada se crea si la solicitud no supera los cuatro filtros.
 */
public class MultipleChoiceQuestionPlugin implements QuestionPlugin {

    private final Pipeline pipeline;

    public MultipleChoiceQuestionPlugin() {
        this(Pipeline.standard());
    }

    public MultipleChoiceQuestionPlugin(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    public String getName() {
        return "multiple-choice";
    }

    @Override
    public boolean supports(String type) {
        return QuestionTypes.MULTIPLE_CHOICE.equalsIgnoreCase(type);
    }

    @Override
    public Question generate(QuestionRequest request) {
        QuestionRequest processed = pipeline.execute(request);

        Question question = new Question(UUID.randomUUID().toString(),
                processed.getTitle().trim(), processed.getContent().trim(), QuestionTypes.MULTIPLE_CHOICE);
        question.setOptions(processed.getOptions());
        question.setCorrectAnswer(processed.getCorrectAnswer());
        question.setClassification(processed.getClassification());
        question.setDifficulty(processed.getDifficulty());
        return question;
    }
}

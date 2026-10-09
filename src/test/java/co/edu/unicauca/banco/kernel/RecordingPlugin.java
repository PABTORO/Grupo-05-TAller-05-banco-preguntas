package co.edu.unicauca.banco.kernel;

import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.domain.QuestionRequest;

import java.util.UUID;

/** Plugin falso: cuenta cuántas veces se llamó a init() y destroy(). */
public class RecordingPlugin implements QuestionPlugin {

    public int initCalls;
    public int destroyCalls;

    @Override
    public String getName() {
        return "recording";
    }

    @Override
    public boolean supports(String type) {
        return "RECORDING".equalsIgnoreCase(type);
    }

    @Override
    public Question generate(QuestionRequest request) {
        return new Question(UUID.randomUUID().toString(), request.getTitle(), request.getContent(), "RECORDING");
    }

    @Override
    public void init() {
        initCalls++;
    }

    @Override
    public void destroy() {
        destroyCalls++;
    }
}

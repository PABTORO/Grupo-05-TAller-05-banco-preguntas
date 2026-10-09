package co.edu.unicauca.banco.domain;

/** Se lanza cuando una solicitud no cumple las reglas de un filtro o de un plugin. */
public class InvalidQuestionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String source;

    public InvalidQuestionException(String source, String message) {
        super("[" + source + "] " + message);
        this.source = source;
    }

    /** Nombre del filtro o plugin que rechazó la solicitud. */
    public String getSource() { return source; }
}

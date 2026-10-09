package co.edu.unicauca.banco.kernel;

import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.domain.QuestionRequest;

/**
 * Contrato común de todos los plugins. Los tres primeros métodos son el
 * contrato pedido en el taller; {@link #init()} y {@link #destroy()} son
 * ganchos opcionales del ciclo de vida que el núcleo invoca al activar y
 * desactivar el plugin.
 */
public interface QuestionPlugin {

    String getName();

    boolean supports(String type);

    Question generate(QuestionRequest request);

    /** Se invoca cuando el núcleo activa el plugin. */
    default void init() { }

    /** Se invoca cuando el núcleo desactiva o descarga el plugin. */
    default void destroy() { }
}

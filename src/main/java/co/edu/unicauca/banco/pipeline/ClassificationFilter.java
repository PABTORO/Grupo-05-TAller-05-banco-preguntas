package co.edu.unicauca.banco.pipeline;

import co.edu.unicauca.banco.domain.InvalidQuestionException;
import co.edu.unicauca.banco.domain.QuestionRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Valida y completa la clasificación de la pregunta.
 * <ul>
 *   <li>Competencia: si viene vacía se deduce por palabras clave del título y
 *       el enunciado; si viene, debe pertenecer al catálogo.</li>
 *   <li>Dificultad: si viene vacía se asigna según la longitud del enunciado;
 *       si viene, debe ser BAJA, MEDIA o ALTA.</li>
 * </ul>
 * Este es el único filtro que <b>transforma</b> la solicitud: devuelve una
 * copia con los dos campos ya normalizados.
 */
public class ClassificationFilter implements Filter {

    public static final List<String> DIFFICULTIES = List.of("BAJA", "MEDIA", "ALTA");

    /** Catálogo de competencias con las palabras clave usadas para deducirlas. */
    private static final Map<String, List<String>> CATALOG = new LinkedHashMap<>();

    static {
        CATALOG.put("Arquitectura de software",
                List.of("arquitectura", "microkernel", "patron", "capas", "tuberias", "filtros",
                        "componente", "microservicio"));
        CATALOG.put("Ingeniería de requisitos",
                List.of("requisito", "historia de usuario", "stakeholder", "caso de uso"));
        CATALOG.put("Diseño de software",
                List.of("diseno", "uml", "cohesion", "acoplamiento", "solid", "diagrama"));
        CATALOG.put("Pruebas de software",
                List.of("prueba", "test", "junit", "cobertura", "defecto"));
        CATALOG.put("Bases de datos",
                List.of("base de datos", "sql", "tabla", "consulta", "normalizacion", "indice"));
        CATALOG.put("Gestión de proyectos",
                List.of("proyecto", "cronograma", "riesgo", "scrum", "sprint", "estimacion"));
        CATALOG.put("Programación",
                List.of("clase", "metodo", "variable", "algoritmo", "herencia", "polimorfismo", "java"));
    }

    public static List<String> classifications() {
        return List.copyOf(CATALOG.keySet());
    }

    @Override
    public QuestionRequest process(QuestionRequest in) {
        QuestionRequest out = in.copy();
        out.setClassification(resolveClassification(in));
        out.setDifficulty(resolveDifficulty(in));
        return out;
    }

    private String resolveClassification(QuestionRequest in) {
        String given = in.getClassification();
        if (TextUtils.isBlank(given)) {
            return infer(in.getTitle() + " " + in.getContent());
        }
        String wanted = TextUtils.normalize(given);
        for (String canonical : CATALOG.keySet()) {
            if (TextUtils.normalize(canonical).equals(wanted)) {
                return canonical;
            }
        }
        throw new InvalidQuestionException(getName(),
                "La clasificación '" + given.trim() + "' no es válida. Opciones: " + classifications());
    }

    private String infer(String text) {
        String haystack = TextUtils.normalize(text);
        String best = null;
        int bestHits = 0;
        for (Map.Entry<String, List<String>> e : CATALOG.entrySet()) {
            int hits = 0;
            for (String keyword : e.getValue()) {
                if (haystack.contains(keyword)) {
                    hits++;
                }
            }
            if (hits > bestHits) {
                best = e.getKey();
                bestHits = hits;
            }
        }
        if (best == null) {
            throw new InvalidQuestionException(getName(),
                    "No se pudo deducir la clasificación; indíquela manualmente. Opciones: " + classifications());
        }
        return best;
    }

    private String resolveDifficulty(QuestionRequest in) {
        String given = in.getDifficulty();
        if (TextUtils.isBlank(given)) {
            int length = in.getContent() == null ? 0 : in.getContent().trim().length();
            if (length < 100) {
                return "BAJA";
            }
            return length < 250 ? "MEDIA" : "ALTA";
        }
        String upper = given.trim().toUpperCase();
        if (!DIFFICULTIES.contains(upper)) {
            throw new InvalidQuestionException(getName(),
                    "La dificultad '" + given.trim() + "' no es válida. Opciones: " + DIFFICULTIES);
        }
        return upper;
    }
}

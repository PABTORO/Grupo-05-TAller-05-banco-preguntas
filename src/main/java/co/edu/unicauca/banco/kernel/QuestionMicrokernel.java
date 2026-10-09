package co.edu.unicauca.banco.kernel;

import co.edu.unicauca.banco.domain.Question;
import co.edu.unicauca.banco.domain.QuestionRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Núcleo del sistema. Solo sabe cuatro cosas: guardar el banco de preguntas,
 * registrar plugins, ejecutarlos y gestionar su ciclo de vida. No conoce ningún
 * tipo de pregunta concreto.
 */
public class QuestionMicrokernel {

    public static final String DEFAULT_PLUGINS_FILE = "/plugins.properties";

    private final Map<String, Question> questions = new LinkedHashMap<>();
    private final QuestionPluginManager pluginManager;

    public QuestionMicrokernel() {
        this(new QuestionPluginManager());
    }

    public QuestionMicrokernel(QuestionPluginManager pluginManager) {
        this.pluginManager = pluginManager;
    }

    // ---------------------------------------------------------------- arranque

    /** Carga los plugins declarados en /plugins.properties del classpath. */
    public List<String> start() {
        try (InputStream in = QuestionMicrokernel.class.getResourceAsStream(DEFAULT_PLUGINS_FILE)) {
            if (in == null) {
                throw new PluginLoadException("No se encontró " + DEFAULT_PLUGINS_FILE + " en el classpath");
            }
            return pluginManager.loadFromProperties(in);
        } catch (IOException e) {
            throw new PluginLoadException("No se pudo leer " + DEFAULT_PLUGINS_FILE, e);
        }
    }

    /** Carga plugins desde un flujo .properties arbitrario. */
    public List<String> start(InputStream propertiesStream) throws IOException {
        return pluginManager.loadFromProperties(propertiesStream);
    }

    public void shutdown() {
        pluginManager.shutdown();
    }

    // -------------------------------------------------------------- ejecución

    /**
     * Busca el plugin activo que soporta el tipo de la solicitud, le pide la
     * pregunta y la guarda en el banco.
     */
    public Question generateQuestion(QuestionRequest request) {
        QuestionPlugin plugin = pluginManager.findActiveFor(request.getType())
                .orElseThrow(() -> new PluginLoadException(
                        "No hay un plugin activo para el tipo '" + request.getType() + "'"));
        Question question = plugin.generate(request);
        if (question == null) {
            throw new IllegalStateException("El plugin '" + plugin.getName() + "' no devolvió ninguna pregunta");
        }
        questions.put(question.getId(), question);
        return question;
    }

    // ------------------------------------------------------- banco de preguntas

    public Optional<Question> getQuestion(String id) {
        return Optional.ofNullable(questions.get(id));
    }

    public List<Question> getAllQuestions() {
        return Collections.unmodifiableList(new ArrayList<>(questions.values()));
    }

    public boolean removeQuestion(String id) {
        return questions.remove(id) != null;
    }

    public int questionCount() {
        return questions.size();
    }

    // ------------------------------------------------- ciclo de vida de plugins

    public void registerPlugin(QuestionPlugin plugin) { pluginManager.register(plugin); }

    public void enablePlugin(String name) { pluginManager.enable(name); }

    public void disablePlugin(String name) { pluginManager.disable(name); }

    public void unloadPlugin(String name) { pluginManager.unload(name); }

    public List<PluginStatus> getPlugins() { return pluginManager.list(); }
}

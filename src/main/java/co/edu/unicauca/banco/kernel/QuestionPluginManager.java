package co.edu.unicauca.banco.kernel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registro y ciclo de vida de los plugins. Las clases se leen de
 * plugins.properties (nombre=clase completa) y se instancian por reflexión,
 * de modo que agregar un plugin nuevo no obliga a tocar el núcleo.
 */
public class QuestionPluginManager {

    private static final Logger LOG = Logger.getLogger(QuestionPluginManager.class.getName());

    private static final class Entry {
        private final QuestionPlugin plugin;
        private PluginState state;

        private Entry(QuestionPlugin plugin, PluginState state) {
            this.plugin = plugin;
            this.state = state;
        }
    }

    private final Map<String, Entry> plugins = new LinkedHashMap<>();

    // ------------------------------------------------------------------ carga

    /**
     * Lee un archivo .properties (clave = identificador, valor = clase) y
     * registra cada plugin. Las entradas inválidas se registran en el log y se
     * omiten para que un plugin defectuoso no tumbe el resto.
     *
     * @return nombres de los plugins que sí quedaron cargados
     */
    public List<String> loadFromProperties(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);

        List<String> loaded = new ArrayList<>();
        for (String key : new TreeSet<>(props.stringPropertyNames())) {
            String className = props.getProperty(key).trim();
            try {
                QuestionPlugin plugin = instantiate(className);
                register(plugin);
                loaded.add(plugin.getName());
            } catch (PluginLoadException e) {
                String cause = e.getCause() == null ? "" : " (" + e.getCause() + ")";
                LOG.log(Level.WARNING, "No se cargó el plugin ''{0}'': {1}{2}",
                        new Object[] {key, e.getMessage(), cause});
            }
        }
        return loaded;
    }

    /** Crea un plugin a partir del nombre completo de su clase usando reflexión. */
    QuestionPlugin instantiate(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (!QuestionPlugin.class.isAssignableFrom(clazz)) {
                throw new PluginLoadException(className + " no implementa QuestionPlugin");
            }
            return (QuestionPlugin) clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new PluginLoadException("No se pudo instanciar " + className, e);
        }
    }

    // --------------------------------------------------------- ciclo de vida

    /** Registra un plugin ya creado, lo inicializa y lo deja activo. */
    public void register(QuestionPlugin plugin) {
        String name = plugin.getName();
        if (plugins.containsKey(name)) {
            throw new PluginLoadException("Ya existe un plugin llamado '" + name + "'");
        }
        plugin.init();
        plugins.put(name, new Entry(plugin, PluginState.ACTIVE));
    }

    public void enable(String name) {
        Entry entry = require(name);
        if (entry.state == PluginState.DISABLED) {
            entry.plugin.init();
            entry.state = PluginState.ACTIVE;
        }
    }

    public void disable(String name) {
        Entry entry = require(name);
        if (entry.state == PluginState.ACTIVE) {
            entry.plugin.destroy();
            entry.state = PluginState.DISABLED;
        }
    }

    /** Elimina el plugin del registro (lo desactiva primero si estaba activo). */
    public void unload(String name) {
        disable(name);
        plugins.remove(name);
    }

    /** Desactiva todos los plugins activos; se usa al cerrar el núcleo. */
    public void shutdown() {
        for (String name : new ArrayList<>(plugins.keySet())) {
            disable(name);
        }
    }

    // -------------------------------------------------------------- consultas

    /** Primer plugin activo que soporta el tipo pedido. */
    public Optional<QuestionPlugin> findActiveFor(String type) {
        return plugins.values().stream()
                .filter(e -> e.state == PluginState.ACTIVE)
                .map(e -> e.plugin)
                .filter(p -> p.supports(type))
                .findFirst();
    }

    public Optional<PluginState> getState(String name) {
        return Optional.ofNullable(plugins.get(name)).map(e -> e.state);
    }

    public List<PluginStatus> list() {
        List<PluginStatus> result = new ArrayList<>();
        for (Map.Entry<String, Entry> e : plugins.entrySet()) {
            result.add(new PluginStatus(e.getKey(),
                    e.getValue().plugin.getClass().getName(), e.getValue().state));
        }
        return result;
    }

    public int size() { return plugins.size(); }

    private Entry require(String name) {
        Entry entry = plugins.get(name);
        if (entry == null) {
            throw new PluginLoadException("No existe el plugin '" + name + "'");
        }
        return entry;
    }
}

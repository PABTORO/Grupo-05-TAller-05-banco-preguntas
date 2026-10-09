package co.edu.unicauca.banco.kernel;

/** Error al registrar o instanciar un plugin. */
public class PluginLoadException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PluginLoadException(String message) { super(message); }

    public PluginLoadException(String message, Throwable cause) { super(message, cause); }
}

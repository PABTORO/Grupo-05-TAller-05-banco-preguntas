package co.edu.unicauca.banco.kernel;

/** Vista de solo lectura de un plugin registrado. */
public record PluginStatus(String name, String className, PluginState state) { }

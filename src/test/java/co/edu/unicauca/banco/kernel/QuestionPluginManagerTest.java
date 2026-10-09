package co.edu.unicauca.banco.kernel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionPluginManagerTest {

    private QuestionPluginManager manager;

    @BeforeEach
    void setUp() {
        manager = new QuestionPluginManager();
    }

    private static InputStream props(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void loadsPluginsByReflectionFromProperties() throws IOException {
        List<String> loaded = manager.loadFromProperties(props(
                "a=co.edu.unicauca.banco.plugins.MultipleChoiceQuestionPlugin\n"
                        + "b=co.edu.unicauca.banco.plugins.CaseBasedQuestionPlugin\n"
                        + "c=co.edu.unicauca.banco.plugins.MultimediaQuestionPlugin\n"));

        assertEquals(3, loaded.size());
        assertTrue(loaded.contains("multiple-choice"));
        assertTrue(loaded.contains("case-based"));
        assertTrue(loaded.contains("multimedia"));
        assertEquals(3, manager.size());
    }

    @Test
    void skipsClassesThatDoNotExist() throws IOException {
        List<String> loaded = manager.loadFromProperties(props(
                "bad=co.edu.unicauca.banco.plugins.NoExiste\n"
                        + "ok=co.edu.unicauca.banco.plugins.CaseBasedQuestionPlugin\n"));

        assertEquals(List.of("case-based"), loaded);
    }

    @Test
    void skipsClassesThatAreNotPlugins() throws IOException {
        List<String> loaded = manager.loadFromProperties(props("bad=java.lang.String\n"));

        assertTrue(loaded.isEmpty());
        assertEquals(0, manager.size());
    }

    @Test
    void instantiateRejectsNonPluginClass() {
        assertThrows(PluginLoadException.class, () -> manager.instantiate("java.lang.String"));
        assertThrows(PluginLoadException.class, () -> manager.instantiate("no.existe.Clase"));
    }

    @Test
    void registerInitializesAndActivatesPlugin() {
        RecordingPlugin plugin = new RecordingPlugin();
        manager.register(plugin);

        assertEquals(1, plugin.initCalls);
        assertEquals(PluginState.ACTIVE, manager.getState("recording").orElseThrow());
    }

    @Test
    void rejectsDuplicatedPluginName() {
        manager.register(new RecordingPlugin());
        assertThrows(PluginLoadException.class, () -> manager.register(new RecordingPlugin()));
    }

    @Test
    void disableCallsDestroyAndHidesPluginFromLookup() {
        RecordingPlugin plugin = new RecordingPlugin();
        manager.register(plugin);

        manager.disable("recording");

        assertEquals(1, plugin.destroyCalls);
        assertEquals(PluginState.DISABLED, manager.getState("recording").orElseThrow());
        assertFalse(manager.findActiveFor("RECORDING").isPresent());
    }

    @Test
    void enableAfterDisableInitializesAgain() {
        RecordingPlugin plugin = new RecordingPlugin();
        manager.register(plugin);
        manager.disable("recording");

        manager.enable("recording");

        assertEquals(2, plugin.initCalls);
        assertTrue(manager.findActiveFor("recording").isPresent());
    }

    @Test
    void disableAndEnableAreIdempotent() {
        RecordingPlugin plugin = new RecordingPlugin();
        manager.register(plugin);

        manager.enable("recording");   // ya activo: no hace nada
        manager.disable("recording");
        manager.disable("recording");  // ya desactivado: no hace nada

        assertEquals(1, plugin.initCalls);
        assertEquals(1, plugin.destroyCalls);
    }

    @Test
    void unloadRemovesPluginAndCallsDestroy() {
        RecordingPlugin plugin = new RecordingPlugin();
        manager.register(plugin);

        manager.unload("recording");

        assertEquals(1, plugin.destroyCalls);
        assertEquals(0, manager.size());
    }

    @Test
    void operationsOnUnknownPluginFail() {
        assertThrows(PluginLoadException.class, () -> manager.enable("nada"));
        assertThrows(PluginLoadException.class, () -> manager.disable("nada"));
        assertThrows(PluginLoadException.class, () -> manager.unload("nada"));
    }

    @Test
    void shutdownDisablesEveryActivePlugin() {
        RecordingPlugin plugin = new RecordingPlugin();
        manager.register(plugin);

        manager.shutdown();

        assertEquals(1, plugin.destroyCalls);
        assertEquals(PluginState.DISABLED, manager.getState("recording").orElseThrow());
    }

    @Test
    void listReportsNameClassAndState() {
        manager.register(new RecordingPlugin());

        PluginStatus status = manager.list().get(0);

        assertEquals("recording", status.name());
        assertEquals(RecordingPlugin.class.getName(), status.className());
        assertEquals(PluginState.ACTIVE, status.state());
    }
}

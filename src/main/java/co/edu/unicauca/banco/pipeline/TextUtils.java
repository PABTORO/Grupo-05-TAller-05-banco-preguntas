package co.edu.unicauca.banco.pipeline;

import java.text.Normalizer;
import java.util.Locale;

/** Utilidades de texto compartidas por los filtros. */
final class TextUtils {

    private TextUtils() { }

    static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /** Minúsculas, sin tildes y sin espacios sobrantes; sirve para comparar textos. */
    static String normalize(String s) {
        if (s == null) {
            return "";
        }
        String decomposed = Normalizer.normalize(s.trim(), Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}

package com.munaqfit.backend.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Limpieza centralizada de texto libre que llega del frontend (tarea 2.3).
 *
 * <p>El objetivo es cerrar el XSS almacenado: aunque Angular escapa las
 * interpolaciones, un valor guardado en BD se reutiliza en CSV, correos o
 * reportes. Se eliminan etiquetas HTML/JS, caracteres de control y espacios
 * sobrantes antes de persistir.</p>
 */
public final class Sanitizacion {

    /** Bloques &lt;script&gt; y &lt;style&gt; completos (incluido su contenido). */
    private static final Pattern BLOQUES_PELIGROSOS =
            Pattern.compile("(?is)<\\s*(script|style)[^>]*>.*?<\\s*/\\s*\\1\\s*>");

    /** Cualquier etiqueta HTML suelta. */
    private static final Pattern ETIQUETAS_HTML = Pattern.compile("<[^>]*>");

    /** Caracteres de control salvo tabulador, salto de linea y retorno. */
    private static final Pattern CONTROLES = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");

    private static final Pattern ESPACIOS = Pattern.compile("[ \\t]{2,}");

    private Sanitizacion() {
    }

    /** Texto plano: sin HTML, sin controles y sin espacios repetidos. */
    public static String texto(String valor) {
        if (valor == null) {
            return null;
        }
        String limpio = BLOQUES_PELIGROSOS.matcher(valor).replaceAll(" ");
        limpio = ETIQUETAS_HTML.matcher(limpio).replaceAll("");
        limpio = CONTROLES.matcher(limpio).replaceAll("");
        limpio = ESPACIOS.matcher(limpio).replaceAll(" ");
        return limpio.trim();
    }

    /** Correo normalizado: texto plano y minusculas. */
    public static String email(String valor) {
        String limpio = texto(valor);
        return limpio == null ? null : limpio.toLowerCase(Locale.ROOT);
    }

    /** DNI peruano: solo digitos (descarta espacios, guiones o etiquetas). */
    public static String dni(String valor) {
        String limpio = texto(valor);
        return limpio == null ? null : limpio.replaceAll("\\D", "");
    }

    /** RUC: solo digitos. */
    public static String ruc(String valor) {
        return dni(valor);
    }

    /** Telefono: digitos, espacios, guiones y prefijo +. */
    public static String telefono(String valor) {
        String limpio = texto(valor);
        return limpio == null ? null : limpio.replaceAll("[^0-9+\\-() ]", "").trim();
    }
}

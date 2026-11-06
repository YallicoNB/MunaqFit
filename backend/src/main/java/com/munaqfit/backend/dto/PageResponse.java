package com.munaqfit.backend.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Contrato de paginacion acordado en la etapa 3 (seccion 5):
 * {@code {contenido, pagina, tamano, totalElementos, totalPaginas}}.
 *
 * <p>Los endpoints de listados administrativos lo devuelven para que el
 * frontend pueda construir los controles de pagina (tarea 3.10) sin
 * depender de un formato distinto por pantalla.</p>
 */
public record PageResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas
) {

    /** Envuelve una pagina de Spring Data tal cual. */
    public static <T> PageResponse<T> desde(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    /** Envuelve una pagina transformando cada elemento a un DTO. */
    public static <E, T> PageResponse<T> desde(Page<E> page, Function<E, T> mapper) {
        return desde(page.map(mapper));
    }
}

package com.munaqfit.backend.util;

import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Parser comun para el parametro {@code ?orden=campo,asc|desc}.
 *
 * <p>Solo acepta campos de una lista blanca para evitar que un cliente
 * ordene por una propiedad inexistente (500) o inyecte rutas de entidad
 * arbitrarias. Si el campo no esta permitido, usa el orden por defecto.</p>
 */
public final class Ordenamiento {

    private Ordenamiento() {
    }

    public static Sort parsear(String orden,
                               Set<String> camposPermitidos,
                               String campoPorDefecto) {
        return parsear(orden, camposPermitidos, campoPorDefecto, Sort.Direction.DESC);
    }

    public static Sort parsear(String orden,
                               Set<String> camposPermitidos,
                               String campoPorDefecto,
                               Sort.Direction direccionPorDefecto) {
        String campo = campoPorDefecto;
        Sort.Direction direccion = direccionPorDefecto;

        if (orden != null && !orden.isBlank()) {
            String[] partes = orden.split(",", 2);
            String solicitado = partes[0].trim();
            if (camposPermitidos.contains(solicitado)) {
                campo = solicitado;
            }
            if (partes.length > 1) {
                String dir = partes[1].trim();
                if ("asc".equalsIgnoreCase(dir)) {
                    direccion = Sort.Direction.ASC;
                } else if ("desc".equalsIgnoreCase(dir)) {
                    direccion = Sort.Direction.DESC;
                }
            }
        }

        return Sort.by(direccion, campo);
    }
}

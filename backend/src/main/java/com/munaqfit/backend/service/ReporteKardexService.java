package com.munaqfit.backend.service;

import com.munaqfit.backend.dto.KardexFilaDTO;
import com.munaqfit.backend.dto.PageResponse;
import com.munaqfit.backend.model.MovimientoInventario.TipoMovimiento;
import com.munaqfit.backend.repository.MovimientoInventarioRepository;
import com.munaqfit.backend.util.Ordenamiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReporteKardexService {

    @Autowired
    private MovimientoInventarioRepository inventarioRepository;

    public List<KardexFilaDTO> obtenerReporteKardex(LocalDateTime desde, LocalDateTime hasta, Long productoId, TipoMovimiento tipo, Long usuarioId) {
        return inventarioRepository.consultarKardexFiltros(desde, hasta, productoId, tipo, usuarioId);
    }

    /** Version paginada del kardex para la tabla administrativa (tarea 2.5). */
    public PageResponse<KardexFilaDTO> obtenerReporteKardexPaginado(LocalDateTime desde,
                                                                     LocalDateTime hasta,
                                                                     Long productoId,
                                                                     TipoMovimiento tipo,
                                                                     Long usuarioId,
                                                                     int pagina,
                                                                     int tamano,
                                                                     String orden) {
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), normalizarTamano(tamano),
                Ordenamiento.parsear(orden,
                        java.util.Set.of("fechaMovimiento", "cantidad", "stockAnterior", "stockNuevo"),
                        "fechaMovimiento", org.springframework.data.domain.Sort.Direction.DESC));
        Page<KardexFilaDTO> page = inventarioRepository.consultarKardexFiltrosPaginado(
                desde, hasta, productoId, tipo, usuarioId, pageable);
        return PageResponse.desde(page);
    }

    private int normalizarTamano(int tamano) {
        return Math.min(Math.max(tamano, 1), 100);
    }
}
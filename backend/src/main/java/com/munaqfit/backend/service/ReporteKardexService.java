package com.munaqfit.backend.service;

import com.munaqfit.backend.dto.KardexFilaDTO;
import com.munaqfit.backend.model.MovimientoInventario.TipoMovimiento;
import com.munaqfit.backend.repository.MovimientoInventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
}
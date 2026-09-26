package com.munaqfit.backend.service;

import com.munaqfit.backend.model.ClienteFidelidad;
import com.munaqfit.backend.model.VisitaCliente;
import com.munaqfit.backend.repository.ClienteFidelidadRepository;
import com.munaqfit.backend.repository.VisitaClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FidelidadService {

    @Autowired
    private ClienteFidelidadRepository clienteFidelidadRepository;

    @Autowired
    private VisitaClienteRepository visitaClienteRepository;

    @Transactional
    public ClienteFidelidad registrarCliente(ClienteFidelidad cliente) {
        cliente.setVisitas(0);
        return clienteFidelidadRepository.save(cliente);
    }

    @Transactional
    public String registrarVisita(Long clienteId) {
        Optional<ClienteFidelidad> optCliente = clienteFidelidadRepository.findById(clienteId);
        
        if (optCliente.isPresent()) {
            ClienteFidelidad cliente = optCliente.get();
            
            int nuevasVisitas = cliente.getVisitas() + 1;
            cliente.setUltimaVisita(LocalDateTime.now());
            
            VisitaCliente historialVisita = new VisitaCliente();
            historialVisita.setClienteFidelidad(cliente);
            historialVisita.setFechaVisita(LocalDateTime.now());
            visitaClienteRepository.save(historialVisita);
            
            if (nuevasVisitas >= cliente.getUmbralPremio()) {
                cliente.setVisitas(0); // Se le da el premio y reiniciamos el contador
                clienteFidelidadRepository.save(cliente);
                return "¡Felicidades! Se alcanzó el umbral de " + cliente.getUmbralPremio() + " visitas. Aplica la bebida gratuita.";
            } else {
                cliente.setVisitas(nuevasVisitas);
                clienteFidelidadRepository.save(cliente);
                return "Visita registrada exitosamente. Total de visitas actuales: " + nuevasVisitas;
            }
        }
        
        throw new RuntimeException("Error: Cliente de fidelidad no encontrado en la base de datos.");
    }
}
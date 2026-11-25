package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.DatoEconomicoDTO;
import pe.edu.upc.agrocrew.dto.DatoEconomicoRequestDTO;
import pe.edu.upc.agrocrew.dto.RentabilidadDTO;

import java.util.List;

/**
 * Datos económicos referenciales (US32) y rentabilidad referencial por hectárea (US22).
 */
public interface DatoEconomicoService {

    List<DatoEconomicoDTO> listar(Long cultivoId, Long departamentoId);

    DatoEconomicoDTO registrar(DatoEconomicoRequestDTO dto);

    DatoEconomicoDTO actualizar(Long id, DatoEconomicoRequestDTO dto);

    /** Rentabilidad del cultivo en la región (departamento) del predio del productor autenticado. */
    RentabilidadDTO rentabilidad(Long predioId, Long cultivoId);
}

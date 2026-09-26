package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.CultivoRequestDTO;
import pe.edu.upc.agrocrew.dto.CultivoResponseDTO;
import pe.edu.upc.agrocrew.dto.GrupoCumDTO;

import java.util.List;

public interface CultivoService {

    List<CultivoResponseDTO> listarActivos();

    List<CultivoResponseDTO> listarTodos();

    CultivoResponseDTO obtener(Long id);

    CultivoResponseDTO registrar(CultivoRequestDTO dto);

    CultivoResponseDTO actualizar(Long id, CultivoRequestDTO dto);

    void cambiarEstado(Long id, boolean activo);

    List<GrupoCumDTO> listarGruposCum();
}

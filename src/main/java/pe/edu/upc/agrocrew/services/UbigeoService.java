package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.UbigeoDTO;

import java.util.List;

public interface UbigeoService {

    List<UbigeoDTO> listarDepartamentos();

    List<UbigeoDTO> listarProvincias(Long departamentoId);

    List<UbigeoDTO> listarDistritos(Long provinciaId);
}

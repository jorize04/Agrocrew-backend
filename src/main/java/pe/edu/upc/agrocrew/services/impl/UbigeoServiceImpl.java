package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.UbigeoDTO;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.repositories.DepartamentoRepository;
import pe.edu.upc.agrocrew.repositories.DistritoRepository;
import pe.edu.upc.agrocrew.repositories.ProvinciaRepository;
import pe.edu.upc.agrocrew.services.UbigeoService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UbigeoServiceImpl implements UbigeoService {

    private final DepartamentoRepository departamentoRepository;
    private final ProvinciaRepository provinciaRepository;
    private final DistritoRepository distritoRepository;

    @Override
    public List<UbigeoDTO> listarDepartamentos() {
        return departamentoRepository.findAllByOrderByNombreAsc().stream()
                .map(d -> new UbigeoDTO(d.getId(), d.getUbigeo(), d.getNombre()))
                .toList();
    }

    @Override
    public List<UbigeoDTO> listarProvincias(Long departamentoId) {
        if (!departamentoRepository.existsById(departamentoId)) {
            throw new RecursoNoEncontradoException("Departamento no encontrado");
        }
        return provinciaRepository.findByDepartamentoIdOrderByNombreAsc(departamentoId).stream()
                .map(p -> new UbigeoDTO(p.getId(), p.getUbigeo(), p.getNombre()))
                .toList();
    }

    @Override
    public List<UbigeoDTO> listarDistritos(Long provinciaId) {
        if (!provinciaRepository.existsById(provinciaId)) {
            throw new RecursoNoEncontradoException("Provincia no encontrada");
        }
        return distritoRepository.findByProvinciaIdOrderByNombreAsc(provinciaId).stream()
                .map(d -> new UbigeoDTO(d.getId(), d.getUbigeo(), d.getNombre()))
                .toList();
    }
}

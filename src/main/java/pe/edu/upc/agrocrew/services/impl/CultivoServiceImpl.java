package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.CultivoRequestDTO;
import pe.edu.upc.agrocrew.dto.CultivoResponseDTO;
import pe.edu.upc.agrocrew.dto.GrupoCumDTO;
import pe.edu.upc.agrocrew.dto.RequerimientoCultivoDTO;
import pe.edu.upc.agrocrew.exceptions.ConflictoException;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.Cultivo;
import pe.edu.upc.agrocrew.models.GrupoCum;
import pe.edu.upc.agrocrew.models.RequerimientoCultivo;
import pe.edu.upc.agrocrew.repositories.CultivoRepository;
import pe.edu.upc.agrocrew.repositories.GrupoCumRepository;
import pe.edu.upc.agrocrew.services.CultivoService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CultivoServiceImpl implements CultivoService {

    private final CultivoRepository cultivoRepository;
    private final GrupoCumRepository grupoCumRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CultivoResponseDTO> listarActivos() {
        return cultivoRepository.findByActivoTrueOrderByNombreAsc().stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CultivoResponseDTO> listarTodos() {
        return cultivoRepository.findAllByOrderByNombreAsc().stream().map(this::convertirADTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CultivoResponseDTO obtener(Long id) {
        return convertirADTO(buscar(id));
    }

    @Override
    @Transactional
    public CultivoResponseDTO registrar(CultivoRequestDTO dto) {
        String nombre = dto.getNombre().trim();
        if (cultivoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe un cultivo con el nombre " + nombre);
        }
        Cultivo cultivo = new Cultivo();
        cultivo.asignarRequerimiento(new RequerimientoCultivo());
        copiarDatos(dto, cultivo);
        Cultivo guardado = cultivoRepository.save(cultivo);
        log.info("Cultivo registrado id={} nombre={}", guardado.getId(), guardado.getNombre());
        return convertirADTO(guardado);
    }

    @Override
    @Transactional
    public CultivoResponseDTO actualizar(Long id, CultivoRequestDTO dto) {
        Cultivo cultivo = buscar(id);
        String nombre = dto.getNombre().trim();
        if (cultivoRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ConflictoException("Ya existe un cultivo con el nombre " + nombre);
        }
        if (cultivo.getRequerimiento() == null) {
            cultivo.asignarRequerimiento(new RequerimientoCultivo());
        }
        copiarDatos(dto, cultivo);
        log.info("Cultivo actualizado id={}", id);
        return convertirADTO(cultivoRepository.save(cultivo));
    }

    @Override
    @Transactional
    public void cambiarEstado(Long id, boolean activo) {
        Cultivo cultivo = buscar(id);
        cultivo.setActivo(activo);
        cultivoRepository.save(cultivo);
        log.info("Cultivo id={} {}", id, activo ? "activado" : "desactivado");
    }

    @Override
    @Transactional(readOnly = true)
    public List<GrupoCumDTO> listarGruposCum() {
        return grupoCumRepository.findAllByOrderByIdAsc().stream()
                .map(g -> new GrupoCumDTO(g.getCodigo(), g.getNombre(), g.getDescripcionSimple(),
                        g.getUsosRecomendados(), g.getUsosNoRecomendados()))
                .toList();
    }

    // ---------------------------------------------------------------- auxiliares

    private Cultivo buscar(Long id) {
        return cultivoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cultivo no encontrado"));
    }

    private void copiarDatos(CultivoRequestDTO dto, Cultivo cultivo) {
        validarRangos(dto.getRequerimiento());

        cultivo.setNombre(dto.getNombre().trim());
        cultivo.setNombreCientifico(dto.getNombreCientifico());
        cultivo.setTipo(dto.getTipo());
        cultivo.setCicloDias(dto.getCicloDias());
        cultivo.setDescripcion(dto.getDescripcion());
        cultivo.setGruposCum(buscarGrupos(dto.getGruposCum()));

        RequerimientoCultivoDTO r = dto.getRequerimiento();
        RequerimientoCultivo req = cultivo.getRequerimiento();
        req.setAltitudMin(r.getAltitudMin());
        req.setAltitudMax(r.getAltitudMax());
        req.setTemperaturaMin(r.getTemperaturaMin());
        req.setTemperaturaMax(r.getTemperaturaMax());
        req.setPrecipitacionMinMm(r.getPrecipitacionMinMm());
        req.setPrecipitacionMaxMm(r.getPrecipitacionMaxMm());
        req.setToleranciaInundacion(r.getToleranciaInundacion());
        req.setRequiereRiego(r.getRequiereRiego());
    }

    private void validarRangos(RequerimientoCultivoDTO r) {
        if (r.getAltitudMin() > r.getAltitudMax()) {
            throw new ReglaNegocioException("La altitud mínima no puede ser mayor que la máxima");
        }
        if (r.getTemperaturaMin() > r.getTemperaturaMax()) {
            throw new ReglaNegocioException("La temperatura mínima no puede ser mayor que la máxima");
        }
        if (r.getPrecipitacionMinMm() > r.getPrecipitacionMaxMm()) {
            throw new ReglaNegocioException("La precipitación mínima no puede ser mayor que la máxima");
        }
    }

    private Set<GrupoCum> buscarGrupos(List<String> codigos) {
        Set<String> solicitados = codigos.stream()
                .map(c -> c.trim().toUpperCase())
                .collect(Collectors.toSet());
        List<GrupoCum> encontrados = grupoCumRepository.findByCodigoIn(solicitados);
        if (encontrados.size() != solicitados.size()) {
            throw new ReglaNegocioException("Hay códigos de grupo CUM inválidos. Use A, C, P, F o X");
        }
        return new HashSet<>(encontrados);
    }

    private CultivoResponseDTO convertirADTO(Cultivo c) {
        CultivoResponseDTO dto = new CultivoResponseDTO();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setNombreCientifico(c.getNombreCientifico());
        dto.setTipo(c.getTipo());
        dto.setCicloDias(c.getCicloDias());
        dto.setDescripcion(c.getDescripcion());
        dto.setActivo(c.getActivo());
        dto.setGruposCum(c.getGruposCum().stream().map(GrupoCum::getCodigo).sorted().toList());

        RequerimientoCultivo req = c.getRequerimiento();
        if (req != null) {
            RequerimientoCultivoDTO r = new RequerimientoCultivoDTO();
            r.setAltitudMin(req.getAltitudMin());
            r.setAltitudMax(req.getAltitudMax());
            r.setTemperaturaMin(req.getTemperaturaMin());
            r.setTemperaturaMax(req.getTemperaturaMax());
            r.setPrecipitacionMinMm(req.getPrecipitacionMinMm());
            r.setPrecipitacionMaxMm(req.getPrecipitacionMaxMm());
            r.setToleranciaInundacion(req.getToleranciaInundacion());
            r.setRequiereRiego(req.getRequiereRiego());
            dto.setRequerimiento(r);
        }
        return dto;
    }
}

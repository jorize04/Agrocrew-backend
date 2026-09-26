package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.PredioRequestDTO;
import pe.edu.upc.agrocrew.dto.PredioResponseDTO;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.Distrito;
import pe.edu.upc.agrocrew.models.Predio;
import pe.edu.upc.agrocrew.models.Usuario;
import pe.edu.upc.agrocrew.repositories.DistritoRepository;
import pe.edu.upc.agrocrew.repositories.PredioRepository;
import pe.edu.upc.agrocrew.services.AlertaService;
import pe.edu.upc.agrocrew.services.PredioService;
import pe.edu.upc.agrocrew.services.UsuarioService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredioServiceImpl implements PredioService {

    private final PredioRepository predioRepository;
    private final DistritoRepository distritoRepository;
    private final UsuarioService usuarioService;
    private final AlertaService alertaService;

    @Override
    @Transactional
    public PredioResponseDTO registrar(PredioRequestDTO dto) {
        Usuario usuario = usuarioService.obtenerUsuarioActual();
        Predio predio = new Predio();
        predio.setUsuario(usuario);
        copiarDatos(dto, predio);
        Predio guardado = predioRepository.save(predio);
        log.info("Predio registrado id={} usuario={}", guardado.getId(), usuario.getId());
        // Si el predio está cerca de un punto crítico de riesgo alto, se alerta de inmediato (US15).
        alertaService.generarAlertasRiesgo(guardado);
        return convertirADTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PredioResponseDTO> listarMisPredios() {
        Usuario usuario = usuarioService.obtenerUsuarioActual();
        return predioRepository.findByUsuarioIdAndActivoTrueOrderByFechaRegistroDesc(usuario.getId())
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PredioResponseDTO obtener(Long id) {
        return convertirADTO(obtenerPredioDelUsuarioActual(id));
    }

    @Override
    @Transactional
    public PredioResponseDTO actualizar(Long id, PredioRequestDTO dto) {
        Predio predio = obtenerPredioDelUsuarioActual(id);
        copiarDatos(dto, predio);
        Predio guardado = predioRepository.save(predio);
        log.info("Predio actualizado id={}", id);
        return convertirADTO(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Predio predio = obtenerPredioDelUsuarioActual(id);
        predio.setActivo(false);
        predioRepository.save(predio);
        log.info("Predio eliminado (lógico) id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Predio obtenerPredioDelUsuarioActual(Long id) {
        Usuario usuario = usuarioService.obtenerUsuarioActual();
        // Si el predio es de otro usuario respondemos 404 (no 403) para no revelar que existe.
        return predioRepository.findByIdAndUsuarioIdAndActivoTrue(id, usuario.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Predio no encontrado"));
    }

    private void copiarDatos(PredioRequestDTO dto, Predio predio) {
        Distrito distrito = distritoRepository.findById(dto.getDistritoId())
                .orElseThrow(() -> new ReglaNegocioException("El distrito seleccionado no es válido"));

        predio.setNombre(dto.getNombre().trim());
        predio.setDistrito(distrito);
        predio.setLatitud(dto.getLatitud());
        predio.setLongitud(dto.getLongitud());
        predio.setAreaHa(dto.getAreaHa());
        predio.setFuenteAgua(dto.getFuenteAgua());
        predio.setCodigoCatastral(dto.getCodigoCatastral());
        // Si el productor no conoce la altitud, usamos la de la capital de su distrito.
        predio.setAltitudMsnm(dto.getAltitudMsnm() != null ? dto.getAltitudMsnm() : distrito.getAltitudMsnm());
    }

    private PredioResponseDTO convertirADTO(Predio p) {
        PredioResponseDTO dto = new PredioResponseDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setCodigoCatastral(p.getCodigoCatastral());
        dto.setLatitud(p.getLatitud());
        dto.setLongitud(p.getLongitud());
        dto.setAltitudMsnm(p.getAltitudMsnm());
        dto.setAreaHa(p.getAreaHa());
        dto.setFuenteAgua(p.getFuenteAgua());
        dto.setDistritoId(p.getDistrito().getId());
        dto.setUbigeo(p.getDistrito().getUbigeo());
        dto.setDistrito(p.getDistrito().getNombre());
        dto.setProvincia(p.getDistrito().getProvincia().getNombre());
        dto.setDepartamento(p.getDistrito().getProvincia().getDepartamento().getNombre());
        dto.setFechaRegistro(p.getFechaRegistro());
        dto.setFechaActualizacion(p.getFechaActualizacion());
        return dto;
    }
}

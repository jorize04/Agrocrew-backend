package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.models.RegistroIntegracion;
import pe.edu.upc.agrocrew.models.ServicioExterno;
import pe.edu.upc.agrocrew.repositories.RegistroIntegracionRepository;
import pe.edu.upc.agrocrew.services.RegistroIntegracionService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistroIntegracionServiceImpl implements RegistroIntegracionService {

    private final RegistroIntegracionRepository repository;

    /** REQUIRES_NEW: el registro se guarda aunque la operación principal falle y se deshaga. */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(ServicioExterno servicio, String endpoint, Integer estadoHttp,
                          long duracionMs, boolean exito, String mensajeError) {
        if (exito) {
            log.info("[{}] OK en {} ms", servicio, duracionMs);
        } else {
            log.error("[{}] Falló en {} ms: {}", servicio, duracionMs, mensajeError);
        }
        try {
            RegistroIntegracion r = new RegistroIntegracion();
            r.setServicio(servicio);
            r.setEndpoint(endpoint != null && endpoint.length() > 500 ? endpoint.substring(0, 500) : endpoint);
            r.setEstadoHttp(estadoHttp);
            r.setDuracionMs(duracionMs);
            r.setExito(exito);
            r.setMensajeError(mensajeError);
            repository.save(r);
        } catch (Exception e) {
            log.warn("No se pudo guardar el registro de integración: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegistroIntegracion> listarRecientes() {
        return repository.findTop50ByOrderByFechaDesc();
    }
}

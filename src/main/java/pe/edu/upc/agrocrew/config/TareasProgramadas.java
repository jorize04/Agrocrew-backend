package pe.edu.upc.agrocrew.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upc.agrocrew.services.AlertaService;
import pe.edu.upc.agrocrew.services.PuntoCriticoService;

/** Tareas automáticas. Los horarios se configuran en application.properties (formato cron). */
@Slf4j
@Component
@RequiredArgsConstructor
public class TareasProgramadas {

    private final PuntoCriticoService puntoCriticoService;
    private final AlertaService alertaService;

    /** Por defecto todos los días a las 3:00. */
    @Scheduled(cron = "${app.integraciones.ana.cron-sincronizacion}", zone = "America/Lima")
    public void sincronizarAna() {
        try {
            log.info("Sincronización programada con la ANA: {}", puntoCriticoService.sincronizarConAna());
        } catch (Exception e) {
            log.error("Falló la sincronización programada con la ANA: {}", e.getMessage());
        }
    }

    /** Por defecto todos los días a las 6:00. */
    @Scheduled(cron = "${app.alertas.cron-lluvias}", zone = "America/Lima")
    public void revisarLluvias() {
        try {
            alertaService.revisarLluviasIntensas();
        } catch (Exception e) {
            log.error("Falló la revisión programada de lluvias: {}", e.getMessage());
        }
    }
}

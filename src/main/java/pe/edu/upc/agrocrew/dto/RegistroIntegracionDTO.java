package pe.edu.upc.agrocrew.dto;

import pe.edu.upc.agrocrew.models.ServicioExterno;

import java.time.LocalDateTime;

/**
 * Registro de una llamada a un servicio externo, con su resultado y duración.
 *
 * @param id           identificador del registro
 * @param servicio     servicio externo consultado
 * @param endpoint     dirección consultada
 * @param estadoHttp   código HTTP de la respuesta
 * @param duracionMs   duración de la llamada en milisegundos
 * @param exito        indica si la llamada fue exitosa
 * @param mensajeError mensaje de error si la llamada falló
 * @param fecha        fecha y hora de la llamada
 */
public record RegistroIntegracionDTO(Long id, ServicioExterno servicio, String endpoint, Integer estadoHttp,
                                     Long duracionMs, Boolean exito, String mensajeError, LocalDateTime fecha) {
}

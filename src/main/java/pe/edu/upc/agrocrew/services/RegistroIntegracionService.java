package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.models.RegistroIntegracion;
import pe.edu.upc.agrocrew.models.ServicioExterno;

import java.util.List;

public interface RegistroIntegracionService {

    void registrar(ServicioExterno servicio, String endpoint, Integer estadoHttp,
                   long duracionMs, boolean exito, String mensajeError);

    List<RegistroIntegracion> listarRecientes();
}

package pe.edu.upc.agrocrew.dto;

/**
 * Predio encontrado en el Catastro Rural del MIDAGRI por su código catastral (US07).
 * Sirve para precargar el formulario de registro de predio.
 *
 * @param distritoId id del distrito en AgroCrew (null si el ubigeo del catastro no coincide con ninguno)
 * @param latitud    latitud del centro del predio (WGS84)
 * @param longitud   longitud del centro del predio (WGS84)
 * @param areaHa     área oficial en hectáreas
 */
public record CatastroDTO(String codigoCatastral, String nombre, String departamento, String provincia,
                          String distrito, String ubigeo, Long distritoId, Double latitud, Double longitud,
                          Double areaHa, String fuente) {
}

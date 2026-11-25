package pe.edu.upc.agrocrew.services;

import pe.edu.upc.agrocrew.dto.CatastroDTO;

/**
 * Búsqueda de predios en el Catastro Rural del MIDAGRI por código catastral (US07).
 */
public interface CatastroService {

    CatastroDTO buscar(String codigo, Long departamentoId);
}

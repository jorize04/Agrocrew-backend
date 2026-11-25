package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.CatastroDTO;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.integraciones.CatastroClient;
import pe.edu.upc.agrocrew.models.Departamento;
import pe.edu.upc.agrocrew.models.Distrito;
import pe.edu.upc.agrocrew.repositories.DepartamentoRepository;
import pe.edu.upc.agrocrew.repositories.DistritoRepository;
import pe.edu.upc.agrocrew.services.CatastroService;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * US07: el código encontrado devuelve la ubicación y el área oficiales (escenario 1); si no existe se responde
 * 404 (escenario 2) y si el MIDAGRI no responde, IntegracionException se traduce a 503 (escenario 3).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CatastroServiceImpl implements CatastroService {

    static final String FUENTE = "MIDAGRI - Catastro Rural (visor OCAPAS del SERFOR)";
    /** Solo letras, números y guiones: el código va dentro de la consulta al servicio ArcGIS. */
    private static final Pattern FORMATO_CODIGO = Pattern.compile("^[A-Za-z0-9-]{1,20}$");

    private final CatastroClient catastroClient;
    private final DepartamentoRepository departamentoRepository;
    private final DistritoRepository distritoRepository;

    @Override
    @Transactional(readOnly = true)
    public CatastroDTO buscar(String codigo, Long departamentoId) {
        String limpio = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
        if (!FORMATO_CODIGO.matcher(limpio).matches()) {
            throw new ReglaNegocioException("El código catastral solo admite letras, números y guiones");
        }
        Departamento departamento = departamentoRepository.findById(departamentoId)
                .orElseThrow(() -> new ReglaNegocioException("El departamento seleccionado no es válido"));

        CatastroClient.PredioCatastral predio = catastroClient.buscarPorCodigo(departamento.getNombre(), limpio)
                .orElseThrow(() -> new RecursoNoEncontradoException("No encontramos el código catastral " + limpio
                        + " en el Catastro Rural de " + departamento.getNombre()
                        + ". Puedes registrar tu predio marcándolo en el mapa."));

        Long distritoId = predio.ubigeo() == null ? null
                : distritoRepository.findByUbigeo(predio.ubigeo()).map(Distrito::getId).orElse(null);
        log.info("Catastro: código {} encontrado en {} (distritoId={})", limpio, departamento.getNombre(), distritoId);
        return new CatastroDTO(predio.codigo(), predio.nombre(), predio.departamento(), predio.provincia(),
                predio.distrito(), predio.ubigeo(), distritoId, predio.latitud(), predio.longitud(),
                predio.areaHa(), FUENTE);
    }
}

package pe.edu.upc.agrocrew.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agrocrew.dto.DatoEconomicoDTO;
import pe.edu.upc.agrocrew.dto.DatoEconomicoRequestDTO;
import pe.edu.upc.agrocrew.dto.RentabilidadDTO;
import pe.edu.upc.agrocrew.exceptions.ConflictoException;
import pe.edu.upc.agrocrew.exceptions.RecursoNoEncontradoException;
import pe.edu.upc.agrocrew.exceptions.ReglaNegocioException;
import pe.edu.upc.agrocrew.models.Cultivo;
import pe.edu.upc.agrocrew.models.DatoEconomico;
import pe.edu.upc.agrocrew.models.Departamento;
import pe.edu.upc.agrocrew.models.Predio;
import pe.edu.upc.agrocrew.repositories.CultivoRepository;
import pe.edu.upc.agrocrew.repositories.DatoEconomicoRepository;
import pe.edu.upc.agrocrew.repositories.DepartamentoRepository;
import pe.edu.upc.agrocrew.services.DatoEconomicoService;
import pe.edu.upc.agrocrew.services.PredioService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatoEconomicoServiceImpl implements DatoEconomicoService {

    static final String MONEDA = "PEN";
    static final String AVISO = "Cifras referenciales por hectárea. Los precios y costos reales cambian según "
            + "la campaña, el mercado y el manejo del cultivo.";

    private final DatoEconomicoRepository datoEconomicoRepository;
    private final CultivoRepository cultivoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final PredioService predioService;

    @Override
    @Transactional(readOnly = true)
    public List<DatoEconomicoDTO> listar(Long cultivoId, Long departamentoId) {
        return datoEconomicoRepository.buscar(cultivoId, departamentoId).stream().map(this::convertir).toList();
    }

    @Override
    @Transactional
    public DatoEconomicoDTO registrar(DatoEconomicoRequestDTO dto) {
        Cultivo cultivo = cultivo(dto.getCultivoId());
        Departamento region = region(dto.getDepartamentoId());
        if (datoEconomicoRepository.existsByCultivoIdAndDepartamentoIdAndPeriodo(
                cultivo.getId(), region.getId(), dto.getPeriodo())) {
            throw new ConflictoException(mensajeDuplicado(cultivo, region, dto.getPeriodo()));
        }
        DatoEconomico dato = new DatoEconomico();
        copiarDatos(dto, cultivo, region, dato);
        DatoEconomico guardado = datoEconomicoRepository.save(dato);
        log.info("Dato económico registrado id={} cultivo={} region={} periodo={}",
                guardado.getId(), cultivo.getNombre(), region.getNombre(), dto.getPeriodo());
        return convertir(guardado);
    }

    @Override
    @Transactional
    public DatoEconomicoDTO actualizar(Long id, DatoEconomicoRequestDTO dto) {
        DatoEconomico dato = datoEconomicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dato económico no encontrado"));
        Cultivo cultivo = cultivo(dto.getCultivoId());
        Departamento region = region(dto.getDepartamentoId());
        if (datoEconomicoRepository.existsByCultivoIdAndDepartamentoIdAndPeriodoAndIdNot(
                cultivo.getId(), region.getId(), dto.getPeriodo(), id)) {
            throw new ConflictoException(mensajeDuplicado(cultivo, region, dto.getPeriodo()));
        }
        copiarDatos(dto, cultivo, region, dato);
        DatoEconomico guardado = datoEconomicoRepository.save(dato);
        log.info("Dato económico actualizado id={}", id);
        return convertir(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public RentabilidadDTO rentabilidad(Long predioId, Long cultivoId) {
        Predio predio = predioService.obtenerPredioDelUsuarioActual(predioId);
        Departamento region = predio.getDistrito().getProvincia().getDepartamento();
        Cultivo cultivo = cultivoRepository.findById(cultivoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cultivo no encontrado"));

        Optional<DatoEconomico> dato = datoEconomicoRepository
                .findFirstByCultivoIdAndDepartamentoIdOrderByPeriodoDesc(cultivo.getId(), region.getId());
        if (dato.isEmpty()) {
            // US22, escenario 2: se informa sin mostrar cifras.
            return new RentabilidadDTO(predio.getId(), cultivo.getId(), cultivo.getNombre(), region.getNombre(),
                    false, null, null, null, null, null, MONEDA, null, null,
                    "Aún no hay información económica suficiente de " + cultivo.getNombre() + " en "
                            + region.getNombre() + ".", AVISO);
        }
        DatoEconomico d = dato.get();
        BigDecimal ingreso = d.getPrecioChacraKg().multiply(d.getRendimientoKgHa()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal costo = d.getCostoProduccionHa().setScale(2, RoundingMode.HALF_UP);
        BigDecimal margen = ingreso.subtract(costo);
        String mensaje = margen.signum() >= 0
                ? "Con los datos de " + d.getPeriodo() + ", el margen estimado es positivo."
                : "Con los datos de " + d.getPeriodo() + ", el costo estimado supera al ingreso.";
        return new RentabilidadDTO(predio.getId(), cultivo.getId(), cultivo.getNombre(), region.getNombre(), true,
                ingreso, costo, margen, d.getPrecioChacraKg(), d.getRendimientoKgHa(), MONEDA,
                d.getFuente(), d.getPeriodo(), mensaje, AVISO);
    }

    private Cultivo cultivo(Long id) {
        return cultivoRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El cultivo seleccionado no es válido"));
    }

    private Departamento region(Long id) {
        return departamentoRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La región seleccionada no es válida"));
    }

    private static String mensajeDuplicado(Cultivo cultivo, Departamento region, String periodo) {
        return "Ya existe un dato de " + cultivo.getNombre() + " en " + region.getNombre() + " para el periodo "
                + periodo + ". Actualice el registro existente.";
    }

    private static void copiarDatos(DatoEconomicoRequestDTO dto, Cultivo cultivo, Departamento region,
                                    DatoEconomico dato) {
        dato.setCultivo(cultivo);
        dato.setDepartamento(region);
        dato.setPrecioChacraKg(dto.getPrecioChacraKg());
        dato.setRendimientoKgHa(dto.getRendimientoKgHa());
        dato.setCostoProduccionHa(dto.getCostoProduccionHa());
        dato.setFuente(dto.getFuente().trim());
        dato.setPeriodo(dto.getPeriodo());
    }

    private DatoEconomicoDTO convertir(DatoEconomico d) {
        return new DatoEconomicoDTO(d.getId(), d.getCultivo().getId(), d.getCultivo().getNombre(),
                d.getDepartamento().getId(), d.getDepartamento().getNombre(), d.getPrecioChacraKg(),
                d.getRendimientoKgHa(), d.getCostoProduccionHa(), d.getFuente(), d.getPeriodo(),
                d.getFechaRegistro(), d.getFechaActualizacion());
    }
}

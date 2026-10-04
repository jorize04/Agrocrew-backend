package pe.edu.upc.agrocrew.services;

import org.springframework.stereotype.Service;
import pe.edu.upc.agrocrew.models.*;

import java.util.*;

/**
 * Motor de reglas de compatibilidad (TS06). Compara los datos del predio con los requerimientos
 * de cada cultivo y calcula un puntaje de 0 a 100 explicado por factor.
 * Es determinístico: los mismos datos producen siempre el mismo resultado. No usa IA.
 * La fórmula está documentada en docs/motor-reglas.md.
 */
@Service
public class MotorReglasService {

    /** Tolerancias: fuera del rango por menos de esto el factor es NEUTRO; por más, LIMITANTE. */
    static final int TOLERANCIA_ALTITUD_M = 300;
    static final double TOLERANCIA_TEMPERATURA_C = 2.0;
    static final double TOLERANCIA_LLUVIA_DEFICIT = 0.30;   // hasta 30 % menos de lluvia
    static final double TOLERANCIA_LLUVIA_EXCESO = 0.30;    // hasta 30 % más de lluvia

    static final double UMBRAL_ALTA = 75;
    static final double UMBRAL_MEDIA = 50;

    /** Orden de los grupos CUM: una tierra de un grupo "mejor" admite los usos de los grupos siguientes. */
    private static final List<String> ORDEN_CUM = List.of("A", "C", "P", "F", "X");

    public record DatosPredio(String grupoCum, Integer altitudMsnm, Double temperaturaMedia,
                              Double precipitacionAnualMm, FuenteAgua fuenteAgua, NivelRiesgo nivelRiesgo) {
    }

    public record Factor(TipoFactor tipo, String valorPredio, String valorRequerido,
                         EfectoFactor efecto, double aporte, String detalle) {
    }

    public record ResultadoCultivo(Cultivo cultivo, double puntaje, Compatibilidad compatibilidad,
                                   List<Factor> factores) {
    }

    /** Evalúa todos los cultivos y los devuelve ordenados de mayor a menor puntaje. */
    public List<ResultadoCultivo> evaluar(DatosPredio d, List<Cultivo> cultivos) {
        List<ResultadoCultivo> resultados = new ArrayList<>();
        for (Cultivo c : cultivos) {
            if (c.getRequerimiento() != null) {
                resultados.add(evaluarCultivo(d, c));
            }
        }
        resultados.sort(Comparator.comparingDouble(ResultadoCultivo::puntaje).reversed()
                .thenComparing(r -> r.cultivo().getNombre()));
        return resultados;
    }

    public ResultadoCultivo evaluarCultivo(DatosPredio d, Cultivo c) {
        RequerimientoCultivo r = c.getRequerimiento();
        List<Factor> factores = List.of(
                suelo(d, c),
                altitud(d, r),
                temperatura(d, r),
                agua(d, r),
                riesgo(d, r));

        double puntaje = factores.stream().mapToDouble(Factor::aporte).sum();

        // Un factor crítico limitante (suelo, altitud o temperatura) impide una compatibilidad media o alta.
        boolean criticoLimitante = factores.stream().anyMatch(f -> f.efecto() == EfectoFactor.LIMITANTE
                && (f.tipo() == TipoFactor.SUELO || f.tipo() == TipoFactor.ALTITUD || f.tipo() == TipoFactor.TEMPERATURA));
        boolean otroLimitante = factores.stream().anyMatch(f -> f.efecto() == EfectoFactor.LIMITANTE);
        if (criticoLimitante) {
            puntaje = Math.min(puntaje, UMBRAL_MEDIA - 1);
        } else if (otroLimitante) {
            puntaje = Math.min(puntaje, UMBRAL_ALTA - 1);
        }
        puntaje = Math.round(puntaje * 10) / 10.0;

        Compatibilidad comp = puntaje >= UMBRAL_ALTA ? Compatibilidad.ALTA
                : puntaje >= UMBRAL_MEDIA ? Compatibilidad.MEDIA : Compatibilidad.BAJA;
        return new ResultadoCultivo(c, puntaje, comp, factores);
    }

    // ------------------------------------------------------------------ factores

    private Factor suelo(DatosPredio d, Cultivo c) {
        int max = TipoFactor.SUELO.getPesoMaximo();
        List<String> gruposCultivo = c.getGruposCum().stream().map(GrupoCum::getCodigo)
                .sorted(Comparator.comparingInt(ORDEN_CUM::indexOf)).toList();
        String requerido = "Grupo " + String.join(", ", gruposCultivo);

        if (d.grupoCum() == null) {
            return new Factor(TipoFactor.SUELO, "Sin clasificación", requerido, EfectoFactor.NEUTRO, max / 2.0,
                    "No hay clasificación oficial del suelo; este factor no se pudo verificar");
        }
        String predio = "Grupo " + d.grupoCum();
        if ("X".equals(d.grupoCum())) {
            return new Factor(TipoFactor.SUELO, predio, requerido, EfectoFactor.LIMITANTE, 0,
                    "Tierras de protección: no son aptas para uso agrícola");
        }
        int ordenPredio = ORDEN_CUM.indexOf(d.grupoCum());
        boolean apto = gruposCultivo.stream()
                .anyMatch(g -> !"X".equals(g) && ORDEN_CUM.indexOf(g) >= ordenPredio);
        return apto
                ? new Factor(TipoFactor.SUELO, predio, requerido, EfectoFactor.FAVORABLE, max,
                "La capacidad de uso del suelo permite este cultivo")
                : new Factor(TipoFactor.SUELO, predio, requerido, EfectoFactor.LIMITANTE, 0,
                "El cultivo necesita tierras de mejor capacidad de uso que las del predio");
    }

    private Factor altitud(DatosPredio d, RequerimientoCultivo r) {
        int max = TipoFactor.ALTITUD.getPesoMaximo();
        String requerido = r.getAltitudMin() + " - " + r.getAltitudMax() + " msnm";
        if (d.altitudMsnm() == null) {
            return new Factor(TipoFactor.ALTITUD, "Sin dato", requerido, EfectoFactor.NEUTRO, max / 2.0,
                    "No se conoce la altitud del predio");
        }
        String predio = d.altitudMsnm() + " msnm";
        double fuera = distanciaAlRango(d.altitudMsnm(), r.getAltitudMin(), r.getAltitudMax());
        if (fuera == 0) {
            return new Factor(TipoFactor.ALTITUD, predio, requerido, EfectoFactor.FAVORABLE, max,
                    "La altitud está dentro del rango del cultivo");
        }
        if (fuera <= TOLERANCIA_ALTITUD_M) {
            return new Factor(TipoFactor.ALTITUD, predio, requerido, EfectoFactor.NEUTRO, max / 2.0,
                    "La altitud está cerca del límite del rango del cultivo");
        }
        return new Factor(TipoFactor.ALTITUD, predio, requerido, EfectoFactor.LIMITANTE, 0,
                "La altitud está fuera del rango del cultivo por " + Math.round(fuera) + " m");
    }

    private Factor temperatura(DatosPredio d, RequerimientoCultivo r) {
        int max = TipoFactor.TEMPERATURA.getPesoMaximo();
        String requerido = fmt(r.getTemperaturaMin()) + " - " + fmt(r.getTemperaturaMax()) + " °C";
        if (d.temperaturaMedia() == null) {
            return new Factor(TipoFactor.TEMPERATURA, "Sin dato", requerido, EfectoFactor.NEUTRO, max / 2.0,
                    "No se pudo obtener el clima del predio");
        }
        String predio = fmt(d.temperaturaMedia()) + " °C";
        double fuera = distanciaAlRango(d.temperaturaMedia(), r.getTemperaturaMin(), r.getTemperaturaMax());
        if (fuera == 0) {
            return new Factor(TipoFactor.TEMPERATURA, predio, requerido, EfectoFactor.FAVORABLE, max,
                    "La temperatura media está dentro del rango del cultivo");
        }
        if (fuera <= TOLERANCIA_TEMPERATURA_C) {
            return new Factor(TipoFactor.TEMPERATURA, predio, requerido, EfectoFactor.NEUTRO, max / 2.0,
                    "La temperatura media está cerca del límite del rango del cultivo");
        }
        return new Factor(TipoFactor.TEMPERATURA, predio, requerido, EfectoFactor.LIMITANTE, 0,
                "La temperatura media está fuera del rango del cultivo por " + fmt(fuera) + " °C");
    }

    private Factor agua(DatosPredio d, RequerimientoCultivo r) {
        int max = TipoFactor.AGUA.getPesoMaximo();
        String requerido = fmt(r.getPrecipitacionMinMm()) + " - " + fmt(r.getPrecipitacionMaxMm()) + " mm/año"
                + (Boolean.TRUE.equals(r.getRequiereRiego()) ? " o riego" : "");
        boolean tieneRiego = d.fuenteAgua() != null && d.fuenteAgua() != FuenteAgua.SECANO;

        if (d.precipitacionAnualMm() == null) {
            return tieneRiego
                    ? new Factor(TipoFactor.AGUA, "Con riego", requerido, EfectoFactor.FAVORABLE, max,
                    "El predio cuenta con riego")
                    : new Factor(TipoFactor.AGUA, "Sin dato de lluvia", requerido, EfectoFactor.NEUTRO, max / 2.0,
                    "No se pudo obtener la lluvia anual del predio");
        }
        String predio = fmt(d.precipitacionAnualMm()) + " mm/año" + (tieneRiego ? " + riego" : "");
        double lluvia = d.precipitacionAnualMm();

        if (lluvia < r.getPrecipitacionMinMm()) {
            if (tieneRiego) {
                return new Factor(TipoFactor.AGUA, predio, requerido, EfectoFactor.FAVORABLE, max,
                        "La lluvia es insuficiente, pero el riego del predio lo compensa");
            }
            if (Boolean.TRUE.equals(r.getRequiereRiego())) {
                return new Factor(TipoFactor.AGUA, predio, requerido, EfectoFactor.LIMITANTE, 0,
                        "El cultivo necesita riego y el predio es de secano");
            }
            boolean deficitLeve = lluvia >= r.getPrecipitacionMinMm() * (1 - TOLERANCIA_LLUVIA_DEFICIT);
            return deficitLeve
                    ? new Factor(TipoFactor.AGUA, predio, requerido, EfectoFactor.NEUTRO, max / 2.0,
                    "La lluvia está un poco por debajo de lo que necesita el cultivo")
                    : new Factor(TipoFactor.AGUA, predio, requerido, EfectoFactor.LIMITANTE, 0,
                    "La lluvia es insuficiente para el cultivo sin riego");
        }
        if (lluvia > r.getPrecipitacionMaxMm()) {
            boolean excesoLeve = lluvia <= r.getPrecipitacionMaxMm() * (1 + TOLERANCIA_LLUVIA_EXCESO);
            return excesoLeve
                    ? new Factor(TipoFactor.AGUA, predio, requerido, EfectoFactor.NEUTRO, max / 2.0,
                    "La lluvia está un poco por encima de lo ideal para el cultivo")
                    : new Factor(TipoFactor.AGUA, predio, requerido, EfectoFactor.LIMITANTE, 0,
                    "La lluvia excede lo que tolera el cultivo");
        }
        return new Factor(TipoFactor.AGUA, predio, requerido, EfectoFactor.FAVORABLE, max,
                "La lluvia anual está dentro del rango del cultivo");
    }

    private Factor riesgo(DatosPredio d, RequerimientoCultivo r) {
        int max = TipoFactor.RIESGO_HIDRICO.getPesoMaximo();
        ToleranciaInundacion tol = r.getToleranciaInundacion();
        String requerido = "Tolerancia " + tol.name().toLowerCase();
        NivelRiesgo nivel = d.nivelRiesgo() == null ? NivelRiesgo.BAJO : d.nivelRiesgo();
        String predio = "Riesgo " + nivel.name().toLowerCase().replace('_', ' ');

        if (nivel == NivelRiesgo.BAJO || (nivel == NivelRiesgo.MEDIO && tol == ToleranciaInundacion.ALTA)) {
            return new Factor(TipoFactor.RIESGO_HIDRICO, predio, requerido, EfectoFactor.FAVORABLE, max,
                    "El riesgo hídrico no afecta a este cultivo");
        }
        boolean limitante = (tol == ToleranciaInundacion.BAJA && nivel.esAltoOMayor())
                || (tol == ToleranciaInundacion.MEDIA && nivel == NivelRiesgo.MUY_ALTO);
        return limitante
                ? new Factor(TipoFactor.RIESGO_HIDRICO, predio, requerido, EfectoFactor.LIMITANTE, 0,
                "El cultivo tolera poco las inundaciones y el predio está en zona de riesgo")
                : new Factor(TipoFactor.RIESGO_HIDRICO, predio, requerido, EfectoFactor.NEUTRO, max / 2.0,
                "Hay riesgo hídrico; el cultivo lo tolera parcialmente");
    }

    // ------------------------------------------------------------------ auxiliares

    private double distanciaAlRango(double valor, double min, double max) {
        if (valor < min) {
            return min - valor;
        }
        return valor > max ? valor - max : 0;
    }

    private String fmt(Double v) {
        if (v == null) {
            return "-";
        }
        return v == Math.floor(v) ? String.valueOf(v.intValue()) : String.format(Locale.US, "%.1f", v);
    }
}
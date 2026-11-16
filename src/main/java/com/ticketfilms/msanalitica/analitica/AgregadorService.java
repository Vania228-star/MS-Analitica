package com.ticketfilms.msanalitica.analitica;

import com.ticketfilms.msanalitica.dto.CompraFallidaEvent;
import com.ticketfilms.msanalitica.dto.ResumenVentana;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgregadorService {

    private static final String MOTIVO_SIN_DATO = "sin motivo";

    private final Map<Long, Map<String, Integer>> fallasPorFuncion = new LinkedHashMap<>();
    private Instant ventanaInicio = Instant.now();

    public synchronized void registrarFalla(CompraFallidaEvent evento) {
        String motivo = (evento.motivo() == null || evento.motivo().isBlank())
                ? MOTIVO_SIN_DATO
                : evento.motivo().trim();
        fallasPorFuncion
                .computeIfAbsent(evento.funcionId(), k -> new LinkedHashMap<>())
                .merge(motivo, 1, Integer::sum);
    }

    public synchronized List<ResumenVentana> cerrarVentana() {
        Instant fin = Instant.now();
        List<ResumenVentana> resumenes = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Integer>> entry : fallasPorFuncion.entrySet()) {
            int total = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
            resumenes.add(new ResumenVentana(
                    ventanaInicio, fin, entry.getKey(), total, new TreeMap<>(entry.getValue())));
        }
        fallasPorFuncion.clear();
        ventanaInicio = fin;
        return resumenes;
    }

    public synchronized void restaurar(List<ResumenVentana> resumenes) {
        for (ResumenVentana r : resumenes) {
            r.detalleMotivos().forEach((motivo, cantidad) -> fallasPorFuncion
                    .computeIfAbsent(r.funcionId(), k -> new LinkedHashMap<>())
                    .merge(motivo, cantidad, Integer::sum));
        }
        log.warn("[ventana] {} resumen(es) devueltos al acumulado", resumenes.size());
    }
}
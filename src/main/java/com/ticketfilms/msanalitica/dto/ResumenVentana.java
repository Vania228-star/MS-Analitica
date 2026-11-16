package com.ticketfilms.msanalitica.dto;

import java.time.Instant;
import java.util.Map;

public record ResumenVentana(
        Instant ventanaInicio,
        Instant ventanaFin,
        Long funcionId,
        int totalFallidas,
        Map<String, Integer> detalleMotivos
) {}

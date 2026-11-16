package com.ticketfilms.msanalitica.dto;

import java.time.Instant;
import java.util.Map;

// Una línea de archivo .jsonl (JSON Lines): un objeto JSON válido por línea,
// sin arreglo envolvente, para leerse por partes sin cargar el archivo entero
// (formato estándar en data lakes y consultable con AWS Athena).
public record ResumenVentana(
        Instant ventanaInicio,
        Instant ventanaFin,
        Long funcionId,
        int totalFallidas,
        Map<String, Integer> detalleMotivos
) {}

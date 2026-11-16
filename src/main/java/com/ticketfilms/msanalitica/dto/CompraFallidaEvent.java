package com.ticketfilms.msanalitica.dto;

import java.util.List;

public record CompraFallidaEvent(
        String codigoBoleto,
        String emailUsuario,
        String motivo,
        Long funcionId,
        List<Long> asientosIds,
        String usuarioId
) {}

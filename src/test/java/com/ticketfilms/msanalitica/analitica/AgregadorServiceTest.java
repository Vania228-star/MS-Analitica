package com.ticketfilms.msanalitica.analitica;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketfilms.msanalitica.dto.CompraFallidaEvent;
import com.ticketfilms.msanalitica.dto.ResumenVentana;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgregadorServiceTest {

    private final AgregadorService agregador = new AgregadorService();

    private CompraFallidaEvent evento(Long funcionId, String motivo) {
        return new CompraFallidaEvent("BOL-1", "u@correo.cl", motivo, funcionId, List.of(1L), "u1");
    }

    @Test
    void agrupaPorFuncionYMotivo() {
        agregador.registrarFalla(evento(5L, "La reserva expiró"));
        agregador.registrarFalla(evento(5L, "La reserva expiró"));
        agregador.registrarFalla(evento(5L, "Pago rechazado"));
        agregador.registrarFalla(evento(8L, "Tarjeta sin fondos"));

        List<ResumenVentana> resumenes = agregador.cerrarVentana();

        assertThat(resumenes).hasSize(2);
        ResumenVentana funcion5 = resumenes.stream()
                .filter(r -> r.funcionId() == 5L).findFirst().orElseThrow();
        assertThat(funcion5.totalFallidas()).isEqualTo(3);
        assertThat(funcion5.detalleMotivos())
                .containsEntry("La reserva expiró", 2)
                .containsEntry("Pago rechazado", 1);
    }

    @Test
    void cerrarVentana_limpiaElAcumulado() {
        agregador.registrarFalla(evento(5L, "motivo"));
        agregador.cerrarVentana();
        assertThat(agregador.cerrarVentana()).isEmpty();
    }

    @Test
    void restaurar_reintegraLosConteos() {
        agregador.registrarFalla(evento(5L, "motivo"));
        List<ResumenVentana> resumenes = agregador.cerrarVentana();
        agregador.restaurar(resumenes);
        assertThat(agregador.cerrarVentana().get(0).totalFallidas()).isEqualTo(1);
    }
}
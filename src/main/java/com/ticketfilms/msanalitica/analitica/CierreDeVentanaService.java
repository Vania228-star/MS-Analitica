package com.ticketfilms.msanalitica.analitica;

import com.ticketfilms.msanalitica.dto.ResumenVentana;
import com.ticketfilms.msanalitica.listener.AnaliticaListener;
import com.ticketfilms.msanalitica.s3.S3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CierreDeVentanaService {

    private final AgregadorService agregador;
    private final S3Service s3Service;
    private final AnaliticaListener listener;

    @Scheduled(
            fixedDelayString = "${analitica.ventana-ms:300000}",
            initialDelayString = "${analitica.ventana-ms:300000}")
    public void cerrarVentana() {
        List<ResumenVentana> resumenes = agregador.cerrarVentana();

        if (resumenes.isEmpty()) {
            int acks = listener.confirmarPendientes();
            if (acks > 0) {
                log.info("[ventana] sin fallas en esta ventana, {} ACK confirmados", acks);
            }
            return;
        }

        int totalFallidas = 0;
        for (ResumenVentana r : resumenes) {
            totalFallidas += r.totalFallidas();
        }

        try {
            
            for (ResumenVentana resumen : resumenes) {
                s3Service.guardarResumen(resumen);
            }
            
            int acks = listener.confirmarPendientes();
            log.info("[ventana] cerrada — resumenes={} fallas={} acks confirmados={}",
                    resumenes.size(), totalFallidas, acks);
        } catch (RuntimeException e) {

            agregador.restaurar(resumenes);
            log.error("[ventana] falló la subida a S3: {} ACK sin confirmar — {}",
                    listener.pendientes(), e.getMessage());
        }
    }
}
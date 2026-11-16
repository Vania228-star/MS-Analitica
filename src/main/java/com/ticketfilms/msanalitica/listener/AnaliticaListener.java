package com.ticketfilms.msanalitica.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketfilms.msanalitica.analitica.AgregadorService;
import com.ticketfilms.msanalitica.dto.CompraFallidaEvent;
import com.ticketfilms.msanalitica.excepcion.MensajeInvalidoException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnaliticaListener {

    private final AgregadorService agregador;
    private final ObjectMapper objectMapper;

    private final Queue<Acknowledgment> acksPendientes = new ConcurrentLinkedQueue<>();

    @KafkaListener(topics = "compra.fallida", groupId = "ms-analitica")
    public void consumir(String mensaje, Acknowledgment ack) {
        CompraFallidaEvent evento = leerEvento(mensaje);

        agregador.registrarFalla(evento);

        acksPendientes.add(ack);

        log.info("[analitica] falla registrada — funcion={} motivo={} acks pendientes={}",
                evento.funcionId(), evento.motivo(), acksPendientes.size());
    }

    CompraFallidaEvent leerEvento(String mensaje) {
        CompraFallidaEvent evento;
        try {
            evento = objectMapper.readValue(mensaje, CompraFallidaEvent.class);
        } catch (JsonProcessingException e) {
            throw new MensajeInvalidoException("El mensaje no es un JSON válido", e);
        }
        if (evento == null || evento.funcionId() == null) {
            throw new MensajeInvalidoException("Al mensaje le falta funcionId");
        }
        return evento;
    }

    public int confirmarPendientes() {
        int confirmados = 0;
        Acknowledgment ack;
        while ((ack = acksPendientes.poll()) != null) {
            ack.acknowledge();
            confirmados++;
        }
        return confirmados;
    }

    public int pendientes() {
        return acksPendientes.size();
    }
}
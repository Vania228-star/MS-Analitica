package com.ticketfilms.msanalitica.config;

import com.ticketfilms.msanalitica.excepcion.MensajeInvalidoException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Slf4j
@Configuration
public class KafkaManejoErroresConfig {

    private static final int MAX_REINTENTOS = 3;

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {

        DeadLetterPublishingRecoverer publicador = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (registro, ex) -> new TopicPartition(registro.topic() + ".DLT", -1));

        ConsumerRecordRecoverer conRegistro = (registro, ex) -> {
            publicador.accept(registro, ex);
            log.error("Mensaje enviado a DLT — topico={} particion={} offset={} motivo={} mensaje={}",
                    registro.topic(), registro.partition(), registro.offset(),
                    ex.getMessage(), registro.value(), ex);
        };

        ExponentialBackOffWithMaxRetries espera = new ExponentialBackOffWithMaxRetries(MAX_REINTENTOS);
        espera.setInitialInterval(1000L);
        espera.setMultiplier(2.0);
        espera.setMaxInterval(4000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(conRegistro, espera);

        handler.addNotRetryableExceptions(MensajeInvalidoException.class);

        handler.setRetryListeners((registro, ex, intento)
                -> log.warn("Reintento {} de {} — topico={} offset={} motivo={}",
                        intento, MAX_REINTENTOS, registro.topic(), registro.offset(), ex.getMessage()));

        return handler;
    }
}
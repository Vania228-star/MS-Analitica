package com.ticketfilms.msanalitica.config;

// Los tópicos ya existen (los crea kafka-init del compose con if-not-exists).
// ms-analitica NO declara beans NewTopic: solo consume compra.fallida.
public final class KafkaTopics {

    public static final String COMPRA_FALLIDA = "compra.fallida";
    public static final String SUFIJO_DLT = ".DLT";

    private KafkaTopics() {
    }
}

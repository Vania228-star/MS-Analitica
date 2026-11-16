package com.ticketfilms.msanalitica.excepcion;

// La subida del resumen a S3 falló. Mientras esto pase NO se confirma el ACK
// y los conteos vuelven al acumulado: se prefiere repetir antes que perder.
public class S3SubidaException extends RuntimeException {

    public S3SubidaException(String mensaje) {
        super(mensaje);
    }

    public S3SubidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

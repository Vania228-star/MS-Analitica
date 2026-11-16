package com.ticketfilms.msanalitica.excepcion;

public class S3SubidaException extends RuntimeException {

    public S3SubidaException(String mensaje) {
        super(mensaje);
    }

    public S3SubidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

package com.ticketfilms.msanalitica.excepcion;

// Error definitivo: JSON malformado o mensaje sin funcionId.
// No se reintenta: va directo al tópico compra.fallida.DLT.
public class MensajeInvalidoException extends RuntimeException {

    public MensajeInvalidoException(String mensaje) {
        super(mensaje);
    }

    public MensajeInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

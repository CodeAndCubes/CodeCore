package com.mrleonardos.codecore.api.net;

/** Содержимое пакета не сходится с протоколом: битая передача или подделка. */
public class MalformedPacketException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MalformedPacketException(String message) {
        super(message);
    }
}

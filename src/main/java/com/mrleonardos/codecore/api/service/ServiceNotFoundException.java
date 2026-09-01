package com.mrleonardos.codecore.api.service;

/** Сервис запросили, но его никто не предоставил. */
public class ServiceNotFoundException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public ServiceNotFoundException(Class<?> type) {
        super("No implementation registered for service " + type.getName());
    }
}

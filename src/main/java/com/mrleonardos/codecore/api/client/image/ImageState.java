package com.mrleonardos.codecore.api.client.image;

/** Что сейчас с картинкой. */
public enum ImageState {

    /** Ещё качается или обрабатывается. */
    LOADING,

    /** Готова, текстуру можно рисовать. */
    READY,

    /** Не получилась: нет по адресу, слишком большая, битый формат. */
    FAILED
}

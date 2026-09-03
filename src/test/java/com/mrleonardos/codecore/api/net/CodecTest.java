package com.mrleonardos.codecore.api.net;

/** Codec на буфере поверх массива байт: ни одного класса сетевой библиотеки в тесте. */
class CodecTest extends CodecContract {

    @Override
    protected CodeBuffer buffer() {
        return new ArrayBuffer();
    }
}

package com.mrleonardos.codecore.api.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Описание поля или секции, которое ядро пишет в файл строкой комментария.
 *
 * <pre>
 *
 * public final class EconomySection {
 *
 *     &#64;Comment("Валюта, которую подставляют команды без явного имени.")
 *     public String defaultCurrency = "coin";
 *
 *     &#64;Comment({ "Границы одного перевода в минорных единицах.", "Ноль снимает нижнюю границу." })
 *     public long minTransfer = 1L;
 * }
 * </pre>
 *
 * <p>
 * Описание принадлежит коду: при каждой записи файла ядро проставляет его заново, поэтому правка текста
 * в моде доезжает до файла с первым же сохранением. Строки, дописанные человеком к своим ключам, ядро не
 * трогает.
 *
 * <p>
 * Аннотация на классе секции даёт заголовок секции, аннотация на поле даёт строку над ключом. Работает
 * только в формате {@link ConfigFormat#TOML}: json комментариев не держит.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.TYPE })
public @interface Comment {

    /** Строки описания, каждая уходит в файл отдельной строкой комментария. */
    String[] value();
}

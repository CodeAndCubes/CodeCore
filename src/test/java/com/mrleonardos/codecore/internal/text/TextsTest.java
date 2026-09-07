package com.mrleonardos.codecore.internal.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TextsTest {

    @AfterEach
    void restoreDefaultLanguage() {
        Texts.language(Texts.DEFAULT_LANGUAGE);
    }

    @Test
    void theChosenLanguageWins() {
        Texts texts = new Texts("ru_RU");

        assertEquals(
            "Дом старт поставлен",
            texts.find("texttest.message.one", "старт")
                .orElse(""));
    }

    @Test
    void aLineWithoutATranslationStaysEnglish() {
        Texts texts = new Texts("ru_RU");

        assertEquals(
            "This line has no translation",
            texts.find("texttest.message.only_english")
                .orElse(""));
    }

    @Test
    void numberSlotsTakeNumbers() {
        Texts texts = new Texts("ru_RU");

        assertEquals(
            "Домов: 3 из 5",
            texts.find("texttest.message.count", Integer.valueOf(3), Integer.valueOf(5))
                .orElse(""));
    }

    @Test
    void numberedSlotsKeepTheirOrder() {
        Texts texts = new Texts(Texts.DEFAULT_LANGUAGE);

        assertEquals(
            "house belongs to Steve",
            texts.find("texttest.message.order", "Steve", "house")
                .orElse(""));
    }

    @Test
    void aDoubledPercentBecomesOne() {
        Texts texts = new Texts(Texts.DEFAULT_LANGUAGE);

        assertEquals(
            "Full by 100%",
            texts.find("texttest.message.percent")
                .orElse(""));
    }

    @Test
    void aMissingArgumentLeavesTheLineAsItIs() {
        Texts texts = new Texts(Texts.DEFAULT_LANGUAGE);

        assertEquals(
            "Home %s is set",
            texts.find("texttest.message.one")
                .orElse(""));
    }

    @Test
    void anUnknownKeyIsLeftToTheCaller() {
        Texts texts = new Texts(Texts.DEFAULT_LANGUAGE);

        assertFalse(
            texts.find("texttest.message.absent")
                .isPresent());
        assertFalse(
            texts.find("nosuchmod.message.one")
                .isPresent(),
            "мода нет на сервере, файла нет, ключа нет");
        assertFalse(
            texts.find("keywithoutamod")
                .isPresent(),
            "по ключу без первого звена искать нечего");
    }

    @Test
    void theCaseOfTheLanguageNameDoesNotMatter() {
        assertEquals("ru_RU", new Texts("RU_ru").language());
        assertEquals("ru_RU", new Texts(" ru_ru ").language());
        assertEquals(Texts.DEFAULT_LANGUAGE, new Texts("").language());
        assertEquals(Texts.DEFAULT_LANGUAGE, new Texts(null).language());
    }

    @Test
    void theCurrentTranslatorFollowsTheSetting() {
        assertEquals(
            Texts.DEFAULT_LANGUAGE,
            Texts.current()
                .language());

        Texts.language("ru_RU");

        assertEquals(
            "Подставлять нечего",
            Texts.current()
                .find("texttest.message.plain")
                .orElse(""));

        Texts.language(Texts.DEFAULT_LANGUAGE);

        assertEquals(
            "Nothing to fill in",
            Texts.current()
                .find("texttest.message.plain")
                .orElse(""),
            "смена языка сбрасывает прочитанные таблицы, а не подмешивает их друг в друга");
    }
}

package com.mrleonardos.codecore.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

/**
 * Строки, которые мод написал в лог.
 *
 * <p>
 * Подсказка админу это и есть поведение: проверять, что она ушла в лог рядом с именем отложенного файла,
 * больше негде. С Minecraft 1.7.10 приезжает log4j 2.0-beta9, готового тестового приёмника там нет,
 * поэтому он собран здесь из {@code AbstractAppender}.
 */
public final class LogCapture extends AbstractAppender {

    private static final String NAME = "capture";

    private final List<String> lines = new ArrayList<>();
    private final org.apache.logging.log4j.core.Logger logger;
    private final Level previous;

    private LogCapture(org.apache.logging.log4j.core.Logger logger) {
        super(NAME, null, null);
        this.logger = logger;
        this.previous = logger.getLevel();
    }

    /** Подцепиться к логгеру и ловить всё, что он пишет. */
    public static LogCapture attach(org.apache.logging.log4j.Logger target) {
        org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) target;
        LogCapture capture = new LogCapture(logger);
        capture.start();
        logger.setLevel(Level.ALL);
        logger.addAppender(capture);
        return capture;
    }

    /** Отцепиться и вернуть логгеру прежний уровень. */
    public void detach() {
        logger.removeAppender(this);
        logger.setLevel(previous);
        stop();
    }

    /** Всё написанное одной строкой: по ней удобно искать подстроки. */
    public String text() {
        return String.join("\n", lines);
    }

    @Override
    public void append(LogEvent event) {
        lines.add(
            event.getMessage()
                .getFormattedMessage());
    }
}

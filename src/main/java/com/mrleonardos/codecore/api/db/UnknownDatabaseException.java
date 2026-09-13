package com.mrleonardos.codecore.api.db;

/**
 * Такой базы в настройках нет, или метка роли не однозначна.
 *
 * <p>
 * Молчаливого выбора первой подходящей записи не бывает: база, выбранная за администратора, однажды
 * окажется не той, и узнают об этом по перепутанным данным.
 */
public final class UnknownDatabaseException extends DatabaseException {

    private static final long serialVersionUID = 1L;

    public UnknownDatabaseException(String message) {
        super(message);
    }
}

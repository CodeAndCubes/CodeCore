package com.mrleonardos.codecore.internal.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import com.mrleonardos.codecore.api.db.DriverMissingException;

/**
 * Соединения от драйвера, который положил администратор.
 *
 * <p>
 * Драйвер грузится по имени класса из настроек: ядро его не шейдит и не поставляет. Нет класса, значит
 * в отказе стоит его имя и куда класть jar, а не общая фраза про недоступную базу.
 *
 * <p>
 * Соединение открывается у самого драйвера, а не через {@code DriverManager}. Тот отдаёт только те
 * драйверы, которые видит загрузчик вызывающего класса, а в 1.7.10 моды и их библиотеки живут в
 * загрузчике Forge: обращение через реестр находило бы драйвер через раз.
 */
public final class DriverConnections implements ConnectionSource {

    private final Driver driver;
    private final String url;
    private final Properties properties;

    private DriverConnections(Driver driver, String url, Properties properties) {
        this.driver = driver;
        this.url = url;
        this.properties = properties;
    }

    /**
     * Загрузить драйвер и приготовить источник.
     *
     * @throws DriverMissingException если класса драйвера нет в classpath сервера
     */
    public static DriverConnections of(String driverClass, String url, String user, String password) {
        Driver driver = load(driverClass);
        Properties properties = new Properties();
        if (user != null && !user.isEmpty()) {
            properties.setProperty("user", user);
        }
        if (password != null && !password.isEmpty()) {
            properties.setProperty("password", password);
        }
        return new DriverConnections(driver, url, properties);
    }

    private static Driver load(String driverClass) {
        try {
            return (Driver) Class.forName(driverClass)
                .newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException
            | LinkageError missing) {
            throw new DriverMissingException(
                "Driver class " + driverClass
                    + " is not on the server classpath, put the driver jar into mods/ or libs/",
                missing);
        }
    }

    @Override
    public Connection open() throws SQLException {
        Connection connection = driver.connect(url, properties);
        if (connection == null) {
            throw new SQLException("The driver did not accept url " + url);
        }
        return connection;
    }
}

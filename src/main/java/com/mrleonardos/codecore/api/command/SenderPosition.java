package com.mrleonardos.codecore.api.command;

/**
 * Точка, из которой пришла команда: измерение и целые координаты блока.
 *
 * <p>
 * Позиция есть у игрока и у командного блока. У консоли и RCON её нет, и это не потеря: координат у них не
 * бывает вовсе.
 *
 * <p>
 * Координаты целые намеренно. Дробная позиция игрока здесь ни к чему: позиция нужна, чтобы подписать автора
 * правки в журнале и найти конкретный блок в мире.
 */
public final class SenderPosition {

    private final int dimension;
    private final int x;
    private final int y;
    private final int z;

    public SenderPosition(int dimension, int x, int y, int z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Номер измерения: 0 обычный мир, -1 нижний, 1 край. */
    public int dimension() {
        return dimension;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SenderPosition)) {
            return false;
        }
        SenderPosition that = (SenderPosition) other;
        return dimension == that.dimension && x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return ((dimension * 31 + x) * 31 + y) * 31 + z;
    }

    /**
     * Запись для логов и отладки. Формат не обещан и может измениться.
     *
     * <p>
     * Подпись автора правки, которая уходит в журнал на диск, собирается из {@link #dimension()},
     * {@link #x()}, {@link #y()} и {@link #z()}, а не отсюда. Разница не в красоте: журналы домов, варпов
     * и переводов переживают обновление мода, и правка этого метода задним числом расходилась бы с тем,
     * что уже записано на серверах.
     */
    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }
}

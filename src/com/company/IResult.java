package com.company;

import com.sun.istack.internal.NotNull;

/**
 * Функциональный интерфейс для асинхронной передачи результата.
 * Аналог {@link java.util.function.Consumer}, но с более семантически понятным именем метода.
 *
 * @param <T> тип результата, передаваемого через метод action
 */
@FunctionalInterface
public interface IResult<T> {

    /**
     * Принимает результат асинхронной операции для дальнейшей обработки.
     *
     * @param t результат выполнения
     */
    void action(@NotNull T t);
}
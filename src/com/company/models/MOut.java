package com.company.models;

import java.util.List;

/**
 * Результат проверки кода
 */
public class MOut{

    /**
     * Глобальная ошибка проверки, например 404, 203 тут не живет, таймаут тоже
     * При наличии записи-проверка не прошла
     */
    public String totalErrorMessage;

    /**
     * Список Данных проверки кода (результат проверки), сколько кодов Вы заслали,
     * столько результатов вы получите
     */
    public List<MOutItems>  itemsList;

}

package com.company.models;

import java.util.List;

/**
 * Результат проверки кода
 */
public class MOut{

    /**
     * Глобальная ошибка проверки, например 404
     */
    public String totalErrorMessage;

    /**
     * Список Данных проверки кода (результат проверки)
     */
    public List<MOutItems>  itemsList;

}

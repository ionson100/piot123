package com.company.models;

import java.util.List;

/**
 * Результат проверки
 */
public class MOut {

    /**
     * Глобальное сообщение об ошибке, возникшей при выполнении запроса:
     * Если это поле не null — значит проверка не прошла
     */
    public String totalErrorMessage;

    /**
     * Список результатов проверки.
     */
    public List<MOutItems> itemsList;

    /**
     * Конструктор по умолчанию — инициализирует объект с пустыми полями.
     */
    public MOut() {
        this.totalErrorMessage = null;
        this.itemsList = null;
    }

    /**
     * Удобный конструктор для создания результата с общей ошибкой.
     *
     * @param errorMessage текст ошибки
     */
    public MOut(String errorMessage) {
        this.totalErrorMessage = errorMessage;
        this.itemsList = null;
    }

    @Override
    public String toString() {
        return "MOut{" +
                "totalErrorMessage='" + totalErrorMessage + '\'' +
                ", itemsList=" + (itemsList != null ? itemsList.size() + " items" : "null") +
                '}';
    }
}
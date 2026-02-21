package com.company.models;

/**
 * Данные проверки кода (результат проверки)
 */
public class MOutItems extends MInItems{

    public String km;

    /**
     * разрешение продать истина- продавать можно, фальш - продавать нельзя
     */
    public boolean  permitSale;


    /**
     * Причина запрете продажи
     */
    public String errorMessage;

    /**
     * Строковое значение тега 1265,
     * при аварийной ситуации крод ответа 203, это поле равно null (Прдавать можно, не заполняя тег 1260)
     */

    public String tag_1265;


    public Double mrcTobacco;
}


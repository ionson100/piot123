package com.company.models.v2;

import java.util.ArrayList;

public class CodesResponse{
    /**
     * (-)Результат обработки операции
     * Возможные значения: «0» — запрос обработан успешно; «4хх», «5хх» — получен неверный запрос
     */
    public int code;

    /**
     * (-) Текстовое описание результата выполнения метода
     * «ok» в случае успешного выполнения или сообщение об ошибке
     */
    public String description;

    public ArrayList<ItemCode> codes;


    /**]
     * (+)Уникальный идентификатор запроса Формат: UUID
     */
    public String reqId;

    /**
     * (+) Дата и время формирования запроса (в UTC)
     * Параметр возвращает дату и время с точностью до миллисекунд
     */
    public long reqTimestamp;

    /**
     * (-)Признак проверки марки в офлайн режиме.
     * Возможные значения:true — проверка офлайн; false — проверка онлайн.
     * При значении true, необходимо ориентироваться на значение поля isBlocked
     */
    public boolean isCheckedOffline;

    /**
     *  Идентификатор экземпляра ПО «Локальный модуль
     *  «Честный ЗНАК»
     */
    public String inst;


    /**
     * Версия ПО «Локальный модуль «Честный ЗНАК»
     */
    public String version;

}














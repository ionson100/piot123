package com.company.models;

import com.company.models.v2.JsonBody_v2;

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

    public JsonBody_v2 bodyV2;

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

    public String getStringForLog(){
	  StringBuilder stringBuilder = new StringBuilder();
      stringBuilder.append("Результат проверки кодов:").append(System.lineSeparator());
      for (MOutItems item : itemsList) {
          stringBuilder.append(item.getStringForLog()).append(System.lineSeparator());
      }
      return stringBuilder.toString();
   }
}
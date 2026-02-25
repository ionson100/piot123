package com.company.models;

/**
 * Результат проверки кода маркировки.
 * Содержит статус продажи, причину отказа (если есть), тег 1265 и цену для табачной продукции.
 * Вимание! Прверку лучше осуществлять по одному коду, при пакетной проверке модуль можен во
 */
public class MOutItems extends MInItems {

    /**
     * ID группы товара из справочника товаров
     * Внимание!, при пакетной проверке он на некоторые кода может не возвращаться
     */
    public Integer codeGroup;
    /**
     * Разрешение на продажу: true — можно продать, false — запрещено.
     */
    public boolean permitSale;

    /**
     * Причина запрета продажи. Отображается кассиру.
     * Может быть null, если продажа разрешена.
     */
    public String errorMessage;

    /**
     * Значение тега 1265 (ФНС), формируется при успешной проверке.
     * Равно null при аварийном режиме (статус 203) — тогда тег 1260 не заполняется.
     */
    public String tag_1265;

    /**
     * Минимальная розничная цена (МРЦ) для табачной продукции (группа 3), в рублях.
     * Должна быть указана в чеке независимо от внутренней цены в учётной системе.
     * Для серой зоны и локальной проверки (null).
     */
    public Double mrcTobacco;

    /**
     * Конструктор по умолчанию — инициализирует поля значениями по умолчанию.
     */
    public MOutItems() {
        this.permitSale = true;
        this.errorMessage = null;
        this.tag_1265 = null;
        this.mrcTobacco = null;
    }

    /**
     * Удобный конструктор для создания результата с основными полями.
     *
     * @param km          Код маркировки
     * @param permitSale  Разрешение на продажу
     * @param errorMessage Причина отказа (или null)
     */
    public MOutItems(String km, boolean permitSale, String errorMessage) {
        this();
        this.km = km;
        this.permitSale = permitSale;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "MOutItems{" +
                "idCase='" + idCase + '\'' +
                ", descriptionCase='" + descriptionCase + '\'' +
                ", km='" + km + '\'' +
                ", permitSale=" + permitSale +
                ", errorMessage='" + errorMessage + '\'' +
                ", tag_1265='" + tag_1265 + '\'' +
                ", mrcTobacco=" + (mrcTobacco != null ? String.format("%.2f", mrcTobacco) : "null") +
                '}';
    }
    public String getStringForLog(){
        StringBuilder sb = new StringBuilder();
        sb.append("Код: ").append(km).append(System.lineSeparator());
        sb.append("Продажа: ").append((permitSale==true?"разрешить":"запретить")).append(System.lineSeparator());
        if(permitSale==false){
            sb.append("Причина: ").append(errorMessage).append(System.lineSeparator());
        }
        if(mrcTobacco!=null){
            sb.append("Цена за единицу руб.: ").append(String.format("%.2f", mrcTobacco)).append(System.lineSeparator());
        }
        if(codeGroup!=null){
            sb.append("Код группы: ").append(codeGroup).append(System.lineSeparator());
        }
        sb.append("Тэг 1265: ").append(tag_1265).append(System.lineSeparator());

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MOutItems)) return false;

        MOutItems that = (MOutItems) o;
        if (permitSale != that.permitSale) return false;
        if (!km.equals(that.km)) return false;
        if (!errorMessage.equals(that.errorMessage)) return false;
        if (!tag_1265.equals(that.tag_1265)) return false;
        return mrcTobacco != null ? mrcTobacco.equals(that.mrcTobacco) : that.mrcTobacco == null;
    }

    @Override
    public int hashCode() {
        int result = km.hashCode();
        result = 31 * result + Boolean.hashCode(permitSale);
        result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
        result = 31 * result + (tag_1265 != null ? tag_1265.hashCode() : 0);
        result = 31 * result + (mrcTobacco != null ? mrcTobacco.hashCode() : 0);
        return result;
    }
}
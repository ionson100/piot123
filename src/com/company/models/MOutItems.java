package com.company.models;

/**
 * Результат проверки кода маркировки.
 * Содержит статус продажи, причину отказа (если есть), тег 1265 и цену для табачной продукции.
 */
public class MOutItems extends MInItems {

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
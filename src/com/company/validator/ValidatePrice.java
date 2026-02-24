package com.company.validator;

import com.company.models.MOutItems;
import com.company.models.v2.ItemCode;
import com.company.utils.MrcBuilder;

/**
 * Утилитарный класс для проверки цены товаров по группам (3, 12, 16).
 * Проверяет соответствие цены продажи минимальной розничной цене (MRC/МРЦ).
 */
class ValidatePrice {

    /**
     * Основной метод валидации цены в зависимости от группы товара.
     *
     * @param mOut Результат проверки (обновляется: цена, разрешение, ошибка)
     * @param code Данные о коде маркировки от PIOT
     */
    static void validate(MOutItems mOut, ItemCode code) {
        if (code.groupIds == null || code.groupIds.isEmpty()) {
            mOut.permitSale = true; // Нет группы — разрешаем по умолчанию
            return;
        }

        if (code.groupIds.contains(3)) {
            validatePrice3(mOut, code);
        } else if (code.groupIds.contains(16)) {
            validatePrice16(mOut, code);
        } else {
            // Группа 12 и другие — разрешаем продажу без проверки цены
            mOut.permitSale = true;
        }
    }

    /**
     * Проверка цены для табачной продукции (группа 3).
     * Поддерживает упаковку типа "UNIT" (штука) и "GROUP" (блок).
     */
    private static void validatePrice3(MOutItems mOut, ItemCode code) {
        if (!code.groupIds.contains(3)) {
            mOut.permitSale = true;
            return;
        }

        switch (code.packageType) {
            case "UNIT":
                double mrcUnit = MrcBuilder.getMrc(code.cis);
                mOut.mrcTobacco = mrcUnit;

                if (code.smp != null && mrcUnit * 100 < code.smp) {
                    mOut.permitSale = false;
                    mOut.errorMessage = String.format(
                            "Цена пачки: %.2f руб. меньше допустимой минимальной цены: %.2f руб.",
                            mrcUnit, code.smp / 100.0
                    );
                } else {
                    mOut.permitSale = true;
                }
                break;

            case "GROUP":
                String priceStr = code.cis.substring(30, 36);
                double priceGroupKopecks = Double.parseDouble(priceStr); // цена блока в копейках
                double priceGroupRubles = priceGroupKopecks / 100.0;
                mOut.mrcTobacco = priceGroupRubles;

                if (code.smp != null) {
                    double minTotalPrice = code.smp * code.packageQuantity;
                    if (priceGroupKopecks < minTotalPrice) {
                        mOut.permitSale = false;
                        mOut.errorMessage = String.format(
                                "Цена блока: %.2f руб. меньше допустимой минимальной цены: %.2f руб.",
                                priceGroupRubles, minTotalPrice / 100.0
                        );
                    } else {
                        mOut.permitSale = true;
                    }
                } else {
                    mOut.permitSale = true;
                }
                break;

            default:
                mOut.permitSale = true;
                break;
        }
    }

    /**
     * Проверка цены для никотиносодержащей продукции (группа 16).
     * На данный момент поддерживается только тип упаковки "UNIT".
     */
    private static void validatePrice16(MOutItems mOut, ItemCode code) {
        switch (code.packageType) {
            case "UNIT":
                double priceKopecks = getPriceNSP(code.gtin);
                double priceRubles = priceKopecks / 100.0;
                mOut.mrcTobacco = priceRubles;

                if (code.mrp != null && code.mrp > priceKopecks) {
                    mOut.permitSale = false;
                    mOut.errorMessage = String.format(
                            "Цена единицы товара: %.2f руб. меньше допустимой минимальной цены: %.2f руб.",
                            priceRubles, code.mrp / 100.0
                    );
                } else {
                    mOut.permitSale = true;
                }
                break;

            case "GROUP":
                mOut.permitSale = false;
                mOut.errorMessage = "Проверка цены для групповой упаковки (GROUP) никотиносодержащей продукции не реализована.";
                // throw new UnsupportedOperationException("GROUP package type is not supported for group 16");
                break;

            default:
                mOut.permitSale = true;
                break;
        }
    }

    /**
     * Получает цену за единицу товара из внешнего источника (например, базы данных).
     * Временная реализация — возвращает фиксированное значение.
     *
     * @param gtin GTIN товара
     * @return цена в копейках
     */
    private static double getPriceNSP(String gtin) {
        // TODO: Заменить на реальное получение цены из БД или справочника
        return 150.0 * 100; // 150 рублей → 15000 копеек
    }
}
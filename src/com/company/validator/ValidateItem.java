package com.company.validator;

import com.company.models.MOutItems;
import com.company.models.v2.ItemCode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Валидатор отдельного кода маркировки (КИЗ).
 * Проверяет статус, срок годности, структуру и другие атрибуты товара,
 * чтобы определить, разрешена ли его продажа.
 */
class ValidateItem extends BaseValidator {

    /**
     * Основной метод валидации одного кода маркировки.
     *
     * @param code Данные о коде из ответа PIOT
     * @return MOutItems — результат проверки с флагом разрешения продажи и сообщением об ошибке (если есть)
     * @throws ParseException если дата в неверном формате
     */
    MOutItems validate(ItemCode code) throws ParseException {
        MOutItems mOut = new MOutItems();
        mOut.km = code.cis;

        // Код не найден в системе ГИС МТ
        if (!code.found) {
            mOut.errorMessage = code.getErrorMessage("Код не найден в системе ГИС МТ");
            mOut.permitSale = false;
            return mOut;
        }

        // Неверная структура кода
        if (!code.valid) {
            mOut.errorMessage = code.getErrorMessage("Структура кода не верная");
            mOut.permitSale = false;
            return mOut;
        }

        // Криптографическая подпись не прошла проверку
        if (!code.verified) {
            mOut.errorMessage = code.getErrorMessage("Крипто-хвост кода не прошёл проверку");
            mOut.permitSale = false;
            return mOut;
        }

        // Код заблокирован по решению органов государственной власти
        if (code.isBlocked) {
            mOut.errorMessage = code.getErrorMessage("Код продукта заблокирован для продажи по решению ОГВ");
            mOut.permitSale = false;
            return mOut;
        }

        // Товар уже выведен из оборота (продан)
        if (code.sold) {
            mOut.errorMessage = code.getErrorMessage("Товар выведен из оборота (продан)");
            mOut.permitSale = false;
            return mOut;
        }

        // Код маркировки ещё не нанесён на упаковку
        if (!code.utilised) {
            mOut.errorMessage = code.getErrorMessage("Код маркировки не нанесён");
            mOut.permitSale = false;
            return mOut;
        }

        // Проверка реализуемости: основное правило
        if (!code.realizable) {
            // Исключение: табачная продукция (groupIds содержит 3) в серой зоне
            if (code.groupIds != null && code.groupIds.contains(3) && Boolean.TRUE.equals(code.grayZone)) {
                mOut.permitSale = true;
                return mOut;
            }

            // Во всех остальных случаях — запрет продажи
            mOut.errorMessage = code.getErrorMessage(
                    "Запрет продажи товара при отсутствии в информационной системе мониторинга сведений о его вводе в оборот."
            );
            mOut.permitSale = false;
            return mOut;
        }

        // Проверка срока годности
        String expireError = checkExpire(code);
        if (expireError != null) {
            mOut.errorMessage = code.getErrorMessage(expireError);
            mOut.permitSale = false;
            return mOut;
        }

        // Проверка цены (MRC для табака и других товаров)
        ValidatePrice.validate(mOut, code);

        // Все проверки пройдены
        mOut.permitSale = true;
        return mOut;
    }

    /**
     * Проверяет, не истек ли срок годности товара.
     *
     * @param codePiot Данные о коде
     * @return Сообщение об ошибке, если просрочено; иначе — null
     * @throws ParseException если дата в неверном формате
     */
    private String checkExpire(ItemCode codePiot) throws ParseException {
        if (codePiot.expireDate == null) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date expireDate = sdf.parse(codePiot.expireDate);
        Date currentDate = new Date();

        if (expireDate.before(currentDate)) {
            String formattedDate = sdf.format(expireDate);
            return "Продукт просрочен.\nДата окончания реализации: " + formattedDate;
        }

        return null;
    }
}
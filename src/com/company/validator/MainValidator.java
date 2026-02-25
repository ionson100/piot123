package com.company.validator;

import com.company.models.MInItems;
import com.company.models.MOut;
import com.company.models.MOutItems;
import com.company.models.v2.CodesResponse;
import com.company.models.v2.ItemCode;
import com.company.models.v2.JsonBody_v2;
import com.company.utils.UtilsPiot;
import com.google.gson.Gson;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Основной валидатор ответа от PIOT.
 * Преобразует JSON-ответ в структуру MOut, проверяя статус и детали каждого кода маркировки.
 */
public class MainValidator extends BaseValidator {

    /**
     * Основной метод валидации ответа от сервера PIOT.
     *
     * @param json     JSON-ответ от сервера
     * @param mInItems Входные данные (тестовые кейсы с КИЗ и метаданными)
     * @return MOut — результат проверки
     * @throws ParseException если разбор даты или данных не удался
     */
    public MOut validate(String json, List<MInItems> mInItems) throws ParseException {
        MOut mOut = new MOut();
        mOut.itemsList = new ArrayList<>(mInItems.size());

        // Парсинг JSON
        JsonBody_v2 bodyV2;
        try {
            bodyV2 = new Gson().fromJson(json, JsonBody_v2.class);
            mOut.bodyV2=bodyV2;
        } catch (Exception e) {
            mOut.totalErrorMessage = "Ошибка парсинга JSON: " + e.getMessage();
            return mOut;
        }

        // Проверка общего кода ошибки
        if (bodyV2.code != null) {
            mOut.totalErrorMessage = "Неизвестная ошибка при статусе 200. code: " + bodyV2.code + " json: " + json;
            return mOut;
        }

        if (bodyV2.codesResponse == null || bodyV2.codesResponse.isEmpty()) {
            mOut.totalErrorMessage = "Ответ содержит пустой массив codesResponse";
            return mOut;
        }

        CodesResponse codeBox = bodyV2.codesResponse.get(0);


        // Обработка случая проверки в оффлайне
        if (Boolean.TRUE.equals(codeBox.isCheckedOffline)) {
            for (ItemCode code : codeBox.codes) {
                MInItems mIn = UtilsPiot.getMInItem(mInItems, code.cis);
                MOutItems outItem = createMOutItemFromOffline(code, mIn,codeBox);
                mOut.itemsList.add(outItem);
            }
            return mOut;
        }

        // Проверка результата внутри codesResponse
        if (codeBox.code != 0 || !"ok".equals(codeBox.description)) {
            mOut.totalErrorMessage = "Сервер вернул ошибку: code=" + codeBox.code + ", description=" + codeBox.description;
            return mOut;
        }


        // Онлайн-режим: обработка через ValidateItem
        for (ItemCode itemCode : codeBox.codes) {
            MOutItems mOutItem = new ValidateItem().validate(itemCode);
            MInItems mIn = UtilsPiot.getMInItem(mInItems, mOutItem.km);


            // Дополнение метаданных из входных данных
            if (mIn != null) {
                mOutItem.descriptionCase = mIn.descriptionCase;
                mOutItem.idCase = mIn.idCase;
            }

            // Формирование тега 1265 (без version и inst, так как не используется в онлайн)
            mOutItem.tag_1265 = String.format("UUID=%s&Time=%d", codeBox.reqId, codeBox.reqTimestamp);

            mOut.itemsList.add(mOutItem);
        }

        return mOut;
    }

    /**
     * Создаёт объект результата для случая оффлайн-проверки.
     */
    private MOutItems createMOutItemFromOffline(ItemCode code, MInItems mIn,CodesResponse codeBox) {
        MOutItems outItem = new MOutItems();
        outItem.km = code.cis;
        outItem.permitSale = !code.isBlocked;
        outItem.errorMessage = code.isBlocked ? "Продажа заблокирована в локальном модуле." : null;

        if (mIn != null) {
            outItem.descriptionCase = mIn.descriptionCase;
            outItem.idCase = mIn.idCase;
        }

        outItem.tag_1265 = String.format(
                "UUID=%s&Time=%d&Inst=%s&Ver=%s",
                codeBox.reqId, codeBox.reqTimestamp, codeBox.inst, codeBox.version
        );

        return outItem;
    }
}
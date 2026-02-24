package com.company;

import com.company.models.MInItems;
import com.company.utils.UtilsPiot;
import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Модуль для обращения к локальному модулю проверки кодов маркировки.
 * Используется как fallback при недоступности основного сервиса PIOT.
 */
class MainRequestLocalModule {

    // Входные данные для запроса к локальному модулю
    static class LmListCode {
        List<LmItemCode> cis_list = new ArrayList<>();
    }

    static class LmItemCode {
        String cis;
    }

    // Ответ от локального модуля
    static class LocalResponseCodeItem {
        String cis;
        boolean permitSale;
        String errorMessage;
        String tag_1265;
    }

    static class LocalResponse {
        String totalError;
        List<LocalResponseCodeItem> codeItems = new ArrayList<>();
    }

    // Модели для десериализации ответа
    public static class Code {
        boolean sold;
        boolean isBlocked;
        String gtin;
        String cis;
    }

    public static class Result {
        String reqId;
        ArrayList<Code> codes;
        long reqTimestamp;
        String inst;
        String description;
        String version;
        int code;
    }

    public static class Root {
        ArrayList<Result> results;
    }

    /**
     * Отправляет список кодов в локальный модуль и возвращает результат проверки.
     *
     * @param mInItems Список входных элементов (с кодами маркировки)
     * @return LocalResponse — результат проверки или ошибка
     */
    LocalResponse check(List<MInItems> mInItems) {
        LocalResponse localResponse = new LocalResponse();
        HttpURLConnection conn = null;

        try {
            // Формирование тела запроса
            LmListCode bodyListCode = new LmListCode();
            for (MInItems item : mInItems) {
                LmItemCode lmItem = new LmItemCode();
                lmItem.cis = UtilsPiot.getCodeCore(item.km); // Извлечение ядра КИЗ
                bodyListCode.cis_list.add(lmItem);
            }

            Gson gson = new Gson();
            String jsonBody = gson.toJson(bodyListCode);

            // Настройка соединения
            URL url = new URL(UtilsPiot.URL_LM);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", UtilsPiot.CONTENT_TYPE);
            conn.setRequestProperty("Accept", UtilsPiot.CONTENT_TYPE);
            conn.setRequestProperty("Authorization", UtilsPiot.AUTHORIZATION);
            conn.setDoOutput(true);

            // Отправка тела запроса
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input);
                os.flush();
            }

            // Чтение ответа
            int status = conn.getResponseCode();
            String responseBody = UtilsPiot.GetHttpBody(conn);

            if (status != 200) {
                localResponse.totalError = "Ошибка обращения к локальному модулю: HTTP " + status + ". Ответ: " + responseBody;
                return localResponse;
            }

            // Парсинг JSON-ответа
            Root root = gson.fromJson(responseBody, Root.class);
            if (root.results == null || root.results.isEmpty()) {
                localResponse.totalError = "Ответ локального модуля пуст: отсутствует поле 'results'.";
                return localResponse;
            }

            Result result = root.results.get(0);

            if (result.code != 0) {
                localResponse.totalError = "Ошибка локального модуля: code=" + result.code + ", описание=" + result.description;
                return localResponse;
            }

            if (result.codes == null || result.codes.isEmpty()) {
                localResponse.totalError = "Локальный модуль вернул пустой список кодов.";
                return localResponse;
            }

            // Формирование результата
            for (Code code : result.codes) {
                LocalResponseCodeItem codeItem = new LocalResponseCodeItem();
                codeItem.cis = code.cis;
                codeItem.permitSale = !code.isBlocked;

                if (!codeItem.permitSale) {
                    codeItem.errorMessage = "Код не прошел проверку в локальном модуле (заблокирован)";
                }

                // Формирование тэга 1265
                codeItem.tag_1265 = String.format(
                        "UUID=%s&Time=%d&Inst=%s&Ver=%s",
                        result.reqId, result.reqTimestamp, result.inst, result.version
                );

                localResponse.codeItems.add(codeItem);
            }

            return localResponse;

        } catch (Exception ex) {
            localResponse.totalError = "Исключение при обращении к локальному модулю: " + ex.getMessage();
            ex.printStackTrace();
            return localResponse;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
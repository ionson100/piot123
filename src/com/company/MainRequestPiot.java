package com.company;

import com.company.models.MInItems;
import com.company.models.MOut;
import com.company.models.MOutItems;
import com.company.utils.UtilsPiot;
import com.company.validator.MainValidator;
import com.google.gson.Gson;
import com.sun.istack.internal.NotNull;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для выполнения HTTP-запросов к системе PIOT и обработки ответов.
 * В случае ошибок или таймаутов переключается на локальный модуль проверки.
 */
class MainRequestPiot {

    // Информация о клиенте (кассовом ПО), отправляемая в теле запроса
    static class ClientInfo {
        String name;      // Наименование ПМСР (кассового ПО)
        String version;   // Версия ПМСР (кассового ПО)
        String id;        // Идентификатор в реестре ГИС МТ
        String token;     // Токен авторизации
    }

    /**
     * Временная модель тела запроса к PIOT
     */
    public static class TempBodyPiot {
        List<String> codes = new ArrayList<>();
        ClientInfo client_info;
    }

    /**
     * Основной метод для выполнения запроса к PIOT.
     * При неудаче (ошибка сети, 5xx, таймаут) — используется локальный модуль.
     *
     * @param mInItems Список входных элементов (коды маркировки + метаданные для теста)
     * @param iResult  Колбэк для возврата результата
     */
    void RequestPiot(@NotNull List<MInItems> mInItems, @NotNull StringBuilder log, @NotNull IResult<MOut> iResult) {
        log.append(System.lineSeparator()).
                append("************** Проверка кодов с помощью ТС ПИоТ **************").
                append(System.lineSeparator()).
                append("Список проверяемых кодов:").
                append(System.lineSeparator());
        HttpsURLConnection conn = null;
        try {
            // Формирование тела запроса
            TempBodyPiot tempBody = new TempBodyPiot();
            tempBody.codes = new ArrayList<>();
            for (MInItems item : mInItems) {
                String cisBase64 = UtilsPiot.CodeToBase64(item.km);
                tempBody.codes.add(cisBase64);
                log.append(item.km).append(" [").append(cisBase64).append("]").append(System.lineSeparator());
            }

            tempBody.client_info = new ClientInfo();
            tempBody.client_info.id = UtilsPiot.ID;
            tempBody.client_info.name = UtilsPiot.NAME;
            tempBody.client_info.version = UtilsPiot.VERSION;
            tempBody.client_info.token = UtilsPiot.TOKEN;

            // Сериализация в JSON
            Gson gson = new Gson();
            String jsonBody = gson.toJson(tempBody);
            byte[] postDataBytes = jsonBody.getBytes(StandardCharsets.UTF_8);

            // Установка соединения

            String urlCore = Main.DEBUG_LOCAL ? UtilsPiot.URL_LOCAL : UtilsPiot.URL;
            log.append("URL TC РИоТ: ").append(urlCore).append(System.lineSeparator());
            log.append("Тело запроса:").append(System.lineSeparator()).append(jsonBody).append(System.lineSeparator());
            URL url = new URL(urlCore);

            conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(3500);
            conn.setConnectTimeout(3500);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", UtilsPiot.CONTENT_TYPE);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            conn.connect();

            int status = conn.getResponseCode();


            log.append("Http code: ").append(status).append(System.lineSeparator());
            String response = UtilsPiot.GetHttpBody(conn);
            log.append("Тело ответа:").append(System.lineSeparator()).append(response).append(System.lineSeparator());

            // Обработка ответа по статусу
            switch (status) {
                case 200:
                    MOut mOut = new MainValidator().validate(response, mInItems);
                    if (mOut.totalErrorMessage != null) {
                        log.append("Произошла общая ошибка при проверке кодов: ").
                                append(mOut.totalErrorMessage).
                                append(System.lineSeparator());
                    } else {
                        if (mOut.bodyV2.codesResponse.get(0).isCheckedOffline) {
                            log.append("Кода были проверены локально.").append(System.lineSeparator());
                        } else {
                            log.append("Кода были проверены online.").append(System.lineSeparator());
                        }

                        log.append(mOut.getStringForLog()).append(System.lineSeparator());
                    }
                    iResult.action(mOut);
                    break;

                case 404:
                    String errorMessage;
                    if (Main.DEBUG_LOCAL) {
                        errorMessage = "Путь Url: " + UtilsPiot.URL_LOCAL + " не найден (404)";

                    } else {
                        errorMessage = "Путь Url: " + UtilsPiot.URL + " не найден (404)";
                    }
                    log.append("Ошибка проверки кодов:").append(errorMessage).append(System.lineSeparator());
                    handleError(iResult, errorMessage);

                    break;

                case 203:
                    log.append("ТС_ПИоТ вернул аварийный режим 203").append(System.lineSeparator());
                    returnSuccessForAll(iResult, mInItems, log); // Разрешено для всех
                    break;

                default:
                    if (status >= 400 && status < 500) {
                        String errorText = "Клиентская ошибка: код " + status + System.lineSeparator() + response;
                        log.append(errorText).append(System.lineSeparator());
                        handleError(iResult, errorText);
                    } else {
                        // Серверные ошибки (5xx): fallback на локальный модуль
                        log.append("ТС ПИоТ вернул статус: ").append(status).append(" Переходим к проверке через локальный модуль.").append(System.lineSeparator());
                        errorAction(mInItems, log, iResult);
                    }
                    break;
            }

            // под вопросом UnknownHostException, стоить ли его обрабатывать
        } catch (java.net.SocketTimeoutException | UnknownHostException e) {
            // Таймаут соединения — используем локальный модуль
            log.append("ТС ПИоТ ошибка подключения либо тайм-аут.").
                    append(System.lineSeparator()).append("Переходим к проверке через локальный модуль.").append(System.lineSeparator());


            errorAction(mInItems, log, iResult);
        } catch (Exception e) {
            e.printStackTrace();
            // Любая другая ошибка (например, парсинг, сеть)
            MOut errorOut = new MOut();
            errorOut.totalErrorMessage = "Внутренняя ошибка: " + e.getMessage();
            iResult.action(errorOut);

        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void errorAction(@NotNull List<MInItems> mInItems, @NotNull StringBuilder log, @NotNull IResult<MOut> iResult) {
        MOut mOut1 = proxyLocal(mInItems, log);
        if (mOut1.totalErrorMessage != null) {
            log.append("Произошла ошибка при проверке через локальный модуль: ").
                    append(mOut1.totalErrorMessage).append(System.lineSeparator());
        } else {
            log.append(mOut1.getStringForLog()).append(System.lineSeparator());
        }
        iResult.action(mOut1);
    }

    /**
     * Возвращает ошибку через колбэк
     */
    private void handleError(IResult<MOut> iResult, String message) {
        MOut out = new MOut();
        out.totalErrorMessage = message;
        iResult.action(out);
    }

    /**
     * Возвращает успешный ответ для всех товаров
     */
    private void returnSuccessForAll(IResult<MOut> iResult, List<MInItems> mInItems, StringBuilder log) {
        MOut mOut = new MOut();
        mOut.itemsList = new ArrayList<>();

        for (MInItems item : mInItems) {
            MOutItems outItem = new MOutItems();
            outItem.descriptionCase = item.descriptionCase;
            outItem.km = item.km;
            outItem.idCase = item.idCase;
            outItem.permitSale = true;
            mOut.itemsList.add(outItem);
        }
        log.append(mOut.getStringForLog());


        iResult.action(mOut);
    }

    /**
     * Fallback-метод: запрос к локальному модулю при недоступности PIOT
     */
    MOut proxyLocal(List<MInItems> mInItems, StringBuilder log) {
        MainRequestLocalModule.LocalResponse localResponse = new MainRequestLocalModule().check(mInItems, log);
        MOut mOut = new MOut();

        if (localResponse.totalError != null) {
            mOut.totalErrorMessage = localResponse.totalError;
            return mOut;
        }

        mOut.itemsList = new ArrayList<>(localResponse.codeItems.size());
        try {
            for (MainRequestLocalModule.LocalResponseCodeItem codeItem : localResponse.codeItems) {
                MInItems mIn = UtilsPiot.getMInItem(mInItems, codeItem.cis);
                MOutItems outItem = new MOutItems();
                outItem.descriptionCase = mIn != null ? mIn.descriptionCase : null;
                outItem.idCase = mIn != null ? mIn.idCase : null;
                outItem.km = mIn != null ? mIn.km : codeItem.cis;
                outItem.tag_1265 = codeItem.tag_1265;
                outItem.permitSale = codeItem.permitSale;
                outItem.errorMessage = codeItem.errorMessage;
                mOut.itemsList.add(outItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mOut.totalErrorMessage = "Ошибка при обработке ответа локального модуля: " + e.getMessage();
        }

        return mOut;
    }
}

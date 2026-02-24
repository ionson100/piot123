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
    public class TempBodyPiot {
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
    void RequestPiot(@NotNull List<MInItems> mInItems, IResult<MOut> iResult) {
        HttpsURLConnection conn = null;
        try {
            // Формирование тела запроса
            TempBodyPiot tempBody = new TempBodyPiot();
            tempBody.codes = new ArrayList<>();
            for (MInItems item : mInItems) {
                tempBody.codes.add(UtilsPiot.CodeToBase64(item.km));
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
            URL url = new URL(UtilsPiot.URL);
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
            String response = UtilsPiot.GetHttpBody(conn);

            // Обработка ответа по статусу
            switch (status) {
                case 200:
                    iResult.action(new MainValidator().validate(response, mInItems));
                    break;

                case 404:
                    handleError(iResult, "Путь Url: " + UtilsPiot.URL + " не найден (404)");
                    break;

                case 203:
                    returnSuccessForAll(iResult, mInItems); // Разрешено для всех
                    break;

                default:
                    if (status >= 400 && status < 500) {
                        handleError(iResult, "Клиентская ошибка: код " + status + "\n" + response);
                    } else {
                        // Серверные ошибки (5xx): fallback на локальный модуль
                        iResult.action(proxyLocal(mInItems));
                    }
                    break;
            }

            // под вопросом UnknownHostException, стоить ли его обрабатывать
        } catch (java.net.SocketTimeoutException|UnknownHostException e) {
            // Таймаут соединения — используем локальный модуль
            iResult.action(proxyLocal(mInItems));
        } catch (Exception e) {
            // Любая другая ошибка (например, парсинг, сеть)
            MOut errorOut = new MOut();
            errorOut.totalErrorMessage = "Внутренняя ошибка: " + e.getMessage();
            iResult.action(errorOut);
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
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
    private void returnSuccessForAll(IResult<MOut> iResult, List<MInItems> mInItems) {
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

        iResult.action(mOut);
    }

    /**
     * Fallback-метод: запрос к локальному модулю при недоступности PIOT
     */
    MOut proxyLocal(List<MInItems> mInItems) {
        MainRequestLocalModule.LocalResponse localResponse = new MainRequestLocalModule().check(mInItems);
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

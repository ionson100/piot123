package com.company.utils;

import com.company.models.MInItems;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

/**
 * Утилитарный класс с константами и вспомогательными методами для работы с PIOT и кодами маркировки.
 */
public class UtilsPiot {


    //Остовой url для проверки локально
    public static final String URL_LOCAL = "https://localhost:51401/api/v2/codes/check";
    // Основной URL API PIOT эмулятор
    public static final String URL = "https://esm-emu.ao-esp.ru/api/v2/codes/check";
    //public static final String URL = "https://tspiot.sandbox.crptech.ru/api/v2/codes/check";
    // URL локального модуля
    public static final String URL_LM = "http://localhost:5995/api/v2/cis/outCheck";

    // Информация о кассовом ПО (ПМСР)
    public static final String NAME = "bitnic";
    public static final String VERSION = "0.0.1";
    public static final String ID = "18aa4ecf-523c-4c2a-a759-d0435f4c0408"; // Идентификатор в реестре ГИС МТ

    // Заголовки HTTP-запросов
    public static final String CONTENT_TYPE = "application/json";
    //public static final String TOKEN = "a24d98fb-88b6-479c-b5d1-f3ad986bd1f2";//TODO: заменить на реальное значение
    //public static final String AUTHORIZATION = "Basic xxxxxxxxxx"; // TODO: заменить на реальное значение

    /**
     * Кодирует строку КИЗ в формат Base64.
     *
     * @param km Полный код маркировки (КИЗ)
     * @return строка в формате Base64
     */
    public static String CodeToBase64(String km) {
        byte[] originalBytes = km.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(originalBytes);
    }

    /**
     * Извлекает "ядро" кода маркировки (CIS), убирая дополнительные данные после разделителя \u001D.
     * Для кодов длиной 29 символов — возвращает первые 21 символ.
     *
     * @param km Полный код маркировки
     * @return ядро кода (21 символ) или null, если не удалось извлечь
     */
    public static String getCodeCore(String km) {
        if (km == null) return null;

        if (km.length() == 29) {
            return km.substring(0, 21);
        }

        int index = km.indexOf('\u001D'); // GS (Group Separator)
        return index != -1 ? km.substring(0, index) : null;
    }

    /**
     * Читает тело HTTP-ответа (включая ошибки).
     *
     * @param connection Активное соединение
     * @return строка с телом ответа или сообщение об ошибке
     * @throws IOException при проблемах ввода-вывода
     */
    public static String GetHttpBody(HttpURLConnection connection) throws IOException {
        BufferedReader br;
        try {
            if (connection.getResponseCode() < 400) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            return "body response empty";
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    /**
     * Находит соответствующий MInItems по коду маркировки (полностью или по ядру).
     *
     * @param mInItems Список входных элементов
     * @param code     Код для поиска (CIS)
     * @return Найденный элемент или null
     */
    public static MInItems getMInItem(List<MInItems> mInItems, String code) {
        if (mInItems == null || code == null) return null;

        for (MInItems item : mInItems) {
            if (code.equals(item.km) || item.km.contains(code)) {
                return item;
            }
        }
        return null;
    }

    // Применяется глобально !!
    public static void disableCertificateValidation() throws Exception {

        // 1. Создаем TrustManager, который не проверяет сертификаты
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };

        // 2. Инициализируем SSLContext этим TrustManager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        // 3. Устанавливаем его как глобальный сокет-фактор по умолчанию
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // 4. Отключаем проверку соответствия имени хоста (HostnameVerifier)
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);


    }

    // Применяйте только к конкретному соединению, а не глобально
    private static void setupConnectionForSelfSigned(HttpsURLConnection conn) throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        conn.setSSLSocketFactory(sc.getSocketFactory());
        conn.setHostnameVerifier((hostname, session) -> true);
    }
}
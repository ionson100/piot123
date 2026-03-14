package com.company;

import com.company.models.MInItems;
import com.company.models.MOutItems;
import com.company.utils.UtilsPiot;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Основной класс приложения для тестирования проверки кодов маркировки.
 * Выполняет асинхронный запрос к системе PIOT с набором тестовых кейсов.
 * Внимание! При пакетной проверке модуль ведет себя не корректно, может не возвращать поле groupIds, хотя оно обязательное.
 * Проверять лучше по одному товару при добавлении в чек.
 */
public class Main {

    //Выбор места проверки кода, если false - то на сервере эмуляторе piot, true - localhost
    //Внимание!, для локальной проверки необходимо инициализировать ТС ПИоТ,
    // через Test Driver Atol, и пускай окно висит в трее иначе модуль будет заворачивать всю проверку на ЛМ
    public static final boolean DEBUG_LOCAL = false;

    public static void main(String[] args) throws Exception {

        List<MInItems> mInItems;

        if (DEBUG_LOCAL) {// проверка через локальный ТС ПИоТ (localhost)
            UtilsPiot.disableCertificateValidation();
        }

        // Инициализация и создание тестовых данных по заданным ID кейсов
        try {
            mInItems = new FactoryTest().initFactory().buildRequest("5.25");//, "5.3", "5.4", "5.5", "5.6", "5.7", "5.8", "5.9", "5.10", "5.11", "5.12", "5.14");
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации тестовых данных:");
            e.printStackTrace();
            return;
        }

        // Асинхронное выполнение запроса к PIOT
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            MainRequestPiot requestHandler = new MainRequestPiot();
            StringBuilder log = new StringBuilder();
            requestHandler.RequestPiot(mInItems, log, mOut -> {
                // Обработка результата
                if (mOut.totalErrorMessage != null) {
                    System.err.println("Произошла ошибка при проверке кодов: " + mOut.totalErrorMessage);
                } else if (mOut.itemsList == null || mOut.itemsList.isEmpty()) {
                    System.err.println("Упс... список результатов проверки пустой");
                } else {
                    // Вывод результатов по каждому элементу
                    for (MOutItems outItem : mOut.itemsList) {
                        System.out.println("\n" +
                                "Кейс теста: " + outItem.idCase + "\n" +
                                "Код маркировки: " + outItem.km + "\n" +
                                "Код группы: " + outItem.codeGroup + "\n" +
                                "Разрешение продать: " + outItem.permitSale + "\n" +
                                "Цена чек: " + outItem.mrcTobacco + " руб.\n" +
                                "Причина отказа: " + outItem.errorMessage + "\n" +
                                "Название кейса теста: " + outItem.descriptionCase + "\n" +
                                "Тэг 1265: " + outItem.tag_1265);
                    }
                }
                System.out.println(log);
            });
        }).exceptionally(throwable -> {
            System.err.println("Неожиданная ошибка в фоновом потоке:");
            throwable.printStackTrace();
            return null;
        });

        System.out.println("Выполняется проверка кодов... Нажмите Ctrl+C для завершения.");
        try {
            future.get(); // Ждём завершения асинхронной задачи
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
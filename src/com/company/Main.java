package com.company;

import com.company.models.MInItems;
import com.company.models.MOutItems;
import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Основной класс приложения для тестирования проверки кодов маркировки.
 * Выполняет асинхронный запрос к системе PIOT с набором тестовых кейсов.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        List<MInItems> mInItems;

        // Инициализация и создание тестовых данных по заданным ID кейсов
        try {
            mInItems = new FactoryTest().initFactory().buildRequest(
                    "5.1", "5.2", "5.3", "5.4", "5.5", "5.6", "5.7", "5.8", "5.9", "5.10",
                    "5.11", "5.12", "5.14"
            );
        } catch (Exception e) {
            System.err.println("Ошибка при инициализации тестовых данных:");
            e.printStackTrace();
            return;
        }

        // Асинхронное выполнение запроса к PIOT
        CompletableFuture.runAsync(() -> {
            MainRequestPiot requestHandler = new MainRequestPiot();
            requestHandler.RequestPiot(mInItems, mOut -> {
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
                                "Разрешение продать: " + outItem.permitSale + "\n" +
                                "Цена чек: " + outItem.mrcTobacco + " руб.\n" +
                                "Причина отказа: " + outItem.errorMessage + "\n" +
                                "Название кейса теста: " + outItem.descriptionCase + "\n" +
                                "Тэг 1265: " + outItem.tag_1265);
                    }
                }
            });
        });

        // Ожидание ввода от пользователя перед завершением
        System.out.println("Нажмите Enter для завершения...");
        System.in.read();
    }
}
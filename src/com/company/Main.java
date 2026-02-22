package com.company;


import com.company.models.MInItems;
import com.company.models.MOutItems;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) throws IOException {

        List<MInItems> mInItems;
        // Назначение кейсов
        try {

            // Получение списка кейсов
            mInItems = new FactoryTest().initFactory().buildRequest("00");//"5.1","5.2","5.3","5.4","5.5","5.6","5.7","5.8","5.9","5.10","5.11","5.12","5.14");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 00 эмулятор продо
        // 13 15 16
        List<MInItems> finalMInItems = mInItems;
        // Выполнение в отдельном потоке
        CompletableFuture.runAsync(() -> {

            MainRequestPiot d=  new MainRequestPiot();
            // Выполнение с возвратом результата обращение к Piot
            d.RequestPiot(finalMInItems, mOut -> {
                // Вывод результата
                if(mOut.totalErrorMessage!=null){
                    // Ошибка
                    System.out.println("Произошла ошибка при проверке кодов: "+mOut.totalErrorMessage);
                }else{
                    // Успешно
                    if(mOut.itemsList==null||mOut.itemsList.size()==0){
                        // Ошибка список результатов пустой
                        System.out.println("Упс... список результатов проверки пустой");
                        return;
                    }
                    // Вывод результатов в консоль
                    for (MOutItems outItem : mOut.itemsList) {
                        System.out.println(System.lineSeparator()+
                                "Кейс теста:"+outItem.idCase+System.lineSeparator()+
                                "Код маркировки:"+outItem.km+System.lineSeparator()+
                                "Разрешение продать:"+outItem.permitSale+System.lineSeparator()+
                                "Цена чек:"+outItem.mrcTobacco+" руб."+System.lineSeparator()+
                                "Причина отказа:"+outItem.errorMessage+System.lineSeparator()+
                                "Название кейса теста:"+outItem.descriptionCase+System.lineSeparator()+
                                "Тэг 1265:"+outItem.tag_1265);
                    }
                }

           });
        });
        // ждем завершения по нажатию клавиши
        System.in.read();
    }

}

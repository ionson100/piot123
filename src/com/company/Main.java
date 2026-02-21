package com.company;


import com.company.models.MInItems;
import com.company.models.MOutItems;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) throws IOException {

        List<MInItems> mInItems;
        try {
            mInItems = new FactoryTest().initFactory().buildRequest("5.1","5.2","5.3","5.4","5.5","5.6","5.7","5.8","5.9","5.10","5.11","5.12","5.14");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 13 15 16
        List<MInItems> finalMInItems = mInItems;
        CompletableFuture.runAsync(() -> {
            // Выполнение без возврата результата
            MainRequestPiot d=  new MainRequestPiot();
            d.RequestPiot(finalMInItems, mOut -> {
                if(mOut.totalErrorMessage!=null){
                    System.out.println("Произошла ошибка при проверке кодов: "+mOut.totalErrorMessage);
                }else{
                    if(mOut.itemsList==null||mOut.itemsList.size()==0){
                        System.out.println("Упс... список результатов проверки пустой");
                        return;
                    }
                    for (MOutItems outItem : mOut.itemsList) {
                        System.out.println(System.lineSeparator()+
                                "Кейс теста:"+outItem.idCase+System.lineSeparator()+
                                "Код макаровки:"+outItem.km+System.lineSeparator()+
                                "Разрешение продать:"+outItem.permitSale+System.lineSeparator()+
                                "Цена чек:"+outItem.mrcTobacco+" руб."+System.lineSeparator()+
                                "Причина отказа:"+outItem.errorMessage+System.lineSeparator()+
                                "Название кейса теста:"+outItem.descriptionCase+System.lineSeparator()+
                                "Тэг 1265:"+outItem.tag_1265);
                    }
                }

           });
        });
        System.in.read();
    }

}

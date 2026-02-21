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
            mInItems = new FactoryTest().initFactory().buildRequest("5.2");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

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
                        System.out.println("case:"+outItem.idCase+
                                "  sale:"+outItem.permitSale+
                                "  МРЦ чек:"+outItem.mrcTobacco+"руб."+
                                "  errorMessage:"+outItem.errorMessage+
                                "  caseName:"+outItem.descriptionCase+System.lineSeparator()+
                                "1265:"+outItem.tag_1265);
                    }
                }

           });
        });
        System.in.read();
    }

}

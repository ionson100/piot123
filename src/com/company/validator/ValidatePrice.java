package com.company.validator;

import com.company.models.MOutItems;
import com.company.models.v2.ItemCode;
import com.company.utils.MrcBuilder;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

class ValidatePrice {

    /**
     * Проверка 3,12,16 группы товаров по цене
     */
   static void validate(MOutItems mOut, ItemCode code){
       if(code.groupIds.contains(3)){
           validatePrice3(mOut,code);
       }else if(code.groupIds.contains(16)){
           validatePrice16(mOut,code);
       }else {
           //12 группа
           mOut.permitSale=true;
       }
   }
    /**
     * Проверка табачной группы
     */
    private static void validatePrice3(MOutItems mOut,ItemCode code){
        if(!code.groupIds.contains(3)) {
            mOut.permitSale=true;
            return;
        }
        switch (code.packageType){
            case "UNIT":{
                double mrc=  MrcBuilder.getMrc(code.cis);
                mOut.mrcTobacco=mrc;
                if(code.smp!=null){

                    if(mrc*100 < code.smp){
                        mOut.permitSale=false;
                        mOut.errorMessage="Цена пачки:"+mrc+"руб. меньше допустимой минимальной цены:"+code.smp/100+"руб";
                        break;
                    }

                }

                mOut.permitSale=true;

                break;
            }
            case "GROUP":{
                String priceStr=code.cis.substring(30,36);
                double price=Double.parseDouble(priceStr);
                mOut.mrcTobacco=price/100;
                if(code.smp!=null){

                    double min=code.smp*code.packageQuantity;
                    if(price < min){
                        mOut.permitSale=false;
                        mOut.errorMessage="Цена блока:"+price/100+"руб. меньше допустимой минимальной цены:"+min/100+"руб";
                        break;
                    }

                }

                mOut.permitSale=true;

                break;
            }
        }

    }

    /**
     * Проверка никотоноседержащей продукции
     */
    private static void validatePrice16(MOutItems mOut,ItemCode code){

        switch (code.packageType){
            case "UNIT":{
                double price=  getPriceNSP(code.gtin);
                mOut.mrcTobacco=price/100;
                if(code.mrp!=null){

                    if(code.mrp >price){
                        mOut.permitSale=false;
                        mOut.errorMessage="Цена единицу товара:"+price/100+"руб. меньше допустимой минимальной цены:"+code.mrp/100+"руб";
                        break;
                    }

                }

                mOut.permitSale=true;

                break;
            }
            case "GROUP":{
                throw new NotImplementedException();

            }
        }

    }

    /**
     * Получение цены за единицу товар из базы данных в копейках
     * @param gtin gtin товара
     * @return цена в копейках
     */
    private static double getPriceNSP(String gtin){
       return 150*100;

    }
}

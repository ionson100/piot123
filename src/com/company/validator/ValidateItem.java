package com.company.validator;

import com.company.models.MOutItems;
import com.company.models.v2.ItemCode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

class ValidateItem extends BaseValidator {



    MOutItems validate(ItemCode code) throws ParseException {
        MOutItems mOut=new MOutItems();
        mOut.km=code.cis;

        ////TODO вопрос
        //// Стоит ли это делать здесь? Ане при добавления в чек?
        //if(checkLocalSales(code.cis)==true){
        //    mOut.errorMessage="Продукт с кодом: "+code.cis+ " был продан ранее.";
        //    mOut.permitSale=false;
        //    return mOut;
        //}

        if(code.found==false){
            mOut.errorMessage=code.getErrorMessage("Код   не найден в системе ГИС МТ");
            mOut.permitSale=false;
            return mOut;
        }
        if(code.valid==false){
            mOut.errorMessage=code.getErrorMessage("Структура кода не верная");
            mOut.permitSale=false;
            return mOut;
        }
        if(code.verified==false){
            mOut.errorMessage=code.getErrorMessage("Крипто хвост кода не прошел проверку");
            mOut.permitSale=false;
            return mOut;
        }

        if(code.isBlocked==true){
            mOut.errorMessage=code.getErrorMessage("Код продукта заблокирован для продажи по решению ОГВ");
            mOut.permitSale=false;
            return mOut;
        }

        if(code.sold==true){
            mOut.errorMessage=code.getErrorMessage("Товар выведен из оборота (Продан)");
            mOut.permitSale=false;
            return mOut;
        }

        if(code.utilised==false){
            mOut.errorMessage=code.getErrorMessage("Код маркировки не нанесен");
            mOut.permitSale=false;
            return mOut;
        }


        if (code.realizable==false&&code.groupIds.contains(3)&&code.grayZone==true){
            //Для табачной продукции в случае параметра
            //grayZone=true разрешается продажа при
            //realizable=false
            mOut.permitSale=true;
            return mOut;
        }

        if(code.realizable==false){
            mOut.errorMessage=code.getErrorMessage("Запрет продажи товара при отсутствии в информационной " +
                    "системе мониторинга сведений о его вводе в оборот.");
            mOut.permitSale=false;
            return mOut;
        }

        String tempError= checkExpire(code);
        if(tempError!=null){
            mOut.errorMessage=code.getErrorMessage(tempError);
            mOut.permitSale=false;
            return mOut;
        }

        ValidatePrice.validate(mOut,code);
        return mOut;
    }

    private String checkExpire(ItemCode codePiot) throws ParseException {

        //2022-12-22 12:16:00
        if(codePiot.expireDate!=null){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date d = sdf.parse(codePiot.expireDate);
                Date curDate=new Date();
                if(d.getTime()<curDate.getTime()){
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = formatter.format(d);
                    return "Продукт просрочен."+System.lineSeparator()+
                            "Дата окончания реализации: "+formattedDate;
                }
        }

        return null;
    }


}

package com.company.validator;

import com.company.models.MInItems;
import com.company.models.MOut;
import com.company.models.MOutItems;
import com.company.models.v2.CodesResponse;
import com.company.models.v2.ItemCode;
import com.company.models.v2.JsonBody_v2;
import com.company.utils.UtilsPiot;
import com.google.gson.Gson;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MainValidator extends BaseValidator {



    /**
     * Точка входа проверки
     * @param json тело ответа;
     * @param mInItems Массив входных данных
     * @return Массив выходных данных
     */
    public MOut validate( String json, List<MInItems> mInItems) throws ParseException {


        MOut mOut=new MOut();
        mOut.itemsList=new ArrayList<>(mInItems.size());

        JsonBody_v2 body_v2= new Gson().fromJson(json,JsonBody_v2.class);
        if(body_v2.code!=null){
            mOut.totalErrorMessage="Неизвестная ошибка при статусе 200. code:"+body_v2.code+" json:"+json;
            return mOut;
        }
        CodesResponse codeBox =body_v2.codesResponse.get(0);
        if(codeBox.code!=0||!codeBox.description.equals("ok")){
            mOut.totalErrorMessage="Произошла ошибка, сервер вернул code:"+codeBox.code+" description:"+codeBox.description;
            return mOut;
        }
        if(codeBox.isCheckedOffline){
            for (ItemCode code : codeBox.codes) {
                MInItems mIn= UtilsPiot.getMInItem(mInItems,code.cis);
                MOutItems mOutInner=new MOutItems();
                mOutInner.descriptionCase =mIn!=null?mIn.descriptionCase :null;
                mOutInner.idCase=mIn!=null?mIn.idCase:null;
                mOutInner.km=code.cis;
                mOutInner.permitSale=!code.isBlocked;
                mOutInner.tag_1265=  "UUID="+ codeBox.reqId+
                        "&Time="+ codeBox.reqTimestamp+
                        "&Inst="+ codeBox.inst+
                        "&Ver="+ codeBox.version;
                if(!mOutInner.permitSale){
                    mOutInner.errorMessage="Продажа заблокирована в локальном модуле.";
                }
                mOut.itemsList.add(mOutInner);
            }
            return mOut;

        }
        for (ItemCode itemCode : codeBox.codes) {

            MOutItems mOutInner=new ValidateItem().validate(itemCode);
            MInItems mIn= UtilsPiot.getMInItem(mInItems,mOutInner.km);
            mOutInner.descriptionCase =mIn!=null?mIn.descriptionCase :null;
            mOutInner.idCase=mIn!=null?mIn.idCase:null;
            mOutInner.tag_1265=  "UUID="+ codeBox.reqId+
                    "&Time="+ codeBox.reqTimestamp;
            mOut.itemsList.add(mOutInner);
        }



        return mOut;
    }
}

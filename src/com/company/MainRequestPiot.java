package com.company;

import com.company.models.MInItems;
import com.company.models.MOut;
import com.company.models.MOutItems;
import com.company.utils.UtilsPiot;
import com.company.validator.MainValidator;
import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

class MainRequestPiot {

    static class ClientInfo{
        //Наименование ПМСР (кассового ПО)
        String name;
        //Версия ПМСР (кассового ПО)
        String version;
        //Идентификатор ПМСР (кассового ПО) в реестре ГИС МТ
        String id;
        //идентификатор версии библиотеки
        //public String lastkey;
        String token;

    }

    /**
     * Тело запроса
     */
    public class TempBodyPiot{
        List<String> codes= new ArrayList<>();
        ClientInfo client_info;
    }
    void RequestPiot(List<MInItems> mInItems, IResult<MOut> iResult){


        HttpsURLConnection conn = null;
        int status;
        TempBodyPiot tempBody=new TempBodyPiot();
        try {
            // формируем тело запроса
            tempBody.codes=new ArrayList<>();
            for (MInItems tempProductKmPiot : mInItems) {
                //добавляем коды as Base64
                tempBody.codes.add(UtilsPiot.CodeToBase64(tempProductKmPiot.km));
            }
            tempBody.client_info= new ClientInfo();
            tempBody.client_info.id= UtilsPiot.ID;
            tempBody.client_info.name= UtilsPiot.NAME;
            tempBody.client_info.version= UtilsPiot.VERSION;
            tempBody.client_info.token= UtilsPiot.TOKEN;


            Gson gson=new Gson();
            //формируем json
            String jsonBody=gson.toJson(tempBody);

            byte[] postDataBytes=jsonBody.getBytes(StandardCharsets.UTF_8);

            URL u = new URL(UtilsPiot.URL);
            conn = (HttpsURLConnection) u.openConnection();
            conn.setReadTimeout(3500 /*milliseconds*/);
            conn.setConnectTimeout(3500 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", UtilsPiot.CONTENT_TYPE);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);
            conn.connect();
            //получаем статус
            status = conn.getResponseCode();


            //получаем ответ
            String response=UtilsPiot.GetHttpBody(conn);

            switch (status){
                case 200:{
                    iResult.action(new MainValidator().validate(response,mInItems));
                    break;
                }
                case 404:{
                    // не тестил на реале, но эмулятор выдает 404, при ошибке, например ошиблись версией
                    MOut mOut=new MOut();
                    mOut.totalErrorMessage="Путь  Url: "+ UtilsPiot.URL +" не верный";
                    iResult.action(mOut);
                    break;
                }
                case 203:{
                    MOut mOut=new MOut();
                    for (MInItems item : mInItems) {
                        MOutItems m=new MOutItems();
                        m.descriptionCase =item.descriptionCase;
                        m.km=item.km;
                        m.idCase=item.idCase;
                        m.permitSale=true;
                        mOut.itemsList.add(m);
                    }
                    iResult.action(mOut);
                }
                default:{

                    // 4хх оибка
                    if(status>399&&status<500){
                        MOut mOut=new MOut();
                        mOut.totalErrorMessage="Произошла ошибка, сервер вернул код:"+status+System.lineSeparator()+response;
                        iResult.action(mOut);
                        break;
                    }
                    // Todo По спецификации модуль не должен возвращать 5хх, только 400 203 200
                    // но разработчики есп решили что можно, не понятно, надо ли за ними подтирать заднизу за 5хх
                    // пока просто лезу в локальный модуль при 5хх
                    iResult.action(proxyLocal(mInItems));
                }

            }

        } catch (java.net.SocketTimeoutException e) {
            // получаем таймаут, лезем в локальный модуль
            iResult.action(proxyLocal(mInItems));
        } catch (Exception e) {
            //если ошибка в ответе piot, то возвращаем ошибку
            MOut mOut=new MOut();
            mOut.totalErrorMessage=e.getMessage();
            iResult.action(mOut);
            e.printStackTrace();
        } finally {
            // закрываем соединение
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // Обращение для проверки через локальный модуль
    MOut proxyLocal(List<MInItems> mInItems){
        MainRequestLocalModule.LocalResponse localResponse= new MainRequestLocalModule().check(mInItems);
        MOut mOut=new MOut();
        if(localResponse.totalError!=null){
            mOut.totalErrorMessage=localResponse.totalError;
            return mOut;

        }else {
            // формируем ответ
            mOut.itemsList=new ArrayList<>(localResponse.codeItems.size());
            try{
                for (MainRequestLocalModule.LocalResponseCodeItem codeItem : localResponse.codeItems) {
                    MOutItems mOutItems=new MOutItems();
                    MInItems mIn= UtilsPiot.getMInItem(mInItems,codeItem.cis);
                    mOutItems.descriptionCase =mIn!=null?mIn.descriptionCase :null;
                    mOutItems.idCase=mIn!=null?mIn.idCase:null;
                    mOutItems.km=mIn!=null?mIn.km:codeItem.cis;
                    mOutItems.tag_1265=codeItem.tag_1265;
                    mOutItems.permitSale=codeItem.permitSale;
                    mOutItems.errorMessage=codeItem.errorMessage;
                    mOut.itemsList.add(mOutItems);
                }
                //возвращаем результат
                return mOut;
            }catch (Exception e1){
                e1.printStackTrace();
                mOut.totalErrorMessage="Ошибка при формировании результата из локального модуля. "+e1.getMessage();
                return mOut;
            }
        }
    }
}

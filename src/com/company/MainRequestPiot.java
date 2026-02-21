package com.company;

import com.company.models.MInItems;
import com.company.models.MOut;
import com.company.models.MOutItems;
import com.company.utils.UtilsPiot;
import com.company.validator.MainValidator;
import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class MainRequestPiot {

    class ClientInfo{
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

    public class TempBodyPiot{
        List<String> codes= new ArrayList<>();
        ClientInfo client_info;
    }
    void RequestPiot(List<MInItems> mInItems, IResult<MOut> iResult){


        HttpsURLConnection conn = null;


        int status;
        TempBodyPiot tempBody=new TempBodyPiot();


        try {





            tempBody.codes=new ArrayList<>();

            for (MInItems tempProductKmPiot : mInItems) {

                tempBody.codes.add(UtilsPiot.CodeToBase64(tempProductKmPiot.km));
            }
            tempBody.client_info=new ClientInfo();
            tempBody.client_info.id= UtilsPiot.ID;
            tempBody.client_info.name= UtilsPiot.NAME;
            tempBody.client_info.version= UtilsPiot.VERSION;
            tempBody.client_info.token= UtilsPiot.TOKEN;


            Gson gson=new Gson();
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
            status = conn.getResponseCode();






            String response=UtilsPiot.GetHttpBody(conn);



            MOut mOut=new MainValidator().validate(status,response,mInItems);

            iResult.action(mOut);



        } catch (java.net.SocketTimeoutException e) {
            MainRequestLocalModule.LocalResponse localResponse= new MainRequestLocalModule().check(mInItems);
            MOut mOut=new MOut();
            if(localResponse.totalError!=null){
                mOut.totalErrorMessage=localResponse.totalError;
                iResult.action(mOut);

            }else {


                mOut.itemsList=new ArrayList<>(localResponse.codeItems.size());
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

                iResult.action(mOut);

            }

        } catch (Exception e) {

            MOut mOut=new MOut();
            mOut.totalErrorMessage=e.getMessage();
            iResult.action(mOut);
            e.printStackTrace();
        } finally {

            if (conn != null) {
                conn.disconnect();
            }


        }
    }
}

package com.company;

import com.company.models.Lm.LmItemCode;
import com.company.models.Lm.LmListCode;
import com.company.models.MInItems;
import com.company.utils.UtilsPiot;
import com.google.gson.Gson;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


 class MainRequestLocalModule {


    class LocalResponseCodeItem{
        String cis;
        boolean permitSale;
        String errorMessage;
        String tag_1265;
    }

    class LocalResponse {

        String totalError;
        List<LocalResponseCodeItem> codeItems=new ArrayList<>();

    }

    public class Code{
        boolean sold;
        boolean isBlocked;
        String gtin;
        String cis;
    }

    public class Result{
         String reqId;
         ArrayList<Code> codes;
         long reqTimestamp;
         String inst;
         String description;
         String version;
         int code;
    }

    public class Root{
        ArrayList<Result> results;
    }





    LocalResponse check(List<MInItems> mInItems){


        LocalResponse localResponse=new LocalResponse();

        LmListCode bodyListCode=new LmListCode();



        HttpURLConnection conn = null;

        String body;


        try {

            LmItemCode codeForBodyList = new LmItemCode();
            for (MInItems item : mInItems) {
                codeForBodyList.cis= UtilsPiot.getCodeCore(item.km);
            }



            bodyListCode.cis_list.add(codeForBodyList);
            body =new Gson().toJson(bodyListCode);

            URL u = new URL(UtilsPiot.URL_LM);
            conn = (HttpURLConnection) u.openConnection();
            conn.setReadTimeout(30000 /*milliseconds*/);
            conn.setConnectTimeout(30000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", UtilsPiot.CONTENT_TYPE);
            conn.setRequestProperty("Authorization", UtilsPiot.AUTHORIZATION);

            conn.setDoInput(true);
            conn.setDoOutput(true);



            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes(StandardCharsets.UTF_8));
            os.flush();

            conn.connect();
            int status = conn.getResponseCode();
            String json= UtilsPiot.GetHttpBody(conn);
            if(status!=200){
                localResponse.totalError ="Обращение к локальному модулю.\n"+
                        "Ошибка Статус ответа:"+status+" сообщение:"+json;
                return localResponse;
            }
            Root root=new Gson().fromJson(json,Root.class);
            Result result=root.results.get(0);

            if(result.code!=0){

                localResponse.totalError="Ошибка запроса локального модуля. Code:" + result.code+" message:" +result.description;
                return localResponse;
            }

            if(result.codes==null||result.codes.size()==0){
                localResponse.totalError="модуль вернул пустое поле кодов.";
                return localResponse;

            }
            for (Code code : result.codes) {
                LocalResponseCodeItem codeItem=new LocalResponseCodeItem();
                codeItem.cis=code.cis;
                codeItem.permitSale=!code.isBlocked;
                if(!codeItem.permitSale){
                    codeItem.errorMessage="Код не прошел проверку в локальном модуле";
                }
                codeItem.tag_1265="UUID="+result.reqId+
                            "&Time="+result.reqTimestamp+
                            "&Inst="+result.inst+
                            "&Ver="+result.version;

                localResponse.codeItems.add(codeItem);

            }
            return localResponse;

        } catch (Exception ex) {
            localResponse.totalError ="Обращение к локальному модулю. Исключение:"+ex.getMessage();
            ex.printStackTrace();
            return localResponse;
        }finally {

            if(conn!=null){
                conn.disconnect();
            }
        }





    }
}

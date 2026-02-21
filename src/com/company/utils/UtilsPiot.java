package com.company.utils;

import com.company.models.MInItems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class UtilsPiot {

    public static final String URL="https://esm-emu.ao-esp.ru/api/v2/codes/check";
    public static final String URL_LM="http://localhost:5995/api/v2/cis/outCheck";


    public final static String NAME="bitnic";

    public final static String VERSION="0.0.1";
    //Идентификатор ПМСР (кассового ПО) в реестре ГИС МТ
    public final static  String ID="18aa4ecf-523c-4c2a-a759-d0435f4c0408";

    public final static String CONTENT_TYPE="application/json";
    public static final String TOKEN ="4415ec48-8096-4a03-ab46-49bac3557f86";
    public static final String AUTHORIZATION ="Basic YnNyOjMxMjg3Mw==";

    public static String CodeToBase64(String km){
        byte[] originalBytes = km.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(originalBytes);
    }
    public static String getCodeCore(String km){
        if(km.length()==29){
            return km.substring(0,21);
        }
        int i=km.indexOf("\u001D");
        if(i==-1){
            return null;
        }
        return km.substring(0,i);
    }

    public static  String GetHttpBody(HttpURLConnection connection) throws IOException {

        BufferedReader br;
        if (100 <= connection.getResponseCode() && connection.getResponseCode() <= 399) {
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            try {
                br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }catch (Exception ex){
                return "body response empty";
            }

        }
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();

    }

    public static MInItems getMInItem(List<MInItems> mInItems, String code){
        for (MInItems mInItem : mInItems) {
            if(mInItem.km.equals(code)||mInItem.km.contains(code)){
                return mInItem;
            }
        }
        return null;
    }
}


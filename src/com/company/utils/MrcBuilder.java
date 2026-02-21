package com.company.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MrcBuilder {

    private static final String str= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"%&'*+-./_,:;=<>?";

    public static  String getCrptPrice(double value){
        StringBuilder builder=new StringBuilder(4);
        double dd=value*100;
        double d= round(dd,0);
        int v= (int) (d);
        int r4=remaining(remaining(remaining(remaining(v, builder),builder),builder),builder);
        return  builder.toString();
    }

    private static int remaining(int value,StringBuilder builder){
        int s= value/80;
        int dd=value-s*80;
        builder.insert(0,str.toCharArray()[dd]);
        return s;
    }

    public static double getMrc(String s){
        String priceStr = s.substring(21, 25);
        double resilt=0;
        for (int i=0;i<priceStr.length();i++){
            int index=-1;

            for (int f=0;f<str.length();f++){
                if(str.toCharArray()[f]==priceStr.toCharArray()[i]){
                    index=f;
                    break;
                }
            }
            if(index==-1){

                return -1;
            }
            double dd=Math.pow(str.length(), 4 - (i + 1)) * index;
            resilt+=dd;
        }
        return resilt/100d;
    }
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_DOWN);
        return bd.doubleValue();
    }
}

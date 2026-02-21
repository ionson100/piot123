package com.company.utils;

public class MrcBuilder {

    private static final String str= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"%&'*+-./_,:;=<>?";

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

}

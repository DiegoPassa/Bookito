package com.zerobudget.bookito.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {
    //contiene metodi statici per funzionalità
    private static Gson gson;

    //serve a creare una stringa json da un oggetto e viceversa
    public static Gson getGsonParser() {
        if(null == gson) {
            GsonBuilder builder = new GsonBuilder();
            gson = builder.create();
        }
        return gson;
    }
}
package com.zerobudget.bookito.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {
    //contiene metodi statici per funzionalità
    private static Gson gson;

    public static String USER_ID = "AZLYEN9WqTOVXiglkPJT";

    //serve a creare una stringa json da un oggetto e viceversa
    public static Gson getGsonParser() {
        if(null == gson) {
            GsonBuilder builder = new GsonBuilder();
            gson = builder.create();
        }
        return gson;
    }

    //metodi per controllare il formato dell'isbn inserito manualmente
    public static int getSum(long isbn) {
        int count = 0;
        int sum = 0;
        do {
            sum += count % 2 == 0 ? isbn % 10 : 3 * (isbn % 10);
            count++;
            isbn /= 10;
        } while (isbn > 0);
        return sum;
    }

    public static boolean isAValidISBN(long isbn) {
        return getSum(isbn) % 10 == 0;
    }
}
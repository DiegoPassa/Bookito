package com.zerobudget.bookito.utils;

import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zerobudget.bookito.models.users.UserLibrary;

public class Utils {
    //contiene metodi statici per funzionalità
    private static Gson gson;

    public static String USER_ID;
    public static UserLibrary CURRENT_USER = new UserLibrary();

    public static String URI_PIC = "";

    public static String EMPTY_INBOX = "Nessuna richiesta ricevuta";
    public static String EMPTY_SEND = "Nessuna richiesta inviata!";

    public static void setUserId(String userId) {
        USER_ID = userId;
    }

    public static void setUriPic(String uri) {
        URI_PIC = uri;
    }

    //serve a creare una stringa json da un oggetto e viceversa
    public static Gson getGsonParser() {
        if (null == gson) {
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

    public static void toggleEmptyWarning(TextView empty, String text, int size) {
        if (size == 0) {
            empty.setText(text);
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    public static void toggleEmptyWarning(TextView empty, int size) {
        if (size == 0) {
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    public static boolean isAValidISBN(long isbn) {
        return getSum(isbn) % 10 == 0;
    }
}
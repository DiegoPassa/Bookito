package com.zerobudget.bookito.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {
    //contiene metodi statici per funzionalità
    private static Gson gson;

    public static String USER_ID;

    public static String URI_PIC = "";

    public static void setUserId(String userId) {
        USER_ID = userId;
    }

    public static void setUriPic(String uri){ URI_PIC = uri; }

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

    public static void sendPushNotification(final String body, final String title, final String fcmToken) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json = new JSONObject();
                    JSONObject notificationJson = new JSONObject();
                    JSONObject dataJson = new JSONObject();
                    notificationJson.put("text", body);
                    notificationJson.put("title", title);
                    notificationJson.put("priority", "high");
                    dataJson.put("customId", "02");
                    dataJson.put("badge", 1);
                    dataJson.put("alert", "Alert");
                    json.put("notification", notificationJson);
                    json.put("data", dataJson);
                    json.put("to", fcmToken);
                    RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization", "key=AAAA5zL0EvY:APA91bEA0DdyOpo-x4ISIkVBaHa5ecaBlQ3XCiNHrfAOYWkNcssubmiXjvpKZcnRtak4GshvkYGjJXXl_ehZMw6eNqAfqCKKuHJf4I0PWX9rO3wLJ1m361B52p3RyTOJ03HMVywc1BIN")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.i("Bene", finalResponse);
                } catch (Exception e) {
                    Log.i("Male", e.getMessage());
                }
                return null;
            }
        }.execute();
    }
}
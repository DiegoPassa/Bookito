package com.zerobudget.bookito.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.ObservableArrayList;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.neighborhood.NeighborhoodModel;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.users.UserLibrary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Utils {
    //contiene metodi statici per funzionalità
    private static Gson gson;

    public static String USER_ID;
    public static UserLibrary CURRENT_USER = new UserLibrary();

    public static String URI_PIC = "";

    public static String EMPTY_INBOX = "Nessuna richiesta ricevuta";
    public static String EMPTY_SEND = "Nessuna richiesta inviata!";

    public static ObservableArrayList<RequestModel> incomingRequests = new ObservableArrayList<>();

    public static List<NeighborhoodModel> neighborhoods = new ArrayList<>();

    public static Map<String, ArrayList<String>> neighborhoodsMap = new HashMap<>();

    public static void setUserId(String userId) {
        USER_ID = userId;
    }

    public static void setUriPic(String uri) {
        URI_PIC = uri;
    }

    public static void neighborhoodsToMap() {
        for (NeighborhoodModel n : neighborhoods) {
            neighborhoodsMap.put(n.getComune(), (ArrayList<String>) n.getQuartieri());
        }
    }

    //serve a creare una stringa json da un oggetto e viceversa
    public static Gson getGsonParser() {
        if (null == gson) {
            GsonBuilder builder = new GsonBuilder();
            gson = builder.create();
        }
        return gson;
    }

    public static double truncateDoubleValue(double value, int decimalpoint) {
        value = value * Math.pow(10, decimalpoint);
        value = Math.floor(value);
        value = value / Math.pow(10, decimalpoint);

        return value;
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
        if (size == 0 && empty != null) {
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
    }

    public static List<NeighborhoodModel> getNeighborhoods() {
        return neighborhoods;
    }

    public static void setNeighborhoods(List<NeighborhoodModel> neighborhoods) {
        Utils.neighborhoods = neighborhoods;
    }

    // ICMP
    public static boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * metodo per caricare l'icona sulla base del tipo del libro
     */
    public static void setUpIconBookType(String type, ImageView icon_type) {
        switch (type) {
            case "Scambio":
                Picasso.get().load(R.drawable.swap).into(icon_type);
                break;
            case "Prestito":
                Picasso.get().load(R.drawable.calendar).into(icon_type);
                break;
            case "Regalo":
                Picasso.get().load(R.drawable.gift).into(icon_type);
                break;
            default:
                break;
        }
    }

    /**
     * modifica lo stato di un libro (tramite isbn)
     * la modifica viene fatta rimuovendo il libro e inserendolo nuovamente con il nuovo stato
     * perché firebase non permette di modificare un valore all'interno della strutura dati in cui essi sono contenuti
     *
     * @param db:     database di riferimento su Firebase
     * @param userID: id dell'utente di riferimento
     * @param isbn:   isbn del libro che necessita del cambiamento di stato
     */
    public static void changeBookStatus(FirebaseFirestore db, String userID, String isbn, boolean newStatus) {
        db.collection("users").document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Object arr = task.getResult().get("books"); //array dei books
                if (arr != null) //si assicura di cercare solo se esiste quache libro
                    for (Object o : (ArrayList<Object>) arr) {
                        HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                        if (map.get("isbn").equals(isbn)) {
                            BookModel oldBook = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
                            BookModel newBook = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), newStatus);

                            //firebase non permette di modificare il valore, va rimosso l'elemento dell'array e inserito con i valori modificati
                            db.collection("users").document(userID).update("books", FieldValue.arrayRemove(oldBook));
                            db.collection("users").document(userID).update("books", FieldValue.arrayUnion(newBook));
                        }
                    }
            }
        });
    }


    /**
     * elimina un libro dalla libreria dell'utente, sulla base dell'isbn
     *
     * @param db:     database di riferimento su Firebase
     * @param userID: id dell'utente di riferimento
     * @param isbn:   isbn del libro che necessita del cambiamento di stato
     */
    public static void deleteUserBook(FirebaseFirestore db, String userID, String isbn) {
        db.collection("users").document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Object arr = task.getResult().get("books"); //array dei books
                if (arr != null) //si assicura di cercare solo se esiste quache libro
                    for (Object o : (ArrayList<Object>) arr) {
                        HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                        if (map.get("isbn").equals(isbn)) {
                            db.collection("users").document(userID).update("books", FieldValue.arrayRemove(map));
                        }
                    }
            }
        });
    }
}
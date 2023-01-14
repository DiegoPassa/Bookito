package com.zerobudget.bookito.ui.add;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentAddBinding;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.utils.Utils;

import org.apache.commons.validator.routines.ISBNValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    private RequestQueue mRequestQueue;
    private BookModel newBook;
    private View root;

    private ProgressBar spinner;

    private FirebaseFirestore db;

    /**
     * interazione con l'api di google books per la ricerca del libro tramite isbn scannerizzato
     */
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) { //isbn scannerizzato

            ISBNValidator validator = new ISBNValidator();
            String isbn = result.getContents();

            if (isbn.length() == 10 && validator.isValidISBN10(isbn))
                isbn = validator.convertToISBN13(isbn);

            if (validator.isValidISBN13(isbn))
                searchBookAPI(isbn);
            else {
                AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this.getContext());
                builder.setTitle("Attenzione");
                builder.setMessage("L'isbn non è valido, si prega di riprovare");
                builder.setPositiveButton("OK", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();
            }
        }
    });

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.newBook = new BookModel();

        this.newBook.setStatus(BookModel.ENABLE);

        db = FirebaseFirestore.getInstance();

        binding = FragmentAddBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        spinner = root.findViewById(R.id.progressBar);

        binding.scanBtn.setOnClickListener(view -> {
            ScanOptions options = new ScanOptions();
            options.setBeepEnabled(false);
            options.setPrompt("Premi 'volume su' per accendere la torcia");
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);

            barLauncher.launch(options);
        });

        binding.isbnNumber.addTextChangedListener(new TextWatcher() {
            private int previousLength;
            private boolean backSpace;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                previousLength = charSequence.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //si assicura che vegnano inserti almeno 10 caratteri
                if (editable.toString().length() < 10) {
                    binding.btnAdd.setEnabled(false);
                }
                if (editable.toString().length() >= 10)
                    binding.btnAdd.setEnabled(true);
            }
        });

        binding.btnAdd.setOnClickListener(view -> {
            hideKeyboard(getActivity());
            spinner.setVisibility(View.VISIBLE);

            String isbn = binding.isbnNumber.getText().toString();

            //crea un validator ISBN
            ISBNValidator validator = new ISBNValidator();
            //se l'isbn inserito è da dieci controlla se è valido e converte a 13
            if (isbn.length() == 10 && validator.isValidISBN10(isbn))
                isbn = validator.convertToISBN13(isbn);

            if (validator.isValidISBN13(isbn))
                searchBookAPI(isbn);
            else {
                AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this.getContext());
                builder.setTitle("Attenzione");
                builder.setMessage("L'isbn inserito non è valido, si prega di riprovare");
                builder.setPositiveButton("OK", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }).show();
                binding.isbnNumber.setError("Attezione! L'isbn non è valido");
                binding.isbnNumber.requestFocus();
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * metodo per nascondere la tastiera
     *
     * @param activity: activty di riferimento
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    static public class CaptureAct extends CaptureActivity {
    }

    /**
     * preleva i dati dall'api di google maps, convertendo l'oggetto json ottenuto in base al formato deciso da goolge
     *
     * @param isbn: isbn del libro cercato
     */
    private void searchBookAPI(String isbn) {
        mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        mRequestQueue.getCache().clear();
        // url per cercare il libro in base all'ISBN scannerizzato
        String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;

        // below line we are  creating a new request queue.
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            spinner.setVisibility(View.GONE);
            boolean foundByIsbn = false;
            try {
                //prova a prlevare i dati in risposta dall'API di goole books
                JSONArray itemsArray = response.getJSONArray("items");
                JSONObject itemsObj = itemsArray.getJSONObject(0);
                JSONObject volumeObj = itemsObj.getJSONObject("volumeInfo");

                //riempe newBook con i dati prelevati
                newBook.setIsbn(isbn);
                newBook.setTitle(volumeObj.optString("title"));

                JSONArray authorsArray = volumeObj.getJSONArray("authors");

                String description = volumeObj.optString("description");
                if (!description.equals(""))
                    newBook.setDescription(description);
                else
                    newBook.setDescription("No description found");

                //data we might need in the future!

                //String subtitle = volumeObj.optString("subtitle");
                //String publisher = volumeObj.optString("publisher");
                //String publishedDate = volumeObj.optString("publishedDate");
                //String previewLink = volumeObj.optString("previewLink");
                //String infoLink = volumeObj.optString("infoLink");
                //int pageCount = volumeObj.optInt("pageCount");
                //JSONObject saleInfoObj = itemsObj.optJSONObject("saleInfo");
                //String buyLink = saleInfoObj.optString("buyLink");

                JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");


                if (imageLinks != null) {
                    //l'API rende un link che inizia con http ma Picasso, usato per estrarre l'immagine ha bisogno dell'https
                    newBook.setThumbnail("https".concat(imageLinks.optString("thumbnail").substring(4)));
                } else {
                    //se l'immagine non è disponibile ne viene usata una di default
                    newBook.setThumbnail("https://feb.kuleuven.be/drc/LEER/visiting-scholars-1/image-not-available.jpg/image");
                }

                //soltanto il primo autore viene utilizzato
                ArrayList<String> authorsArrayList = new ArrayList<>();
                if (authorsArray.length() != 0) {
                    for (int j = 0; j < authorsArray.length(); j++) {
                        authorsArrayList.add(authorsArray.optString(0));
                    }
                    newBook.setAuthor(authorsArrayList.get(0));
                } else
                    newBook.setAuthor(null);

                //passaggio dei dati del new book al prossimo fragment
                Bundle args = new Bundle();
                String bookString = Utils.getGsonParser().toJson(newBook);
                args.putString("BK", bookString);

                foundByIsbn = true;
                //spinner.setVisibility(View.GONE);
                Navigation.findNavController(root).navigate(R.id.action_navigation_insertNew_to_addConfirmFragment, args);

            } catch (JSONException e) {
                Toast.makeText(getContext().getApplicationContext(), "Attendere prego, sarà necessario più tempo del previsto!", Toast.LENGTH_LONG).show();
            }

            //a volte google boooks api non trova i libri tramite isbn, non si sa perché
            //nel dubbio viene fatto un tentativo di ricerca senza il filtro isbn: cercando tra i primi 40 risultati
            //in caso non si trovasse si avvisa l'utente
            if (!foundByIsbn) {
                String url_2 = "https://www.googleapis.com/books/v1/volumes?q=" + isbn + "&maxResults=40";

                JsonObjectRequest booksObjrequestNotByIsbn = new JsonObjectRequest(Request.Method.GET, url_2, null, responseNotByIsbn -> {
                    spinner.setVisibility(View.GONE);
                    boolean foundNotByIsbn = false;
                    try {
                        //prova a prlevare i dati in risposta dall'API di goole books
                        JSONArray itemsArray = responseNotByIsbn.getJSONArray("items");
                        Bundle args = null;
                        //scorre per tutti i risultati trovati
                        for (int i = 0; i < itemsArray.length(); i++) {
                            JSONObject itemsObj = itemsArray.getJSONObject(i);

                            JSONObject volumeObj = itemsObj.getJSONObject("volumeInfo");
                            JSONArray isbnArr = volumeObj.getJSONArray("industryIdentifiers");

                            //confronta l'isbn trovato con quello che l'utente ha cercato
                            if (isbnArr.getJSONObject(0).optString("identifier").equals(isbn) || isbnArr.getJSONObject(1).optString("identifier").equals(isbn)) {

                                //riempe newBook con i dati prelevati
                                newBook.setIsbn(isbn);
                                newBook.setTitle(volumeObj.optString("title"));

                                JSONArray authorsArray = volumeObj.getJSONArray("authors");

                                String description = volumeObj.optString("description");
                                if (!description.equals(""))
                                    newBook.setDescription(description);
                                else
                                    newBook.setDescription("No description found");

                                JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");

                                if (imageLinks != null) {
                                    //l'API rende un link che inizia con http ma Picasso, usato per estrarre l'immagine ha bisogno dell'https
                                    newBook.setThumbnail("https".concat(imageLinks.optString("thumbnail").substring(4)));
                                } else {
                                    //se l'immagine non è disponibile ne viene usata una di default
                                    newBook.setThumbnail("https://feb.kuleuven.be/drc/LEER/visiting-scholars-1/image-not-available.jpg/image");
                                }

                                //soltanto il primo autore viene utilizzato
                                ArrayList<String> authorsArrayList = new ArrayList<>();
                                if (authorsArray.length() != 0) {
                                    for (int j = 0; j < authorsArray.length(); j++) {
                                        authorsArrayList.add(authorsArray.optString(0));
                                    }
                                    newBook.setAuthor(authorsArrayList.get(0));
                                } else
                                    newBook.setAuthor(null);

                                //passaggio dei dati del new book al prossimo fragment
                                args = new Bundle();
                                String bookString = Utils.getGsonParser().toJson(newBook);
                                args.putString("BK", bookString);

                                foundNotByIsbn = true;
                                Navigation.findNavController(root).navigate(R.id.action_navigation_insertNew_to_addConfirmFragment, args);
                            }
                        }

                        if (!foundNotByIsbn) {
                            Toast.makeText(getContext().getApplicationContext(), "Oh no, il libro non è stato trovato", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException ex) {
                        Toast.makeText(getContext().getApplicationContext(), "Oh no, il libro non è stato trovato", Toast.LENGTH_LONG).show();
                        ex.printStackTrace();
                    }

                }, error -> {
                    if (!Utils.isOnline())
                        Toast.makeText(getContext().getApplicationContext(), "Sembra che tu non sia connesso ad internet, connettiti e riprova!", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getContext().getApplicationContext(), "Qualcosa è andato storto", Toast.LENGTH_LONG).show();
                });

                queue.add(booksObjrequestNotByIsbn);
            }

        }, error -> {
            if (!Utils.isOnline())
                Toast.makeText(getContext().getApplicationContext(), "Sembra che tu non sia connesso ad internet, connettiti e riprova!", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getContext().getApplicationContext(), "Qualcosa è andato storto", Toast.LENGTH_LONG).show();
        });

        queue.add(booksObjrequest);
    }
}
package com.zerobudget.bookito.ui.add;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentAddBinding;
import com.zerobudget.bookito.ui.library.BookModel;
import com.zerobudget.bookito.ui.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    private RequestQueue mRequestQueue;
    private BookModel newBook;
    private View root;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) { //isbn

            mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
            mRequestQueue.getCache().clear();
            // url per cercare il libro in base all'ISBN scannerizzato
            String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + result.getContents();
            // below line we are  creating a new request queue.
            RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());

            JsonObjectRequest booksObjrequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
                try {
                    //prova a prlevare i dati in risposta dall'API di goole books
                    JSONArray itemsArray = response.getJSONArray("items");

                    JSONObject itemsObj = itemsArray.getJSONObject(0);
                    JSONObject volumeObj = itemsObj.getJSONObject("volumeInfo");

                    //riempe newBook con i dati prelevati
                    newBook.setIsbn(result.getContents());

                    newBook.setTitle(volumeObj.optString("title"));

                    String subtitle = volumeObj.optString("subtitle");
                    JSONArray authorsArray = volumeObj.getJSONArray("authors");
                    String publisher = volumeObj.optString("publisher");
                    String publishedDate = volumeObj.optString("publishedDate");

                    String description = volumeObj.optString("description");
                    if(!description.equals(""))
                        newBook.setDescription(description);
                    else
                        newBook.setDescription("No description found");

                    int pageCount = volumeObj.optInt("pageCount");
                    JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");

                    //l'API rende un link che inizia con http
                    //Picasso, usato per estrarre l'immagine ha bisogno dell'https
                    if (imageLinks != null) {
                        newBook.setThumbnail("https".concat(imageLinks.optString("thumbnail").substring(4)));
                    } else {
                        newBook.setThumbnail("https://feb.kuleuven.be/drc/LEER/visiting-scholars-1/image-not-available.jpg/image");
                    }

                    String previewLink = volumeObj.optString("previewLink");
                    String infoLink = volumeObj.optString("infoLink");
                    JSONObject saleInfoObj = itemsObj.optJSONObject("saleInfo");
                    String buyLink = saleInfoObj.optString("buyLink");

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

                    Navigation.findNavController(root).navigate(R.id.action_navigation_insertNew_to_confirmAddFragment, args);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                //TODO
                Toast.makeText(this.getContext(), "Errore!", Toast.LENGTH_LONG);
            });
            queue.add(booksObjrequest);
        }
    });


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.newBook = new BookModel();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        AddViewModel addViewModel =
                new ViewModelProvider(this).get(AddViewModel.class);

        binding = FragmentAddBinding.inflate(inflater, container, false);
        root = binding.getRoot();





        //TextView textView = binding.textNotifications;
        //addViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //addViewModel.getScore().observe(getViewLifecycleOwner(), textView::setText);

       /* binding.addOneBtm.setOnClickListener(v -> {
            addViewModel.plusScore();
        });

        binding.subOneBtn.setOnClickListener(view -> {
            addViewModel.subScore();
            // Toast.makeText(getActivity().getApplicationContext(), newBook.getTitle(), Toast.LENGTH_SHORT).show();
        });*/

        binding.scanBtn.setOnClickListener(view -> {
            ScanOptions options = new ScanOptions();
            options.setBeepEnabled(false);
            options.setPrompt("Premi 'volume su' per accendere la torcia");
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);

            barLauncher.launch(options);
        });

        Log.d("USER NOW", ""+UserModel.getCurrentUser().serialize());
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    static public class CaptureAct extends CaptureActivity {

    }
}
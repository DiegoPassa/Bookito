package com.zerobudget.bookito.ui.add;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.zerobudget.bookito.databinding.FragmentAddBinding;
import com.zerobudget.bookito.ui.library.BookModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;
    private RequestQueue mRequestQueue;
    private BookModel newBook;

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
                    newBook.setTitle(volumeObj.optString("title"));

                    String subtitle = volumeObj.optString("subtitle");
                    JSONArray authorsArray = volumeObj.getJSONArray("authors");
                    String publisher = volumeObj.optString("publisher");
                    String publishedDate = volumeObj.optString("publishedDate");
                    String description = volumeObj.optString("description");
                    int pageCount = volumeObj.optInt("pageCount");
                    JSONObject imageLinks = volumeObj.optJSONObject("imageLinks");

                    newBook.setThumbnail(imageLinks.optString("thumbnail"));

                    String previewLink = volumeObj.optString("previewLink");
                    String infoLink = volumeObj.optString("infoLink");
                    JSONObject saleInfoObj = itemsObj.optJSONObject("saleInfo");
                    String buyLink = saleInfoObj.optString("buyLink");

                    ArrayList<String> authorsArrayList = new ArrayList<>();
                    if (authorsArray.length() != 0) {
                        for (int j = 0; j < authorsArray.length(); j++) {
                            authorsArrayList.add(authorsArray.optString(0));
                        }
                        newBook.setAuthors(authorsArrayList);
                    } else
                        newBook.setAuthors(null);

                    // popup
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddFragment.this.getContext());
                    builder.setTitle("Result");
                    builder.setMessage(result.getContents() + newBook.getTitle());
                    builder.setPositiveButton("OK", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }).show();

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
        newBook = new BookModel();

        AddViewModel addViewModel =
                new ViewModelProvider(this).get(AddViewModel.class);

        binding = FragmentAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView textView = binding.textNotifications;
        addViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        binding.textView2.setText(Integer.toString(addViewModel.getScore()));

        binding.addOneBtm.setOnClickListener(v -> {
            addViewModel.plusScore();
            binding.textView2.setText(Integer.toString(addViewModel.getScore()));
        });

        binding.subOneBtn.setOnClickListener(view -> {
            addViewModel.subScore();
            binding.textView2.setText(Integer.toString(addViewModel.getScore()));
            // Toast.makeText(getActivity().getApplicationContext(), newBook.getTitle(), Toast.LENGTH_SHORT).show();
        });

        binding.scanBtn.setOnClickListener(view -> {
            ScanOptions options = new ScanOptions();
            options.setBeepEnabled(false);
            options.setPrompt("Premi 'volume su' per accendere la torcia");
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);

            barLauncher.launch(options);
        });

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
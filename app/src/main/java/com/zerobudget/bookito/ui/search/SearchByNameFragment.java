package com.zerobudget.bookito.ui.search;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.databinding.FragmentSearchByNameBinding;
import com.zerobudget.bookito.models.search.SearchResultsModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class SearchByNameFragment extends SearchFragment {

    private FragmentSearchByNameBinding binding;
    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();

        binding = FragmentSearchByNameBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        viewBooks(new ArrayList<>(), binding.recycleViewSearch);

        binding.bookTextfield.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().trim().isEmpty()) {
                    binding.recycleViewSearch.setVisibility(View.VISIBLE);
                    searchAllBooks_UsrCity(editable.toString());
                } else {
                    //la nascondo se no da problemi di visualizzazione con i thread quando si cancella troppo velocemente
                    binding.recycleViewSearch.setVisibility(View.GONE);
                    viewBooks(new ArrayList<>(), binding.recycleViewSearch);
                }
            }
        });

        //ricarica la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
/*            binding.search.setQuery("", false); //clear the text
            binding.search.setIconified(true); //rimette la search view ad icona*/
            binding.bookTextfield.setText("");
            viewBooks(new ArrayList<>(), binding.recycleViewSearch);
            //svuota la recycle view
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }


    @Override
    protected void searchAllBooks_UsrCity(String param) {
        Task<QuerySnapshot> res = db.collection("users")
                .whereEqualTo("city", Utils.CURRENT_USER.getCity())
                .get();
        res.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arrBooks = document.get("books"); //array dei books
                        if (arrBooks != null) { //si assicura di cercare solo se esiste quache libro
                            addBooksToArray(document, arrBooks, arrResults, param);

                        }
                    }
                }
                Collections.sort(arrResults);

                if (arrResults.isEmpty())
                    searchAllBooks_UsrTownship(param);
                else
                    searchAllBooks_OthersCityorTownship(arrResults, param, false);
                //viewBooks(arrResults);
            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
            }

        });
    }


    @Override
    protected void searchAllBooks_UsrTownship(String param) {
        Task<QuerySnapshot> res = db.collection("users")
                .whereEqualTo("township", Utils.CURRENT_USER.getTownship())
                .get();
        res.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arrBooks = document.get("books"); //array dei books
                        if (arrBooks != null) { //si assicura di cercare solo se esiste quache libro
                            addBooksToArray(document, arrBooks, arrResults, param);
                        }
                    }
                }
                Collections.sort(arrResults);

                searchAllBooks_OthersCityorTownship(arrResults, param, true);

            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
            }

        });
    }


    @Override
    protected void searchAllBooks_OthersCityorTownship(ArrayList<SearchResultsModel> arrResults, String param, boolean isTownship) {
        Task<QuerySnapshot> res;
        if (isTownship) {
            res = db.collection("users")
                    .whereNotEqualTo("township", Utils.CURRENT_USER.getTownship())
                    .orderBy("township")
                    .orderBy("city")
                    .get();
        } else
            res = db.collection("users")
                    .whereNotEqualTo("city", Utils.CURRENT_USER.getCity())
                    .orderBy("city")
                    .orderBy("township")
                    .get();

        res.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<SearchResultsModel> arrResultsTmp = new ArrayList<>(); //libri trovati

                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arrBooks = document.get("books"); //array dei books
                        if (arrBooks != null) { //si assicura di cercare solo se esiste quache libro

                            addBooksToArray(document, arrBooks, arrResultsTmp, param);
                        }
                    }
                }
                //Collections.sort(arrResultsTmp);
                arrResults.addAll(arrResultsTmp);
                viewBooks(arrResults, binding.recycleViewSearch);
            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
            }

        });
    }
}
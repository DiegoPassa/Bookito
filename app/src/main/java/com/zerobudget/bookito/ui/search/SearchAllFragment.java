package com.zerobudget.bookito.ui.search;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentSearchAllBinding;
import com.zerobudget.bookito.models.search.SearchResultsModel;
import com.zerobudget.bookito.utils.CustomLinearLayoutManager;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class SearchAllFragment extends SearchFragment {

    private FragmentSearchAllBinding binding;
    private FirebaseFirestore db;
    ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

    private boolean showedAll = false;
    private ProgressBar progressBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();

        binding = FragmentSearchAllBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        progressBar = binding.progressBar;

        //to fix error E/Recyclerview: No Adapter Attached; Skipping Layout
        RecyclerView recyclerView = binding.recycleViewSearch;
        Search_RecycleViewAdapter adapter = new Search_RecycleViewAdapter(this.getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new CustomLinearLayoutManager(this.getContext()));
        //end of fixing it

        viewBooks(new ArrayList<>(), binding.recycleViewSearch);

        binding.btnSearch.setOnClickListener(view -> {
            showedAll = false;
            Navigation.findNavController(view).navigate(R.id.action_navigation_search_to_searchByNameFragment);
        });

        binding.btnSeeAllBooks.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            binding.linearLayoutChips.setVisibility(View.VISIBLE);
            showedAll = true;
            arrResults.clear();

            binding.chipTrade.setChecked(false);
            binding.chipShare.setChecked(false);
            binding.chipGift.setChecked(false);

            searchAllBooks_UsrCity("", true, true, true);
        });

        //ricarica la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);//svuota la recycle view
            if (showedAll) {
                checkAllChips();
            } else {
                arrResults.clear();
                viewBooks(new ArrayList<>(), binding.recycleViewSearch);
            }
        });

        binding.chipTrade.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());
        binding.chipGift.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());
        binding.chipShare.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());

        return root;
    }

    /**
     * controlla quali chips sono selezionati e sulla base di ciò cerca i libri in base al tipo selezionato
     *
     * se nessun chip è attivo cerca tutti i libri indiferentemente*/
    private void checkAllChips(){
        viewBooks(new ArrayList<>(), binding.recycleViewSearch);
        arrResults.clear();
        progressBar.setVisibility(View.VISIBLE);

        if(binding.chipTrade.isChecked()){
            searchAllBooks_UsrCity( "", true, false, false);
        }

        if(binding.chipShare.isChecked()){
            searchAllBooks_UsrCity( "", false, true, false);
        }

        if(binding.chipGift.isChecked()){
            searchAllBooks_UsrCity( "", false, false, true);
        }

        if(!binding.chipTrade.isChecked() && !binding.chipGift.isChecked() && !binding.chipShare.isChecked())
            searchAllBooks_UsrCity("", true, true, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    protected void searchAllBooks_UsrCity(String param, boolean isTrade, boolean isShare, boolean isGift) {
        Task<QuerySnapshot> res = db.collection("users")
                .whereEqualTo("city", Utils.CURRENT_USER.getCity())
                .get();

        res.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arrBooks = document.get("books"); //array dei books
                        if (arrBooks != null) //si assicura di cercare solo se esiste quache libro
                            addBooksToArray(document, arrBooks, arrResults, "", isTrade, isShare, isGift);
                    }
                }
                Collections.sort(arrResults);

                if (arrResults.isEmpty())
                    searchAllBooks_UsrTownship("", isTrade, isShare, isGift);
                else
                    searchAllBooks_OthersCityorTownship(arrResults, "", isTrade, isShare, isGift, false);

            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
            }

        });
    }

    @Override
    protected void searchAllBooks_UsrTownship(String param, boolean isTrade, boolean isShare, boolean isGift) {
        Task<QuerySnapshot> res = db.collection("users")
                .whereEqualTo("township", Utils.CURRENT_USER.getTownship())
                .get();

        res.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                //ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arrBooks = document.get("books"); //array dei books
                        if (arrBooks != null) //si assicura di cercare solo se esiste quache libro
                            addBooksToArray(document, arrBooks, arrResults, "", isTrade, isShare, isGift);
                    }
                }
                Collections.sort(arrResults);
                searchAllBooks_OthersCityorTownship(arrResults, "", isTrade, isShare, isGift, true);

            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
            }

        });
    }

    @Override
    protected void searchAllBooks_OthersCityorTownship(ArrayList<SearchResultsModel> arrResults, String param, boolean isTrade, boolean isShare, boolean isGift, boolean isTownship) {
        progressBar.setVisibility(View.GONE);

        Task<QuerySnapshot> res;

        /**se non ha trovato libri nella city del current user, prova a cercare i libri nella sua township
         * quindi dovrà cercare i libri restanti nelle township che non sono uguali alla sua*/
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
                        if (arrBooks != null) //si assicura di cercare solo se esiste quache libro
                            addBooksToArray(document, arrBooks, arrResults, "", isTrade, isShare, isGift);
                    }
                }
                // Collections.sort(arrResultsTmp);
                arrResults.addAll(arrResultsTmp);
                RecyclerView recyclerView = binding.recycleViewSearch;
                if (recyclerView.getAdapter() != null)
                    viewBooks(arrResults, recyclerView);
            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
            }
        });
    }
}


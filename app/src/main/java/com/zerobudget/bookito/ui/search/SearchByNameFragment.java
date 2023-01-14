package com.zerobudget.bookito.ui.search;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;

import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentSearchByNameBinding;
import com.zerobudget.bookito.models.search.SearchResultsModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class SearchByNameFragment extends SearchFragment {

    private FragmentSearchByNameBinding binding;
    private FirebaseFirestore db;

    private SearchView searchToolBar;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();

        binding = FragmentSearchByNameBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        viewBooks(new ArrayList<>(), binding.recycleViewSearch);

        //ricarica la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            searchToolBar.clearFocus();
            searchToolBar.setQuery("", false);
            viewBooks(new ArrayList<>(), binding.recycleViewSearch);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = getActivity().findViewById(R.id.topAppBar);

        toolbar.setTitle("");
        Menu menu = toolbar.getMenu();
        menu.setGroupVisible(R.id.default_group, false);
        menu.setGroupVisible(R.id.search_group, true);

        MenuItem searchViewItem = menu.findItem(R.id.search_item);
        searchToolBar = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchToolBar.setQueryHint("cerca per titolo o autore");
        searchToolBar.setIconified(false);

        searchToolBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchToolBar.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!s.isEmpty()) {
                    binding.recycleViewSearch.setVisibility(View.VISIBLE);
                    searchAllBooks_UsrCity(s, true, true, true);
                } else {
                    //la nascondo se no da problemi di visualizzazione con i thread quando si cancella troppo velocemente
                    binding.recycleViewSearch.setVisibility(View.GONE);
                    viewBooks(new ArrayList<>(), binding.recycleViewSearch);
                }
                return false;
            }
        });
        // setHasOptionsMenu(true);
    }

    @Override
    protected void searchAllBooks_UsrCity(String param, boolean isTrade, boolean isShare, boolean isGift) {
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
                            addBooksToArray(document, arrBooks, arrResults, param, isTrade, isShare, isGift);

                        }
                    }
                }
                Collections.sort(arrResults);

                if (arrResults.isEmpty())
                    searchAllBooks_UsrTownship(param, isTrade, isShare, isGift);
                else
                    searchAllBooks_OthersCityorTownship(arrResults, param, isTrade, isShare, isGift, false);
                //viewBooks(arrResults);
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
                ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arrBooks = document.get("books"); //array dei books
                        if (arrBooks != null) { //si assicura di cercare solo se esiste quache libro
                            addBooksToArray(document, arrBooks, arrResults, param, isTrade, isShare, isGift);
                        }
                    }
                }
                Collections.sort(arrResults);

                searchAllBooks_OthersCityorTownship(arrResults, param, isTrade, isShare, isGift, true);

            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
            }

        });
    }


    @Override
    protected void searchAllBooks_OthersCityorTownship(ArrayList<SearchResultsModel> arrResults, String param, boolean isTrade, boolean isShare, boolean isGift, boolean isTownship) {
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

                            addBooksToArray(document, arrBooks, arrResultsTmp, param, isTrade, isShare, isGift);
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
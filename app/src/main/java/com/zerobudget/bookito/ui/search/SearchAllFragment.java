package com.zerobudget.bookito.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentSearchAllBinding;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.search.SearchResultsModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SearchAllFragment extends SearchFragment {

    private FragmentSearchAllBinding binding;
    private FirebaseFirestore db;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        //end of fixing it

        viewBooks(new ArrayList<>(), binding.recycleViewSearch);

        binding.btnSearch.setOnClickListener(view -> {
            Navigation.findNavController(view).navigate(R.id.action_navigation_search_to_searchByNameFragment);
        });

        binding.btnSeeAllBooks.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            showedAll = true;
            searchAllBooks_UsrCity("");
        });

        //ricarica la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);//svuota la recycle view
            if (showedAll) {
                searchAllBooks_UsrCity("");
            } else {
                viewBooks(new ArrayList<>(), binding.recycleViewSearch);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    protected void searchAllBooks_UsrCity(String param) {
        db.collection("users")
                .whereEqualTo("city", Utils.CURRENT_USER.getCity())
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

                        for (DocumentSnapshot document : task.getResult()) {
                            if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                                Object arr = document.get("books"); //array dei books
                                if (arr != null) { //si assicura di cercare solo se esiste quache libro
                                    for (Object o : (ArrayList<Object>) arr) {
                                        HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                                        if ((boolean) map.get("status")) {
                                            BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
                                            SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, document.toObject(UserModel.class));
                                            arrResults.add(searchResultsModel);
                                        }
                                    }
                                }
                            }
                        }
                        Collections.sort(arrResults);
                        searchAllBooks_OthersCities(arrResults, "");
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }

                });
    }

    @Override
    protected void searchAllBooks_OthersCities(ArrayList<SearchResultsModel> arrResults, String param) {
        progressBar.setVisibility(View.GONE);
        db.collection("users")
                .whereNotEqualTo("city", Utils.CURRENT_USER.getCity())
                .orderBy("city")
                .orderBy("township")
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<SearchResultsModel> arrResultsTmp = new ArrayList<>(); //libri trovati
                        for (DocumentSnapshot document : task.getResult()) {
                            if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                                Object arr = document.get("books"); //array dei books
                                if (arr != null) { //si assicura di cercare solo se esiste quache libro

                                    for (Object o : (ArrayList<Object>) arr) {
                                        HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                                        if ((boolean) map.get("status")) {
                                            BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
                                            SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, document.toObject(UserModel.class));
                                            arrResultsTmp.add(searchResultsModel);
                                        }
                                    }
                                }
                            }
                        }
                        // Collections.sort(arrResultsTmp);
                        arrResults.addAll(arrResultsTmp);
                        viewBooks(arrResults, binding.recycleViewSearch);
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });
    }
}


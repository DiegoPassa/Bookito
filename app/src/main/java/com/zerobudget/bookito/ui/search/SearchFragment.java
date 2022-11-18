package com.zerobudget.bookito.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentSearchBinding;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private FirebaseFirestore db;

    private boolean showedAll = false;

    //TODO: cercare nel quartire del current user
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewBooks(new ArrayList<>());

        binding.btnSearch.setOnClickListener(view -> {
            Navigation.findNavController(view).navigate(R.id.action_navigation_search_to_searchByNameFragment);
        });

        binding.btnSeeAllBooks.setOnClickListener(view -> {
            showedAll  = true;
            searchAllBooks_UsrNeighborhood();
        });

        //ricarica la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);//svuota la recycle view
            if (showedAll) {
                searchAllBooks_UsrNeighborhood();
            } else {
                viewBooks(new ArrayList<>());
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void searchAllBooks_UsrNeighborhood(){
        db.collection("users").whereEqualTo("neighborhood", UserModel.getCurrentUser().getNeighborhood()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arr = document.get("books"); //array dei books
                        if (arr != null) { //si assicura di cercare solo se esiste quache libro
                            for (Object o : (ArrayList<Object>) arr) {
                                HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                                    BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"));
                                    SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, UserModel.getUserFromDocument(document));
                                    arrResults.add(searchResultsModel);
                            }
                        }
                    }
                }
                Collections.sort(arrResults);
                searchAllBooks_OthersNeighborhood(arrResults);
                //viewBooks(arrResults);
            } else {
                Log.d("TAG", "Error getting documents: ", task.getException());
            }

        });
    }


    private void searchAllBooks_OthersNeighborhood(ArrayList<SearchResultsModel> arrResults) {
        db.collection("users").whereNotEqualTo("neighborhood", UserModel.getCurrentUser().getNeighborhood()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<SearchResultsModel> arrResultsTmp = new ArrayList<>(); //libri trovati
                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arr = document.get("books"); //array dei books
                        if (arr != null) { //si assicura di cercare solo se esiste quache libro

                            for (Object o : (ArrayList<Object>) arr) {
                                HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                                BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"));
                                SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, UserModel.getUserFromDocument(document));
                                arrResultsTmp.add(searchResultsModel);
                            }
                        }
                    }
                }
                Collections.sort(arrResultsTmp);
                arrResults.addAll(arrResultsTmp);
                viewBooks(arrResults);
            } else {
                Log.d("TAG", "Error getting documents: ", task.getException());
            }
        });
    }

    protected void viewBooks(ArrayList<SearchResultsModel> arr) {
        if(getView() != null) { //evita il crash dell'applicazione
            RecyclerView recyclerView = binding.recycleViewSearch;

            Search_RecycleViewAdapter adapter = new Search_RecycleViewAdapter(this.getContext(), arr);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }
    }
}


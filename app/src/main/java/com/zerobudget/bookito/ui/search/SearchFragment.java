package com.zerobudget.bookito.ui.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.databinding.FragmentSearchBinding;
import com.zerobudget.bookito.ui.library.BookModel;
import com.zerobudget.bookito.ui.users.UserModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SearchViewModel homeViewModel =
                new ViewModelProvider(this).get(SearchViewModel.class);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        binding = FragmentSearchBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // final TextView textView = binding.textHome;
        // homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        //barra di ricerca, cerca alla pressione del tasto invio
        binding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchBookByTitle(s);
                return true;
            }
//.
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    //ricerca libro per titolo
    protected void searchBookByTitle(String t) {
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

                    for (DocumentSnapshot document : task.getResult()) {
                        //TODO: sostituire l'id con l'id del current user
                        if (!document.getId().equals("AZLYEN9WqTOVXiglkPJT")) { //deve cercare i libri degli altri utenti
                            Object arr = document.get("books"); //array dei books
                            if (arr != null) { //si assicura di cercare solo se esiste quache libro
                                Iterator<Object> iterator = ((ArrayList<Object>) arr).iterator(); //cast ad array list per avere l'iteratore

                                int i = 0;//contatore
                                while (iterator.hasNext()) {
                                    Object o = ((ArrayList<Object>) arr).get(i); //cast ad array list per prendere il libro i
                                    HashMap<Object, Object> map = (HashMap<Object, Object>) o; // cast per prendere i dati del libro i

                                    //ricerca per titolo
                                    if (Objects.requireNonNull(map.get("title")).toString().contains(t)) {
                                        Log.d("Title", "" + map.get("title"));

                                        BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"));
                                        SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, UserModel.getUserFromDocument(document));
                                        arrResults.add(searchResultsModel);
                                    }
                                    iterator.next();
                                    i++;
                                }
                            }
                        }
                    }

/*                    for (int i = 0; i < arrBkFound.size(); i++) {
                        Log.d("ArrBKMFound:", arrBkFound.get(i).getTitle());

                    }*/

                    viewBooks(arrResults);
                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }

            }
        });


    }

    protected void viewBooks(ArrayList<SearchResultsModel> arr) {
        RecyclerView recyclerView = binding.recycleViewSearch;

        Search_RecycleViewAdapter adapter = new Search_RecycleViewAdapter(this.getContext(), arr);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }
}


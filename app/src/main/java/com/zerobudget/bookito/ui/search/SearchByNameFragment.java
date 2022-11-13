package com.zerobudget.bookito.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.databinding.FragmentSearchByNameBinding;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SearchByNameFragment extends Fragment {

    private FragmentSearchByNameBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    //TODO: cercare nel quartire del current user
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        binding = FragmentSearchByNameBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        viewBooks(new ArrayList<>());

        binding.bookTextfield.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            /* TODO: Se si cancella velocemente c'è un delay tra il thread di ricerca e la chiama della ricerca succesiva,
             *       questo porta a visualizzare dei risultati anche se l'input è vuoto (credo)
             */
            @Override
            public void afterTextChanged(Editable editable) {
                //controlla che la text field non sia vuota
                if (!TextUtils.isEmpty(binding.bookTextfield.getText().toString().trim()))
                    searchBookByTitle(editable.toString());
                else {
                    viewBooks(new ArrayList<>());
                }
            }
        });

        //ricarica la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
/*            binding.search.setQuery("", false); //clear the text
            binding.search.setIconified(true); //rimette la search view ad icona*/
            binding.bookTextfield.setText("");
            viewBooks(new ArrayList<>()); //svuota la recycle view
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    //ricerca libro per titolo
    private void searchBookByTitle(String searched_title) {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

                for (DocumentSnapshot document : task.getResult()) {
                    //TODO: sostituire l'id con l'id del current user
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arr = document.get("books"); //array dei books
                        if (arr != null) { //si assicura di cercare solo se esiste quache libro

                            for (Object o : (ArrayList<Object>) arr) {
                                HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                                if (Objects.requireNonNull(map.get("title")).toString().contains(searched_title)) {
                                    Log.d("Title", "" + map.get("title"));
                                    BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"));
                                    SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, UserModel.getUserFromDocument(document));
                                    arrResults.add(searchResultsModel);
                                }
                            }
                        }
                    }
                }

                viewBooks(arrResults);
            } else {
                Log.d("TAG", "Error getting documents: ", task.getException());
            }

        });
    }

    protected void viewBooks(ArrayList<SearchResultsModel> arr) {
        if (getView() != null) { //evita il crash dell'applicazione
            RecyclerView recyclerView = binding.recycleViewSearch;

            Search_RecycleViewAdapter adapter = new Search_RecycleViewAdapter(this.getContext(), arr);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }
    }

}
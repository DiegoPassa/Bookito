package com.zerobudget.bookito.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.databinding.FragmentSearchByNameBinding;
import com.zerobudget.bookito.models.search.SearchResultsModel;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class SearchByNameFragment extends Fragment {

    private FragmentSearchByNameBinding binding;
    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        db = FirebaseFirestore.getInstance();

        binding = FragmentSearchByNameBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        viewBooks(new ArrayList<>());

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
                    searchBookByTitle_UsrNeighborhood(editable.toString());
                } else {
                    //la nascondo se no da problemi di visualizzazione con i thread quando si cancella troppo velocemente
                    binding.recycleViewSearch.setVisibility(View.GONE);
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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    /**
     * ricerca libro per titolo nel quartiere dell'utente
     */
    private void searchBookByTitle_UsrNeighborhood(String searched_book) {
        db.collection("users")
                .whereEqualTo("township", Utils.CURRENT_USER.getTownship())
                .orderBy("city")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arr = document.get("books"); //array dei books
                        if (arr != null) { //si assicura di cercare solo se esiste quache libro

                            for (Object o : (ArrayList<Object>) arr) {
                                HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                                //converte in lower case per non avere problemi di non corrispondenza tra maiuscole e minuscole
                                if ((Objects.requireNonNull(map.get("title")).toString().toLowerCase(Locale.ROOT).contains(searched_book.toLowerCase(Locale.ROOT)))
                                        || (Objects.requireNonNull(map.get("author")).toString().toLowerCase(Locale.ROOT).contains(searched_book.toLowerCase(Locale.ROOT)))) {
                                    //Log.d("Title", "" + map.get("title"));
                                    BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
                                    SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, document.toObject(UserModel.class));
                                    arrResults.add(searchResultsModel);
                                }
                            }
                        }
                    }
                }
                //Collections.sort(arrResults);
                searchBookByTitle_OthersNeighborhood(searched_book, arrResults);
                //viewBooks(arrResults);
            } else {
                Log.d("TAG", "Error getting documents: ", task.getException());
            }

        });
    }

    /**
     * ricerca del libro per titolo anche negli altri quartieri*/
    private void searchBookByTitle_OthersNeighborhood(String searched_book, ArrayList<SearchResultsModel> arrResults) {
        db.collection("users")
                .whereNotEqualTo("township", Utils.CURRENT_USER.getTownship())
                .orderBy("township")
                .orderBy("city")
                .get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<SearchResultsModel> arrResultsTmp = new ArrayList<>(); //libri trovati

                for (DocumentSnapshot document : task.getResult()) {
                    if (!document.getId().equals(Utils.USER_ID)) { //deve cercare i libri degli altri utenti
                        Object arr = document.get("books"); //array dei books
                        if (arr != null) { //si assicura di cercare solo se esiste quache libro

                            for (Object o : (ArrayList<Object>) arr) {
                                HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                                //converte in lower case per non avere problemi di non corrispondenza tra maiuscole e minuscole
                                if ((boolean) map.get("status"))
                                    if ((Objects.requireNonNull(map.get("title")).toString().toLowerCase(Locale.ROOT).contains(searched_book.toLowerCase(Locale.ROOT)))
                                            || (Objects.requireNonNull(map.get("author")).toString().toLowerCase(Locale.ROOT).contains(searched_book.toLowerCase(Locale.ROOT)))) {
                                        //Log.d("Title", "" + map.get("title"));
                                        BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
                                        SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, document.toObject(UserModel.class));
                                        arrResultsTmp.add(searchResultsModel);
                                    }
                            }
                        }
                    }
                }
                //Collections.sort(arrResultsTmp);
                arrResults.addAll(arrResultsTmp);
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
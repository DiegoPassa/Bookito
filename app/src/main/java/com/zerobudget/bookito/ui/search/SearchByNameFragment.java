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
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.search.SearchResultsModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

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
                    Log.d("EDITABLE", editable.toString());
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
                                        //converte in lower case per non avere problemi di non corrispondenza tra maiuscole e minuscole
                                        if ((map.get("title").toString().toLowerCase(Locale.ROOT).contains(param.toLowerCase(Locale.ROOT)))
                                                || (map.get("author").toString().toLowerCase(Locale.ROOT).contains(param.toLowerCase(Locale.ROOT)))) {
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
                        searchAllBooks_OthersCities(arrResults, param);
                        //viewBooks(arrResults);
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }

                });
    }

    @Override
    protected void searchAllBooks_OthersCities(ArrayList<SearchResultsModel> arrResults, String param) {
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
                                        //converte in lower case per non avere problemi di non corrispondenza tra maiuscole e minuscole
                                        if ((boolean) map.get("status"))
                                            if ((map.get("title").toString().toLowerCase(Locale.ROOT).contains(param.toLowerCase(Locale.ROOT)))
                                                    || (map.get("author").toString().toLowerCase(Locale.ROOT).contains(param.toLowerCase(Locale.ROOT)))) {
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
                        viewBooks(arrResults, binding.recycleViewSearch);
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }

                });
    }
}
package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.databinding.FragmentTradeBookBinding;
import com.zerobudget.bookito.models.Requests.RequestTradeModel;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.ui.search.SearchResultsModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class BookTradeFragment extends Fragment {

    private FragmentTradeBookBinding binding;
    private RequestTradeModel requestTradeModel;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTradeBookBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        assert args != null;
        String str = args.getString("BK");
        requestTradeModel = Utils.getGsonParser().fromJson(str, RequestTradeModel.class);

        Log.d("REQ", requestTradeModel.getSender());

        db = FirebaseFirestore.getInstance();

        setUpBookModel();

        return root;
    }


    //TODO: non permettere di selezionare un libo se questo è già occupato in un'altra richiesta credo
    private void setUpBookModel() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati

                for (DocumentSnapshot document : task.getResult()) {
                    if (document.getId().equals(requestTradeModel.getSender())) { //deve cercare i libri di chi ha fatto la richiesta
                        Object arr = document.get("books"); //array dei books
                        if (arr != null) { //si assicura di cercare solo se esiste quache libro
                            for (Object o : (ArrayList<Object>) arr) {
                                HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                                if (map.get("type").equals("Scambio")) {
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
            RecyclerView recyclerView = binding.recycleViewMyLibrary;

            BookTrade_RecycleViewAdapter adapter = new BookTrade_RecycleViewAdapter(this.getContext(), arr, requestTradeModel);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        }
    }
}
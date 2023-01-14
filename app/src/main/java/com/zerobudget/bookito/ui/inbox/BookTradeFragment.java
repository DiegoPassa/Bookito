package com.zerobudget.bookito.ui.inbox;

import static android.content.ContentValues.TAG;

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
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.requests.RequestTradeModel;
import com.zerobudget.bookito.models.search.SearchResultsModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class BookTradeFragment extends Fragment {

    private FragmentTradeBookBinding binding;
    private RequestTradeModel requestTradeModel;
    private FirebaseFirestore db;

    //libri di scambio del sender, che non siano già in una richiesta di scambio accettata
    private ArrayList<String> senderTradedBooks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTradeBookBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        senderTradedBooks = new ArrayList<>();

        Bundle args = getArguments();
        assert args != null;
        String str = args.getString("BK");
        requestTradeModel = Utils.getGsonParser().fromJson(str, RequestTradeModel.class);

        db = FirebaseFirestore.getInstance();

        //ricarica la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);//svuota la recycle view
            setUpBookModel();
        });

        setUpBookModel();
        return root;
    }


    /**
     * visualizza i libri per concludere lo scambio
     */
    private void setUpBookModel() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<SearchResultsModel> arrResults = new ArrayList<>(); //libri trovati
                for (DocumentSnapshot document : task.getResult()) {
                    //deve cercare i libri di chi ha fatto la richiesta
                    if (document.getId().equals(requestTradeModel.getSender())) {
                        Object arr = document.get("books"); //array dei books
                        if (arr != null) { //si assicura di cercare solo se esiste quache libro
                            for (Object o : (ArrayList<Object>) arr) {
                                HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                                if ((boolean) map.get("status"))
                                    //mostra solo i libri disponibili, quindi con status true
                                    if (map.get("type").equals("Scambio")) {
                                        BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
                                        SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, document.toObject(UserModel.class));
                                        arrResults.add(searchResultsModel);
                                    }
                            }
                        }
                    }
                }

                if (arrResults.size() > 0) {
                    binding.noBooksFound.setVisibility(View.GONE);
                    viewBooks(arrResults);
                } else {
                    //nessun libro disponibile
                    binding.noBooksFound.setVisibility(View.VISIBLE);
                }
            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
            }
        });
    }

    /**
     * permette la visualizzazione dei libri sulla pagina
     *
     * @param arr: array list con i risultati da visualizzare sulla pagina
     */
    protected void viewBooks(ArrayList<SearchResultsModel> arr) {
        if (getView() != null) { //evita il crash dell'applicazione
            RecyclerView recyclerView = binding.recycleViewMyLibrary;

            BookTrade_RecycleViewAdapter adapter = new BookTrade_RecycleViewAdapter(this.getContext(), arr, requestTradeModel);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        }
    }
}
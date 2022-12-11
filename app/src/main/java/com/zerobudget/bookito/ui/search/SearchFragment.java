package com.zerobudget.bookito.ui.search;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zerobudget.bookito.models.search.SearchResultsModel;

import java.util.ArrayList;

abstract class SearchFragment extends Fragment {

    /**
     * ricerca dei libri degli altri utenti nel quartiere dell'utente*/
    abstract void searchAllBooks_UsrCity(String param);

    /**
     * ricerca dei libri nel comune dell'utente, usata nel caso non ce ne fossero nel quartiere*/
    abstract void searchAllBooks_UsrTownship(String param);

    /**
     * ricerca dei libri degli altri utenti negli altri quartieri ordinati per città*/
    abstract void searchAllBooks_OthersCities(ArrayList<SearchResultsModel> arrResults, String param);

    /**
     * permette la visualizzazione dei libri
     */
    protected void viewBooks(ArrayList<SearchResultsModel> arr, RecyclerView recyclerView){
        if (getView() != null) { //evita il crash dell'applicazione

            Search_RecycleViewAdapter adapter = new Search_RecycleViewAdapter(this.getContext(), arr);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }
    };
}

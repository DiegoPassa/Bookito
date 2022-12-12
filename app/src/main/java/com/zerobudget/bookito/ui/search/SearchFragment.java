package com.zerobudget.bookito.ui.search;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zerobudget.bookito.models.search.SearchResultsModel;

import java.util.ArrayList;

abstract class SearchFragment extends Fragment {

    /**
     * ricerca dei libri degli altri utenti nel quartiere dell'utente
     *
     * @param param:stringa contente la parole chiave cercata*/
    abstract void searchAllBooks_UsrCity(String param);

    /**
     * ricerca dei libri nel comune dell'utente, usata nel caso non ce ne fossero nel quartiere
     *
     * @param param:stringa contente la parole chiave cercata*/
    abstract void searchAllBooks_UsrTownship(String param);

    /**
     * ricerca dei libri degli altri utenti negli altri quartieri ordinati per città
     *
     * @param arrResults: array con i risultati dei libri trovati nei metodi precedenti
     * @param param: stringa contente la parole chiave cercata
     * @param isTownship: true se il precedente metodo ha effettuato la ricerca nella township dell'utente
     * */
    abstract void searchAllBooks_OthersCityorTownship(ArrayList<SearchResultsModel> arrResults, String param, boolean isTownship);

    /**
     * permette la visualizzazione dei libri
     *
     * @param arr: arraylist di SearchResultsModel, contiene i risultati della ricerca
     * @param recyclerView: recycleView da riempire con i risultati ottenuti
     */
    protected void viewBooks(ArrayList<SearchResultsModel> arr, RecyclerView recyclerView){
        if (getView() != null) { //evita il crash dell'applicazione

            Search_RecycleViewAdapter adapter = new Search_RecycleViewAdapter(this.getContext(), arr);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }
    };
}

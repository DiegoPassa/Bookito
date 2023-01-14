package com.zerobudget.bookito.ui.search;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.search.SearchResultsModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.CustomLinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

abstract class SearchFragment extends Fragment {

    /**
     * ricerca dei libri degli altri utenti nel quartiere dell'utente
     *
     * @param param:stringa contente la parole chiave cercata
     */
    abstract void searchAllBooks_UsrCity(String param, boolean isTrade, boolean isShare, boolean isGift);

    /**
     * ricerca dei libri nel comune dell'utente, usata nel caso non ce ne fossero nel quartiere
     *
     * @param param:stringa contente la parole chiave cercata
     */
    abstract void searchAllBooks_UsrTownship(String param, boolean isTrade, boolean isShare, boolean isGift);

    /**
     * ricerca dei libri degli altri utenti negli altri quartieri ordinati per città
     *
     * @param arrResults: array con i risultati dei libri trovati nei metodi precedenti
     * @param param:      stringa contente la parole chiave cercata
     * @param isTownship: true se il precedente metodo ha effettuato la ricerca nella township dell'utente
     */
    abstract void searchAllBooks_OthersCityorTownship(ArrayList<SearchResultsModel> arrResults, String param, boolean isTrade, boolean isShare, boolean isGift, boolean isTownship);

    /**
     * aggiunge i libri trovati all'array
     *
     * @param doc:        documento corrente di firebase sul quale si stanno cercando i libri
     * @param arrBooks:   i libri prelevati dal documento
     * @param param:      stringa contenente il testo cercato
     * @param arrResults: array nel quale inserire i libri trovati
     */
    protected void addBooksToArray(DocumentSnapshot doc, Object arrBooks, ArrayList<SearchResultsModel> arrResults, String param, boolean isTrade, boolean isShare, boolean isGift) {
        for (Object o : (ArrayList<Object>) arrBooks) {
            HashMap<Object, Object> map = (HashMap<Object, Object>) o;

            if ((boolean) map.get("status")) {
                if ((map.get("title").toString().toLowerCase(Locale.ROOT).contains(param.toLowerCase(Locale.ROOT)))
                        || (map.get("author").toString().toLowerCase(Locale.ROOT).contains(param.toLowerCase(Locale.ROOT)))) {
                    BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));

                    if (isTrade && tmp.getType().equals("Scambio")) {
                        SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, doc.toObject(UserModel.class));
                        arrResults.add(searchResultsModel);
                    }

                    if (isShare && tmp.getType().equals("Prestito")) {
                        SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, doc.toObject(UserModel.class));
                        arrResults.add(searchResultsModel);
                    }

                    if (isGift && tmp.getType().equals("Regalo")) {
                        SearchResultsModel searchResultsModel = new SearchResultsModel(tmp, doc.toObject(UserModel.class));
                        arrResults.add(searchResultsModel);
                    }
                }
            }
        }
    }

    /**
     * permette la visualizzazione dei libri
     *
     * @param arr:          arraylist di SearchResultsModel, contiene i risultati della ricerca
     * @param recyclerView: recycleView da riempire con i risultati ottenuti
     */
    protected void viewBooks(ArrayList<SearchResultsModel> arr, RecyclerView recyclerView) {
        if (getView() != null) { //evita il crash dell'applicazione

            Search_RecycleViewAdapter adapter = new Search_RecycleViewAdapter(this.getContext(), arr);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new CustomLinearLayoutManager(this.getContext()));
        }
    }
}

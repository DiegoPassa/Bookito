package com.zerobudget.bookito.ui.library;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentLibraryBinding;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private ArrayList<BookModel> bookModels;

    private FirebaseFirestore db;

    private ProgressBar spinner;

    /**
     * preleva i libri dell'utente corrente dal database*/
    public void setUpBookModel(){
        bookModels = new ArrayList<>();
        //TODO: in attesa dell'autenticazione dell'utente qusto resta commentato
        //if (currentUser != null) {
        //   String id = currentUser.getUid();
        spinner.setVisibility(View.VISIBLE);
        db.collection("users").document(Utils.USER_ID).get()
                .addOnCompleteListener(task -> {
                    Log.d("QUERY", "queryyy");
                    if (task.isSuccessful()) {
                        spinner.setVisibility(View.GONE);

                        ArrayList<BookModel> arrBkm = new ArrayList<>();

                        Object arr = task.getResult().get("books"); //array dei books
                        if(arr != null) { //si assicura di cercare solo se esiste quache libro
                            for (Object o : (ArrayList<Object>) arr) {
                                HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                                BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"));
                                arrBkm.add(tmp);//aggiunge il bookmodel tmp all'array list
                            }


                            addBooksOnLibrary(arrBkm); //visualizza il libro nella libreria

                        }

                    }else{
                        Log.d("ERR", "error");
                    }
                });
        //}
    }

    /**
     * visualizza i libri dell'utente corrente nella libreria virtuale*/
     protected void addBooksOnLibrary(ArrayList<BookModel> arr){
         if(getView() != null) { //evita il crash dell'applicazione
             RecyclerView recyclerView = binding.recycleViewMyLibrary;

             Book_RecycleViewAdapter adapter = new Book_RecycleViewAdapter(this.getContext(), arr);

             recyclerView.setAdapter(adapter);
             recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
         }
     }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // LibraryViewModel libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinner = root.findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        setUpBookModel();
        //visulizzazione spostata in addBookOnLibrary()

        binding.floatingActionButton.setOnClickListener(view -> Navigation.findNavController(view).navigate(R.id.action_navigation_library_to_navigation_insertNew));

        //permette di ricaricare la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            setUpBookModel();
        });


        binding.recycleViewMyLibrary.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 10 && binding.floatingActionButton.isShown()) {
                    binding.floatingActionButton.hide();
                }

                if (dy < -10 && !binding.floatingActionButton.isShown()) {
                    binding.floatingActionButton.show();
               }

                if (!binding.recycleViewMyLibrary.canScrollVertically(-1)) {
                    binding.floatingActionButton.show();
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
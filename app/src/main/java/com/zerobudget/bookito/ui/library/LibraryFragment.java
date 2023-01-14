package com.zerobudget.bookito.ui.library;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentLibraryBinding;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private FirebaseFirestore db;
    private Book_RecycleViewAdapter adapter;
    private final Rect scrollBounds = new Rect();
    private ProgressBar spinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recycleViewMyLibrary;
        spinner = binding.progressBar;

        if (!binding.addBookButton.getLocalVisibleRect(scrollBounds))
            binding.floatingActionButton.setVisibility(View.VISIBLE);

        binding.nestedScrollView2.getHitRect(scrollBounds);

        db = FirebaseFirestore.getInstance();
        adapter = new Book_RecycleViewAdapter(this.getContext(), (ArrayList<BookModel>) Utils.CURRENT_USER.getBooks(), binding.emptyLibrary);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        // getLibraryRealtime();
        getBooksFromDB();

        binding.floatingActionButton.setOnClickListener(view -> Navigation.findNavController(view).navigate(R.id.action_navigation_library_to_navigation_insertNew));
        binding.addBookButton.setOnClickListener(view -> Navigation.findNavController(view).navigate(R.id.action_navigation_library_to_navigation_insertNew));

        //permette di ricaricare la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            // getLibraryRealtime();
            getBooksFromDB();
        });

        binding.nestedScrollView2.setOnScrollChangeListener((View.OnScrollChangeListener) (view, i, i1, i2, i3) -> {
            if (i1 > i3) {
                binding.floatingActionButton.hide();
            } else {
                if (!binding.addBookButton.getLocalVisibleRect(scrollBounds))
                    binding.floatingActionButton.show();
            }
        });

        return root;
    }

    /**
     * preleva i liri dell'utente corrente dal database
     */
    public void getBooksFromDB() {
        db.collection("users").document(Utils.USER_ID)
                .get().addOnSuccessListener(documentSnapshot -> {
                    loadLibrary(documentSnapshot.get("books"));
                });
    }

    /**
     * carica la libreria dell'utente
     *
     * @param books: i libri prelevati dal database
     */
    private void loadLibrary(Object books) {
        spinner.setVisibility(View.VISIBLE);
        Utils.CURRENT_USER.getBooks().clear();
        for (Object o : (ArrayList<Object>) books) {
            HashMap<String, Object> map = (HashMap<String, Object>) o;
            BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
            Utils.CURRENT_USER.getBooks().add(tmp);//aggiunge il bookmodel tmp all'array list
        }
        spinner.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();

        Utils.toggleEmptyWarning(binding.emptyLibrary, Utils.CURRENT_USER.getBooks().size());
        if (!binding.addBookButton.getLocalVisibleRect(scrollBounds)) {
            binding.floatingActionButton.show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
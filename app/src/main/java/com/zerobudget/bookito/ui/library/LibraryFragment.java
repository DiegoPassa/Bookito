package com.zerobudget.bookito.ui.library;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
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

    ArrayList<BookModel> bookModels = new ArrayList<>();
    private FragmentLibraryBinding binding;
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private Book_RecycleViewAdapter adapter;

    private ProgressBar spinner;

    public void getLibraryRealtime() {
        db.collection("users").document(Utils.USER_ID)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "onEvent: ", error);
                        return;
                    }
                    if (value != null) {
                        Log.d(TAG, "onEvent: " + value.get("books"));
                        loadLibrary(value.get("books"));
                    } else {
                        Log.e(TAG, "onEvent: NULL");
                    }
                });
    }

    private void loadLibrary(Object books) {
        spinner.setVisibility(View.VISIBLE);
        bookModels.clear();
        for (Object o : (ArrayList<Object>) books) {
            HashMap<String, Object> map = (HashMap<String, Object>) o;
            BookModel tmp = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
            bookModels.add(tmp);//aggiunge il bookmodel tmp all'array list
        }
        spinner.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // LibraryViewModel libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recycleViewMyLibrary;

        spinner = root.findViewById(R.id.progressBar);

        db = FirebaseFirestore.getInstance();

        // setUpBookModel();
        adapter = new Book_RecycleViewAdapter(this.getContext(), bookModels);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));

        getLibraryRealtime();

        binding.floatingActionButton.setOnClickListener(view -> Navigation.findNavController(view).navigate(R.id.action_navigation_library_to_navigation_insertNew));

        //permette di ricaricare la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            getLibraryRealtime();
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
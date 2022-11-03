package com.zerobudget.bookito.ui.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zerobudget.bookito.databinding.FragmentLibraryBinding;

import java.util.ArrayList;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;

    private ArrayList<BookModel> bookModels;

    public void setUpBookModel(){
        bookModels = new ArrayList<>();
        bookModels.add(new BookModel("0", "Geronimo Stilton", null));
        bookModels.add(new BookModel("0", "Title", null));
        bookModels.add(new BookModel("0", "Falce", null));
        bookModels.add(new BookModel("0", "Geronimo Stilton", null));
        bookModels.add(new BookModel("0", "Title", null));
        bookModels.add(new BookModel("0", "Falce", null));
        bookModels.add(new BookModel("0", "Geronimo Stilton", null));
        bookModels.add(new BookModel("0", "Title", null));
        bookModels.add(new BookModel("0", "Falce", null));

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // LibraryViewModel libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setUpBookModel();
        RecyclerView recyclerView = binding.recycleViewMyLibrary;

        Book_RecycleViewAdapter adapter = new Book_RecycleViewAdapter(this.getContext(), bookModels);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
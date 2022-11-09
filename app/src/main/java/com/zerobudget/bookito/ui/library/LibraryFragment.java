package com.zerobudget.bookito.ui.library;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentLibraryBinding;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class LibraryFragment extends Fragment {

    private FragmentLibraryBinding binding;
    private ArrayList<BookModel> bookModels;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public void setUpBookModel(){
        bookModels = new ArrayList<>();
        //TODO: in attesa dell'autenticazione dell'utente qusto resta commentato
        //if (currentUser != null) {
        //   String id = currentUser.getUid();
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(Utils.USER_ID).get()
                .addOnCompleteListener(task -> {
                    Log.d("QUERY", "queryyy");
                    if (task.isSuccessful()) {
                        binding.progressBar.setVisibility(View.GONE);
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
                        Log.d("err", "error");
                    }
                });
        //}
    }

     protected void addBooksOnLibrary(ArrayList<BookModel> arr){
        RecyclerView recyclerView = binding.recycleViewMyLibrary;

        Book_RecycleViewAdapter adapter = new Book_RecycleViewAdapter(this.getContext(), arr);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
     }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // LibraryViewModel libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();


        setUpBookModel();
        //visulizzazione spostata in addBookOnLibrary()

        binding.floatingActionButton.setOnClickListener(view -> Navigation.findNavController(view).navigate(R.id.action_navigation_library_to_navigation_insertNew));



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
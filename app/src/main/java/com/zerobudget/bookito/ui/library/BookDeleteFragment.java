package com.zerobudget.bookito.ui.library;

import static com.google.firebase.firestore.FieldValue.arrayRemove;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.databinding.FragmentDeleteBookBinding;
import com.zerobudget.bookito.ui.users.UserModel;
import com.zerobudget.bookito.utils.Utils;


public class BookDeleteFragment extends Fragment {

    private FragmentDeleteBookBinding binding;
    private BookModel bookSelected;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDeleteBookBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        String str = args.getString("BK");
        bookSelected= Utils.getGsonParser().fromJson(str, BookModel.class);

        //TODO: in attesa dell'autenticazione dell'utente qusto resta commentato
        //if (currentUser != null) {
        //   String id = currentUser.getUid();

        binding.bookTitle.setText(bookSelected.getTitle());
        binding.bookAuthor.setText(bookSelected.getAuthor());
        binding.bookDescription.setText(bookSelected.getDescription());
        binding.bookDescription.setMovementMethod(new ScrollingMovementMethod());
        Picasso.get().load(bookSelected.getThumbnail()).into(binding.bookThumbnail);

        binding.btnDelete.setOnClickListener(view -> {
            //TODO: query per eliminare il bookSelected
                });

        //}

        return root;
    }
}

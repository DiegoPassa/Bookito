package com.zerobudget.bookito.ui.add;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentConfirmAddBinding;
import com.zerobudget.bookito.ui.library.BookModel;
import com.zerobudget.bookito.ui.users.UserModel;
import com.zerobudget.bookito.utils.Utils;


public class AddConfirmFragment extends Fragment {

    private FragmentConfirmAddBinding binding;
    protected BookModel newBook;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentConfirmAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        String str = args.getString("BK");
        newBook= Utils.getGsonParser().fromJson(str, BookModel.class);

        binding.bookTitle.setText(newBook.getTitle());
        binding.bookAuthor.setText(newBook.getAuthor());
        binding.bookDescription.setText(newBook.getDescription());
        Picasso.get().load(newBook.getThumbnail()).into(binding.bookThumbnail);

        Log.d("BKCONF", newBook.getTitle());

        binding.btnConfirm.setOnClickListener(view -> {
            addBook(); //aggiunge il libro al database
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle("Result");
            builder.setMessage("Libro inserito correttamente");
            builder.setPositiveButton("OK",  (dialogInterface, i) -> {
                dialogInterface.dismiss();
                Navigation.findNavController(view).navigate(R.id.to_navigation_library);
            }).show();
        });

        binding.btnCancel.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle("Result");
            builder.setMessage("Inserimento annullato");
            builder.setPositiveButton("OK",  (dialogInterface, i) -> {
                dialogInterface.dismiss();
                Navigation.findNavController(view).navigate(R.id.to_navigation_library);
            }).show();
        });

        return root;
    }


    protected void addBook() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //TODO: in attesa dell'autenticazione dell'utente qusto resta commentato
        //if (currentUser != null) {
        //   String id = currentUser.getUid();

        db.collection("users").document("AZLYEN9WqTOVXiglkPJT")
                .update("books", FieldValue.arrayUnion(newBook.serialize())).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        UserModel.getCurrentUser().appendBook(newBook);
                });
        // }
    }
}

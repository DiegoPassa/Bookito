package com.zerobudget.bookito.ui.library;

import static com.google.firebase.firestore.FieldValue.arrayRemove;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentDeleteBookBinding;
import com.zerobudget.bookito.ui.users.UserModel;
import com.zerobudget.bookito.utils.Utils;


public class BookDeleteFragment extends Fragment {

    private FragmentDeleteBookBinding binding;
    private BookModel bookSelected;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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
            //rimuove il libro selezionato
            db.collection("users").document(Utils.USER_ID).update("books", FieldValue.arrayRemove(bookSelected));
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle("Eliminazione");
            builder.setMessage("Il libro "+bookSelected.getTitle()+" è stato eliminato correttamente");
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                Navigation.findNavController(view).navigate(R.id.to_navigation_library);
            }).show();
        });

        //}

        return root;
    }
}

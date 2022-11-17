package com.zerobudget.bookito.ui.library;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentDeleteBookBinding;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.List;


public class BookDeleteFragment extends Fragment {

    private FragmentDeleteBookBinding binding;
    private BookModel bookSelected;

    private FirebaseFirestore db;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();

        binding = FragmentDeleteBookBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        String str = args.getString("BK");
        bookSelected = Utils.getGsonParser().fromJson(str, BookModel.class);

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

            //rimuove la richiesta relativa a quel libro se esiste!
            db.collection("requests").whereEqualTo("receiver", Utils.USER_ID).whereEqualTo("requestedBook", bookSelected.getIsbn()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    for (DocumentSnapshot document : documents) {
                        DocumentReference documentReference = document.getReference();
                        documentReference.delete();
                    }
                }
            });


            AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this.getContext());
            builder.setTitle("Eliminazione");
            builder.setMessage("Il libro " + bookSelected.getTitle() + " è stato eliminato correttamente");
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                Navigation.findNavController(view).navigate(R.id.to_navigation_library);
            }).show();
        });

        //}

        return root;
    }
}

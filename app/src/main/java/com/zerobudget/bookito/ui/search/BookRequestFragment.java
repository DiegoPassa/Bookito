package com.zerobudget.bookito.ui.search;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentRequestBookBinding;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.utils.Utils;

public class BookRequestFragment extends Fragment {

    private FragmentRequestBookBinding binding;
    private SearchResultsModel usrBookSelected;

    private FirebaseFirestore db;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();

        binding = FragmentRequestBookBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        assert args != null;
        String str = args.getString("USR_BK");
        usrBookSelected = Utils.getGsonParser().fromJson(str, SearchResultsModel.class);

        binding.bookTitle.setText(usrBookSelected.getBook().getTitle());
        binding.bookAuthor.setText(usrBookSelected.getBook().getAuthor());
        binding.bookDescription.setText(usrBookSelected.getBook().getDescription());
        binding.bookDescription.setMovementMethod(new ScrollingMovementMethod());
        String owner = usrBookSelected.getUser().getFirstName() + " " + usrBookSelected.getUser().getLastName();
        binding.bookOwner.setText(owner);
        binding.bookType.setText(usrBookSelected.getBook().getType());
        Picasso.get().load(usrBookSelected.getBook().getThumbnail()).into(binding.bookThumbnail);


        switch (usrBookSelected.getBook().getType()) {
            case "Scambio":
                binding.bookmarkOutline.setColorFilter(getContext().getColor(R.color.bookmark_outline_scambio), PorterDuff.Mode.SRC_ATOP);
                binding.bookmark.setColorFilter(getContext().getColor(R.color.bookmark_scambio), PorterDuff.Mode.SRC_ATOP);
                break;
            case "Prestito":
                binding.bookmarkOutline.setColorFilter(getContext().getColor(R.color.bookmark_outline_prestito), PorterDuff.Mode.SRC_ATOP);
                binding.bookmark.setColorFilter(getContext().getColor(R.color.bookmark_prestito), PorterDuff.Mode.SRC_ATOP);
                break;
            case "Regalo":
                binding.bookmarkOutline.setColorFilter(getContext().getColor(R.color.bookmark_outine_regalo), PorterDuff.Mode.SRC_ATOP);
                binding.bookmark.setColorFilter(getContext().getColor(R.color.bookmark_regalo), PorterDuff.Mode.SRC_ATOP);
                break;
            default:
                Picasso.get().load(R.drawable.bookmark_template).into(binding.bookmark);
                break;
        }

        binding.btnRequest.setOnClickListener(view -> {

            //TODO: in attesa dell'autenticazione dell'utente qusto resta commentato, cambiare anche id nei set sotto
            //if (currentUser != null) {
            //   String id = currentUser.getUid();

            //preleva l'id dell'utente dal database
            db.collection("users").get().addOnCompleteListener(task -> {
                RequestModel rm = new RequestModel();
                rm.setRequestedBook(usrBookSelected.getBook().getIsbn());
                rm.setTitle(usrBookSelected.getBook().getTitle());
                rm.setThumbnail(usrBookSelected.getBook().getThumbnail());
                rm.setStatus("undefined");
                rm.setType(usrBookSelected.getBook().getType());
                rm.setSender(Utils.USER_ID);

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        if (doc.get("telephone") == null) Log.d("ERRORE", "SUPREMOOOO");
                        if (doc.get("telephone").equals(usrBookSelected.getUser().getTelephone())) {
                            rm.setReceiver(doc.getId());
                            Log.d("REC", rm.getReceiver());
                            requestBook(rm, view); //prova a inserire la richiesta del libro

                        }
                    }
                }
            });
        });
        //}
        return root;
    }

    private boolean checkRequests(QueryDocumentSnapshot doc, RequestModel rm) {
        boolean err = false;

        if (doc.get("receiver").equals(rm.getReceiver())
                && doc.get("requestedBook").equals(rm.getRequestedBook())
                && doc.get("sender").equals(rm.getSender())
                && doc.get("thumbnail").equals(rm.getThumbnail())
                && doc.get("title").equals(rm.getTitle())
                && doc.get("type").equals(rm.getType()))
            err = true;

        return err;
    }

    private void requestBook(RequestModel rm, View view) {

        db.collection("requests").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                boolean err = false;

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    //TODO: aggiungere un flag nel libro per impedire la visualizzazione nelle ricerche se esiste già una richiesta
                    //controlla se esiste già una richiesta uguale, non posso usare serialize di request model perchè ho lo status che varia
                    if (checkRequests(doc, rm))
                        err = true;
                }
                //se esiste già una richiesta da errore
                if (err) {
                    AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this.getContext());
                    builder.setTitle("Richiesta Rifiutata");
                    builder.setMessage("La richiesta esiste già!");
                    builder.setPositiveButton("OK", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }).show();
                } else {
                    db.collection("requests").add(rm.serialize()).addOnSuccessListener(documentReference -> {
                        Log.d("OKK", documentReference.getId());
                    }).addOnFailureListener(e -> Log.w("ERROR", "Error adding document", e));

                    AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this.getContext());
                    builder.setTitle("Richiesta effettuata");
                    builder.setMessage("La richiesta è andata a buon fine");
                    builder.setPositiveButton("OK", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }).show();
                }

                Navigation.findNavController(view).navigate(R.id.navigation_search);
            } else {
                Log.d("ERR", "Error getting documents: ", task.getException());
            }
        });
        //}

    }
}
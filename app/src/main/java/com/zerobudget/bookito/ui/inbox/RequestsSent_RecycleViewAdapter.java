package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class RequestsSent_RecycleViewAdapter extends RequestsReceived_RecycleViewAdapter {
    private final StorageReference storageRef;
    private boolean isUndefined;


    public RequestsSent_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests, TextView empty) {
        super(ctx, requests, empty);
        this.isUndefined = false;

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel otherModel = requests.get(holder.getAdapterPosition()).getOtherUser();
        String idReceiver = requests.get(holder.getAdapterPosition()).getReceiver();

        if (otherModel != null) {
            String other_user = "A: " + requests.get(holder.getAdapterPosition())
                    .getOtherUser()
                    .getFirstName() + " " + requests.get(holder.getAdapterPosition())
                    .getOtherUser()
                    .getLastName();

            holder.user_name.setText(other_user);
        } else holder.user_name.setText("undefined");

        holder.title.setText(requests.get(holder.getAdapterPosition()).getTitle());
        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(holder.book_image);


        if (requests.get(holder.getAdapterPosition()).getOtherUser().isHasPicture()) {
            holder.user_gravatar.setVisibility(View.GONE);
            //scorre le immagini e cerca solo quella dell'utente relativo alla richiesta
            storageRef.child("profile_pics/").listAll().addOnSuccessListener(listResult -> {
                for (StorageReference item : listResult.getItems()) {
                    // All the items under listRef.
                    if (!item.getName().equals(Utils.USER_ID) && item.getName().equals(idReceiver)) {
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            Picasso.get().load(uri).into(holder.usr_pic);
                            holder.usr_pic.setVisibility(View.VISIBLE);
                            //holder.user_gravatar.setVisibility(View.GONE);

                        }).addOnFailureListener(exception -> {
                            int code = ((StorageException) exception).getErrorCode();
                            if (code == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                holder.user_gravatar.setHash(requests.get(holder.getAdapterPosition()).getOtherUser().getTelephone().hashCode());
                                holder.user_gravatar.setVisibility(View.VISIBLE);
                                holder.usr_pic.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });
        } else {
            holder.user_gravatar.setHash(requests.get(holder.getAdapterPosition()).getOtherUser().getTelephone().hashCode());
            holder.user_gravatar.setVisibility(View.VISIBLE);
            holder.usr_pic.setVisibility(View.GONE);
        }

        setupIconType(holder, requests.get(position).getType());

        holder.request_selected.setOnClickListener(view -> {
            if (otherModel != null && holder.getAdapterPosition() != -1) {
                createNewContactDialog(holder, null);
            }
        });
    }

    @Override
    public void createNewContactDialog(ViewHolder holder, Flag flag) {
        checkIfStillUndefined(requests.get(holder.getAdapterPosition()));

        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(context);
        View view = View.inflate(context, R.layout.popup, null);

        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();

        loadPopupViewMembers(view);

        noteText.setText(requests.get(holder.getAdapterPosition()).getNote());

        String requestTypeStr = "Richiesta " + requests.get(holder.getAdapterPosition()).getType();
        titlePopup.setText(requestTypeStr);
        String firstAndLastNameStr = requests.get(holder.getAdapterPosition()).getOtherUser().getFirstName() + " " + requests.get(holder.getAdapterPosition()).getOtherUser().getLastName();
        owner.setText(firstAndLastNameStr);
        ownerLocation.setText(requests.get(holder.getAdapterPosition()).getOtherUser().getNeighborhood());
        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(thumbnail);
        refuseButton.setText("Annulla richiesta");
        confirmButton.setVisibility(View.GONE);

        refuseButton.setOnClickListener(view1 -> {
            if (isUndefined) {
                Log.d("UNDF", "true");
                if (holder.getAdapterPosition() != -1) {
                    dialog.dismiss();

                    AlertDialog.Builder newDialog = new MaterialAlertDialogBuilder(context);
                    newDialog.setTitle("Conferma cancellazione");
                    newDialog.setMessage(Html.fromHtml("Sei sicuro di voler <b>annullare</b> la richiesta per <b>" +
                                    requests.get(holder.getAdapterPosition()).getTitle() + "</b>?",
                            Html.FROM_HTML_MODE_LEGACY));
                    newDialog.setPositiveButton("SI", (dialogInterface, i) -> {
                        super.deleteRequest(requests.get(holder.getAdapterPosition()));
                        requests.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        Utils.toggleEmptyWarning(emptyWarning, Utils.EMPTY_SEND, requests.size());
                        dialogInterface.dismiss();
                        Toast.makeText(context, "Richiesta annullata correttamente!", Toast.LENGTH_LONG).show();
                    });

                    newDialog.setNegativeButton("NO", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    });

                    newDialog.show();
                }
            } else {
                dialog.dismiss();
                Toast.makeText(context, "Oh no, la richiesta è già stata accettata! Contatta l'utente in chat.", Toast.LENGTH_LONG).show();
                //TODO: reload page in the sent request tab
                //Navigation.findNavController(holder.itemView).navigate(R.id.request_page_nav);
            }
        });

        dialog.show();
    }

    private void checkIfStillUndefined(RequestModel r) {
        db.collection("requests").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult())
                    if (doc.getId().equals(r.getRequestId()))
                        isUndefined = doc.get("status").equals("undefined");
            }
        });
    }

}

package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.text.Html;
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
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.UserFlag;
import com.zerobudget.bookito.utils.Utils;
import com.zerobudget.bookito.utils.popups.PopupInbox;

import java.util.ArrayList;
import java.util.HashMap;

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
        RequestModel currentRequest = requests.get(position);

        UserModel otherModel = currentRequest.getOtherUser();
        String idReceiver = currentRequest.getReceiver();

        if (otherModel != null) {
            String other_user = "A: " + currentRequest
                    .getOtherUser()
                    .getFirstName() + " " + currentRequest
                    .getOtherUser()
                    .getLastName();

            holder.user_name.setText(other_user);
            holder.user_location.setText(context.getString(R.string.user_location, requests.get(position).getOtherUser().getTownship(), requests.get(position).getOtherUser().getCity()));
        } else holder.user_name.setText("undefined");

        holder.title.setText(currentRequest.getTitle());
        Picasso.get().load(currentRequest.getThumbnail()).into(holder.book_image);


        if (currentRequest.getOtherUser().isHasPicture()) {
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
                                holder.user_gravatar.setHash(currentRequest.getOtherUser().getTelephone().hashCode());
                                holder.user_gravatar.setVisibility(View.VISIBLE);
                                holder.usr_pic.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });
        } else {
            holder.user_gravatar.setHash(currentRequest.getOtherUser().getTelephone().hashCode());
            holder.user_gravatar.setVisibility(View.VISIBLE);
            holder.usr_pic.setVisibility(View.GONE);
        }

        //inserisce l'immagine del tipo del libro
        Utils.setUpIconBookType(requests.get(holder.getAdapterPosition()).getType(), holder.book_type);

        holder.request_selected.setOnClickListener(view -> {
            if (otherModel != null && holder.getAdapterPosition() != -1) {
                HashMap<String, Object> karma = otherModel.getKarma(); //HashMap<String, Long>
                Number points = (Number) karma.get("points");
                Number feedback_numbers = (Number) karma.get("numbers");
                Flag flag = UserFlag.getFlagFromUser(points, feedback_numbers);
                createNewContactDialog(holder, flag, currentRequest);
            }
        });
    }

    @Override
    public void createNewContactDialog(ViewHolder holder, Flag flag, RequestModel currentRequest) {
        checkIfStillUndefined(currentRequest);

        View view = View.inflate(context, R.layout.popup_inbox, null);

        PopupInbox dialogBuilder = new PopupInbox(context, view);
        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();

        dialogBuilder.setReputationMessage(currentRequest, flag);
        dialogBuilder.setUpInformation(currentRequest);

        dialogBuilder.setTextRefuseButton("Annulla richiesta");
        dialogBuilder.getConfirmButton().setVisibility(View.GONE);

        dialogBuilder.getRefuseButton().setOnClickListener(view1 -> {
            if (isUndefined) {
                if (holder.getAdapterPosition() != -1) {
                    dialog.dismiss();

                    AlertDialog.Builder newDialog = new MaterialAlertDialogBuilder(context);
                    newDialog.setTitle("Conferma cancellazione");
                    newDialog.setMessage(Html.fromHtml("Sei sicuro di voler <b>annullare</b> la richiesta per <b>" +
                                    currentRequest.getTitle() + "</b>?",
                            Html.FROM_HTML_MODE_LEGACY));
                    newDialog.setPositiveButton("SI", (dialogInterface, i) -> {
                        super.deleteRequest(currentRequest);
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

    /**
     * controlla che la richiesta sia ancora undefined
     *
     * @param r: richiesta di riferimento
     */
    private void checkIfStillUndefined(RequestModel r) {
        db.collection("requests").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult())
                    if (doc.getId().equals(r.getRequestId()))
                        isUndefined = doc.get("status").equals("undefined");
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }
}

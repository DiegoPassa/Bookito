package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.Requests.RequestShareModel;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RequestAccepted_RecycleViewAdapter extends Inbox_RecycleViewAdapter {
    private StorageReference storageRef;

    public RequestAccepted_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests, TextView empty) {
        super(ctx, requests, empty);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel otherUser = requests.get(holder.getAdapterPosition()).getOtherUser();
        String idSender = requests.get(holder.getAdapterPosition()).getSender();
        String idReceiver = requests.get(holder.getAdapterPosition()).getReceiver();

        Uri[] otherUserPic = new Uri[1];

        if (otherUser != null) {
            String nameOtherUser = otherUser.getFirstName();
            String surnameOtherUser = otherUser.getLastName();

            if (isCurrentUserReceiver(requests.get(holder.getAdapterPosition()))) {
                holder.user_name.setText(nameOtherUser + " " + surnameOtherUser);
            } else {
                holder.user_name.setText(Html.fromHtml("<b> ( TU ) -> </b>" + nameOtherUser + surnameOtherUser, Html.FROM_HTML_MODE_LEGACY));
            }
        }

        holder.title.setText(requests.get(holder.getAdapterPosition()).getTitle());
        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(holder.book_image);

        if(requests.get(holder.getAdapterPosition()).getOtherUser().isHasPicture()) {
            //holder.usr_pic.setVisibility(View.VISIBLE);
            holder.user_gravatar.setVisibility(View.GONE);
            storageRef.child("profile_pics/").listAll().addOnSuccessListener(listResult -> {
                for (StorageReference item : listResult.getItems()) {
                    // All the items under listRef.
                    if (!item.getName().equals(Utils.USER_ID) && (item.getName().equals(idReceiver)
                            || item.getName().equals(idSender))) {
                        //Log.d("item", item.getName());
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Utils.setUriPic(uri.toString());
                            //Log.d("PIC", Utils.URI_PIC);
                            otherUserPic[0] = uri;
                            Log.d("carico immaginme", ""+uri.getClass());
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
        }else{
            holder.user_gravatar.setHash(requests.get(holder.getAdapterPosition()).getOtherUser().getTelephone().hashCode());
            holder.user_gravatar.setVisibility(View.VISIBLE);
            holder.usr_pic.setVisibility(View.GONE);
        }

        setUpColorType(holder, requests.get(position).getType());

        holder.request_selected.setOnClickListener(view1 -> {
            if (otherUser != null && holder.getAdapterPosition() != -1) {
                Bundle args = new Bundle();
                String toJson = Utils.getGsonParser().toJson(requests.get(holder.getAdapterPosition()).getOtherUser());
                args.putString("otherChatUser", toJson);

                if (isCurrentUserReceiver(requests.get(holder.getAdapterPosition())))
                    args.putString("otherUserId", requests.get(holder.getAdapterPosition()).getSender());

                else args.putString("otherUserId", requests.get(holder.getAdapterPosition()).getReceiver());

                args.putString("requestID", requests.get(holder.getAdapterPosition()).getrequestId());
                args.putParcelable("otherUserPic", otherUserPic[0]);
                args.putString("receiverID",  requests.get(holder.getAdapterPosition()).getReceiver());
                Navigation.findNavController(holder.itemView).navigate(R.id.to_chat_fragment, args);

            }
        });

        holder.request_selected.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                showImagePicDialog(holder);
                return false;
            }
        });
    }

    private void showImagePicDialog(ViewHolder holder) {
        //String[] options = {"Scatta foto", "Seleziona da galleria", "Elimina foto"};

        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(context);
        View view = View.inflate(context, R.layout.popup_longclick_requests, null);

        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();

        TextView title = view.findViewById(R.id.title_popup);

        TextView infoRequest = view.findViewById(R.id.info_request);
        TextView feedback = view.findViewById(R.id.feedback);
        TextView reportUser = view.findViewById(R.id.report_user);
        TextView cancelRequest = view.findViewById(R.id.cancel_request);

        title.setText("Cosa vuoi fare?");

        infoRequest.setOnClickListener(view1 -> {
            createNewContactDialog(holder);
        });

        feedback.setOnClickListener(view1 -> {
            //TODO: fare le recensioni con le stelline
            Toast.makeText(context, "Funzionalità da implementare", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });


        cancelRequest.setOnClickListener(view1 -> {
            //TODO: visualizzare la pagina per mettere la recensione
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setTitle("Conferma");
            builder.setMessage(Html.fromHtml("Sei sicuro di voler annullare la richiesta di <br><b>" + requests.get(holder.getAdapterPosition()).getTitle() + "</b>?", Html.FROM_HTML_MODE_LEGACY));
            builder.setPositiveButton("SI", (dialogInterface, i) -> {
                //torna disponibile perchè è un prestito, altrimenti no
                if(requests.get(holder.getAdapterPosition()) instanceof RequestShareModel)
                    changeBookStatus(requests.get(holder.getAdapterPosition()).getRequestedBook());


                db.collection("requests").document(requests.get(holder.getAdapterPosition()).getrequestId()).delete();

                Toast.makeText(context, "Richiesta annullata!", Toast.LENGTH_LONG).show();

                dialogInterface.dismiss();
            }).setNegativeButton("NO",  (dialogInterface, i) -> {
                dialogInterface.dismiss();
            }).show();

            dialog.dismiss();
        });

        dialogBuilder.setView(view);
        dialog.show();
    }

    private void changeBookStatus(String bookRequested){
        db.collection("users").document(Utils.USER_ID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Object arr = task.getResult().get("books"); //array dei books
                if (arr != null) //si assicura di cercare solo se esiste quache libro
                    for (Object o : (ArrayList<Object>) arr) {
                        HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                        if(map.get("isbn").equals(bookRequested)){
                            BookModel oldBook =  new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
                            BookModel newBook =  new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), true);

                            //firebase non permette di modificare il valore, va rimosso l'elemento dell'array e inserito con i valori modificati
                            db.collection("users").document(Utils.USER_ID).update("books", FieldValue.arrayRemove(oldBook));
                            db.collection("users").document(Utils.USER_ID).update("books", FieldValue.arrayUnion(newBook));
                        }
                    }
            }
        });
    }

    public void createNewContactDialog(ViewHolder holder) {
        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(context);

        View view = View.inflate(context, R.layout.popup, null);

        dialogBuilder.setView(view);

        AlertDialog dialog = dialogBuilder.create();

        loadPopupViewMembers(view);
        String requestTypeStr = "Richiesta " + requests.get(holder.getAdapterPosition()).getType();
        titlePopup.setText(requestTypeStr);
        String firstAndLastNameStr = requests.get(holder.getAdapterPosition()).getOtherUser().getFirstName() + " " + requests.get(holder.getAdapterPosition()).getOtherUser().getLastName();
        owner.setText(firstAndLastNameStr);
        ownerLocation.setText(requests.get(holder.getAdapterPosition()).getOtherUser().getNeighborhood());


        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(thumbnail);

        if (requests.get(holder.getAdapterPosition()) instanceof RequestShareModel) {
            Date date = ((RequestShareModel) requests.get(holder.getAdapterPosition())).getDate();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String dateString = "Data di restituzione:\n"+sdf.format(date);

            returnDate.setText(dateString);
            returnDate.setVisibility(View.VISIBLE);
        }


        refuseButton.setText("OK, torna indietro");
        refuseButton.setOnClickListener(view1 -> {
            dialog.dismiss();
        });

        confirmButton.setVisibility(View.GONE);

        dialog.show();

    }
//    @Override
//    public void createNewContactDialog(int position, ViewHolder holder, Flag user) {
//        Bundle args = new Bundle();
//        String toJson = Utils.getGsonParser().toJson(requests.get(holder.getAdapterPosition()));
//        args.putString("otherChatUser", toJson);
//        Navigation.findNavController(View.inflate(context, R.id.recycleView_Inbox, null)).navigate(R.layout.chat_fragment);
//    }

    protected boolean isCurrentUserReceiver(RequestModel r) {
        return r.getReceiver().equals(Utils.USER_ID);
    }
}

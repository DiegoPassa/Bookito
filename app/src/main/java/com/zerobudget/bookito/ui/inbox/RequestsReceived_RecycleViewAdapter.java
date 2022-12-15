package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.lelloman.identicon.view.ClassicIdenticonView;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.Notifications;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.chat.MessageModelWithImage;
import com.zerobudget.bookito.models.notification.NotificationModel;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.requests.RequestShareModel;
import com.zerobudget.bookito.models.requests.RequestTradeModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.UserFlag;
import com.zerobudget.bookito.utils.Utils;
import com.zerobudget.bookito.utils.popups.PopupInbox;

import java.util.ArrayList;
import java.util.HashMap;

public class RequestsReceived_RecycleViewAdapter extends RecyclerView.Adapter<RequestsReceived_RecycleViewAdapter.ViewHolder> {

    protected final Context context;
    protected ArrayList<RequestModel> requests;
    private String isbn_trade;

    protected FirebaseFirestore db;
    protected FirebaseAuth auth;
    private final StorageReference storageRef;

    protected TextView emptyWarning;

    private boolean exists;

    public RequestsReceived_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests, TextView empty) {
        this.context = ctx;
        this.requests = requests;
        this.exists = false;
        emptyWarning = empty;

        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

    }

    @NonNull
    @Override
    public RequestsReceived_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recycleview_requests, parent, false);

        return new RequestsReceived_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel senderModel = requests.get(holder.getAdapterPosition()).getOtherUser();
        String idSender = requests.get(holder.getAdapterPosition()).getSender();

        if (senderModel != null) {
            String other_usr = "Da: " + requests.get(holder.getAdapterPosition()).getOtherUser().getFirstName() + " " + requests.get(holder.getAdapterPosition()).getOtherUser().getLastName();
            holder.user_name.setText(other_usr);
            holder.user_location.setText(context.getString(R.string.user_location, requests.get(position).getOtherUser().getTownship(), requests.get(position).getOtherUser().getCity()));
        } // else holder.user_name.setText("undefined");
        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(holder.book_image);
        holder.title.setText(requests.get(holder.getAdapterPosition()).getTitle());
        if (requests.get(holder.getAdapterPosition()).getOtherUser() != null) {

            Utils.setUpIconBookType(requests.get(holder.getAdapterPosition()).getType(), holder.book_type);

            if (requests.get(holder.getAdapterPosition()).getOtherUser().isHasPicture()) {
                holder.user_gravatar.setVisibility(View.GONE);
                storageRef.child("profile_pics/").listAll().addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        // All the items under listRef.
                        if (!item.getName().equals(Utils.USER_ID) && item.getName().equals(idSender)) {
                            item.getDownloadUrl().addOnSuccessListener(uri -> {
                                // Utils.setUriPic(uri.toString());

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
        }


        holder.request_selected.setOnClickListener(view -> {
            if (senderModel != null && holder.getAdapterPosition() != -1) {
                HashMap<String, Object> karma = senderModel.getKarma(); //HashMap<String, Long>
                Number points = (Number) karma.get("points");
                Number feedback_numbers = (Number) karma.get("numbers");
                Flag flag = UserFlag.getFlagFromUser(points, feedback_numbers);
                createNewContactDialog(holder, flag);

            }
        });
    }

    /**
     * crea il popup con le inforazioni relative alla richiesta
     * l'utente potrà accettare o rifiutare
     * in caso di scambio, prima di accettare l'utente corrente dovrà selezionare un libro dalla libreria dell'altro utente*/
    public void createNewContactDialog(ViewHolder holder, Flag flag) {
        //controlla se la richiesta esiste ancora
        checkIfStillExists(requests.get(holder.getAdapterPosition()));

        View view = View.inflate(context, R.layout.popup_inbox, null);
        //loadPopupViewMembers(view);

        //utilizza la classe popupInbox per generare dinamicamente i vari elementi del popup
        PopupInbox dialogBuilder = new PopupInbox(context, view);
        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();

        dialogBuilder.setUpInformation(requests.get(holder.getAdapterPosition()));
        dialogBuilder.setReputationMessage(requests.get(holder.getAdapterPosition()), flag);

        //se è un  prestito visualizza la data di restituzione
        if (requests.get(holder.getAdapterPosition()) instanceof RequestShareModel) {
            dialogBuilder.setUpDate((RequestShareModel) requests.get(holder.getAdapterPosition()));
        }

        //pulsante per visualizzare la libreria dell'altro utente, in caso di scambio
        if (requests.get(holder.getAdapterPosition()) instanceof RequestTradeModel) {
            dialogBuilder.setTextConfirmButton("Libreria Utente");
        }

        dialogBuilder.getConfirmButton().setOnClickListener(view1 -> {
            if (holder.getAdapterPosition() != -1) {
                if (exists) { //controlla che la richiesta esista ancora
                    if (requests.get(holder.getAdapterPosition()) instanceof RequestTradeModel) {

                        Bundle args = new Bundle();
                        String bookString = Utils.getGsonParser().toJson(requests.get(holder.getAdapterPosition()));
                        args.putString("BK", bookString);

                        checkIfTheBookIsAlreadyAcceptedSomewhere(requests.get(holder.getAdapterPosition()), holder, args);
                        //Navigation.findNavController(holder.itemView).navigate(R.id.action_request_page_nav_to_bookTradeFragment, args);
                    } else {
                        acceptRequest(requests.get(holder.getAdapterPosition()));
                    }
                } else {
                    Toast.makeText(context, "Oh no, la richiesta è stata eliminata dal richiedente!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(holder.itemView).navigate(R.id.request_page_nav);
                }
                // notifyItemRangeChanged(holder.getAdapterPosition(), requests.size());
            }
            Utils.toggleEmptyWarning(emptyWarning, Utils.EMPTY_INBOX, requests.size());
            dialog.dismiss();
        });

        dialogBuilder.getRefuseButton().setOnClickListener(view1 -> {
            if (holder.getAdapterPosition() != -1) {
                deleteRequest(requests.get(holder.getAdapterPosition()));
                // requests.remove(holder.getAdapterPosition());
                // notifyItemRemoved(holder.getAdapterPosition());
                // notifyItemRangeChanged(holder.getAdapterPosition(), requests.size());
            }
            Utils.toggleEmptyWarning(emptyWarning, Utils.EMPTY_INBOX, requests.size());
            dialog.dismiss();
        });
        dialog.show();

    }

    /**
     * elimina la richiesta in caso di rifiuto
     *
     * @param r: richiesta da eliminare*/
    protected void deleteRequest(RequestModel r) {
        db.collection("requests").document(r.getRequestId()).delete();
        sendNotification(r, "Reject");
    }

    /**
     * accetta la richiesta cambiando lo status del libro e della richiesta
     *
     * @param r: richiesta da accettare*/
    protected void acceptRequest(RequestModel r) {
        Utils.changeBookStatus(db, Utils.USER_ID, r.getRequestedBook(), false);

                //controlla prima che non esista già una richiesta accettata per il libro
        db.collection("requests")
                .whereEqualTo("receiver", Utils.USER_ID)
                .whereEqualTo("status", "accepted")
                .whereEqualTo("requestedBook", r.getRequestedBook())
                .get()
                .addOnCompleteListener(task -> {
                    boolean existsOther = task.getResult().size() > 0;

                    if (!existsOther) {
                        sendNotification(r, "Accept");
                        //l'update ha successo solo se trova il documento, avviso all'utente in caso di insuccesso
                        db.collection("requests").document(r.getRequestId()).update("status", "accepted").addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                //requests.remove(holder.getAdapterPosition());
                                //notifyItemRemoved(holder.getAdapterPosition());
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/chatapp/" + r.getRequestId());

                                ref.child("user1").setValue(r.getReceiver());
                                if (r.getReceiver().equals(Utils.USER_ID))
                                    ref.child("user2").setValue(r.getSender());
                                else ref.child("user2").setValue(Utils.USER_ID);

                                //messaggio di default per visualizzare il libro richiesto e per iniziare la conversazione
                                String messageTxt = "Ciao, ti contatto per il tuo libro in " + r.getType() + " dal titolo '" + r.getTitle() + "'!";
                                MessageModelWithImage defaultMsg = new MessageModelWithImage(r.getThumbnail(), r.getSender(), r.getReceiver(), messageTxt, "sent", Timestamp.now().getSeconds());
                                ref.push().setValue(defaultMsg);
                                Toast.makeText(context, "Richiesta accettata!", Toast.LENGTH_LONG).show();

                                Notifications.sendPushNotification(Utils.CURRENT_USER.getFirstName() + " ha accettato la tua richiesta per \"" + r.getTitle() + "\"", "Richiesta accettata!", r.getOtherUser().getNotificationToken());
                            } else
                                Toast.makeText(context, "Oh no, la richiesta è stata eliminata dal richiedente!", Toast.LENGTH_LONG).show();
                        });
                    } else
                        Toast.makeText(context, "Esiste già una richiesta accettata per il libro!\nAttendere o eliminare la richiesta.", Toast.LENGTH_LONG).show();
                });

    }

    /**
     * crea la notifica da visulizzare nell'area notifiche dell'applicazione
     *
     * @param r: richiesta di riferimento
     * @param status: stato della richiesta, se Accept è accettata, altrimenti è rifiutata*/
    private void sendNotification(RequestModel r, String status) {
        String otherUserId = r.getSender().equals(Utils.USER_ID) ? r.getReceiver() : r.getSender();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/notification/"+otherUserId);
        String body = status.equals("Accept") ? Utils.CURRENT_USER.getFirstName() + " ha accettato la tua richiesta!" : Utils.CURRENT_USER.getFirstName() + " ha rifiutato la tua richiesta!";
        String title = status.equals("Accept") ? "Richiesta accettata!" : "Richiesta rifiutata!";

        /*
        PROBLEMA, NON POSSIAMO RICHIAMARE LA SERIALIZE DEL CURRENT USER PERCHÉ SI TRATTA DI UN USERLIBRARY, QUINDI MI GENERA ANCHE TUTTI I SUOI LIBRI E QUINDI DA ERRORE
         */

        UserModel currentUser = new UserModel(Utils.CURRENT_USER.getFirstName(), Utils.CURRENT_USER.getLastName(),
                Utils.CURRENT_USER.getTelephone(), Utils.CURRENT_USER.getTownship(), Utils.CURRENT_USER.getCity(),
                Utils.CURRENT_USER.getKarma(), Utils.CURRENT_USER.isHasPicture(), Utils.CURRENT_USER.getNotificationToken());

        NotificationModel notificationModel = new NotificationModel(Utils.USER_ID, status, body, title, r.getThumbnail(), r, currentUser,Timestamp.now().getSeconds());
        ref.push().setValue(notificationModel.serialize());
    }

    /**
     * controlla se la richiesta esiste ancora (quindi se il sender non l'ha annullata)
     *
     * @param r: richiesta di riferimento*/
    private void checkIfStillExists(RequestModel r) {
        db.collection("requests").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult())
                    if (doc.getId().equals(r.getRequestId()))
                        exists = true;
            }
        });
    }

    /**
     * controlla se esiste già una richiesta accettata per quel libro dell'utente corrente da qualche parte*/
    private void checkIfTheBookIsAlreadyAcceptedSomewhere(RequestModel r, ViewHolder holder, Bundle args) {
        db.collection("requests").get().addOnCompleteListener(task -> {
            boolean existsOther = false;
            for (QueryDocumentSnapshot doc : task.getResult()) {
                //controllo se il receiver è il current user, perché mi interessa verificare se ci sono altre richieste per il suo libro
                //può essere che esista un altro utente con lo stesso libro ma non sono interessata a quello
                if(doc.get("receiver").equals(Utils.USER_ID)) {
                    if (doc.get("requestedBook").equals(r.getRequestedBook()) && doc.get("status").equals("accepted"))
                        existsOther = true;

                    if (doc.contains("requestTradeBook"))
                        if (doc.get("requestTradeBook").equals(r.getRequestedBook()))
                            existsOther = true;
                }
            }

            if (!existsOther)
                Navigation.findNavController(holder.itemView).navigate(R.id.action_request_page_nav_to_bookTradeFragment, args);
            else
                Toast.makeText(context, "Esiste già una richiesta accettata per il libro!\nAttendere o eliminare la richiesta.", Toast.LENGTH_LONG).show();

        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected final TextView title;
        protected final ConstraintLayout request_selected;
        protected final TextView user_name;
        protected final TextView user_location;
        protected final ImageView book_image;
        protected final ClassicIdenticonView user_gravatar;
        protected final ImageView usr_pic;
        protected final ImageView book_type;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.requestTitle);
            user_name = itemView.findViewById(R.id.requester_name);
            book_image = itemView.findViewById(R.id.book_image_request);
            request_selected = itemView.findViewById(R.id.request);
            user_gravatar = itemView.findViewById(R.id.user_gravatar);
            usr_pic = itemView.findViewById(R.id.profile_pic);
            book_type = itemView.findViewById(R.id.icon_type2);
            user_location = itemView.findViewById(R.id.user_location);
        }
    }


    public String getIsbn_trade() {
        return isbn_trade;
    }

    public void setIsbn_trade(String isbn_trade) {
        this.isbn_trade = isbn_trade;
    }
}

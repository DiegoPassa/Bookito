package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.lelloman.identicon.view.ClassicIdenticonView;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.requests.RequestShareModel;
import com.zerobudget.bookito.models.requests.RequestTradeModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.UserFlag;
import com.zerobudget.bookito.utils.Utils;
import com.zerobudget.bookito.utils.popups.PopupInbox;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RequestsAccepted_RecycleViewAdapter extends RecyclerView.Adapter<RequestsAccepted_RecycleViewAdapter.ViewHolder> {
    private final StorageReference storageRef;

    protected final Context context;
    protected ArrayList<RequestModel> requests;

    protected FirebaseFirestore db;
    protected FirebaseAuth auth;

    protected TextView emptyWarning;

    private boolean exists;

    private Uri[] otherUserPic = new Uri[1];

    public RequestsAccepted_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests, TextView empty) {
        this.context = ctx;
        this.requests = requests;
        this.exists = false;
        emptyWarning = empty;

        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public int getItemViewType(int position) {
        if (requests.get(position).getType().equals("Scambio")) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public RequestsAccepted_RecycleViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return new RequestsAccepted_RecycleViewAdapter.SwapViewHolder(LayoutInflater.from(context).inflate(R.layout.recycleview_requests_swap, parent, false));
            case 1:
                return new RequestsAccepted_RecycleViewAdapter.BorrowOrGiftViewHolder(LayoutInflater.from(context).inflate(R.layout.recycleview_requests_borrow, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel otherUser = requests.get(holder.getAdapterPosition()).getOtherUser();
        String idSender = requests.get(holder.getAdapterPosition()).getSender();
        String idReceiver = requests.get(holder.getAdapterPosition()).getReceiver();

        switch (holder.getItemViewType()) {
            case 0:
                // La richiesta è uno scambio
                SwapViewHolder swapHolder = (SwapViewHolder) holder;
                RequestTradeModel trade = (RequestTradeModel) requests.get(holder.getAdapterPosition());
                /*for (BookModel b : Utils.CURRENT_USER.getBooks()) {
                    if (b.getIsbn().equals(trade.getRequestTradeBook())) {
                        Picasso.get().load(b.getThumbnail()).into(swapHolder.book2_thumbnail);
                        break;
                    }
                }*/
                Picasso.get().load(trade.getThumbnailBookTrade()).into(swapHolder.book2_thumbnail);
                break;
            case 1:
                // La richiesta è un prestito oppure un regalo
                BorrowOrGiftViewHolder otherHolder = (BorrowOrGiftViewHolder) holder;
                otherHolder.book_title.setText(requests.get(holder.getAdapterPosition()).getTitle());
                if (requests.get(holder.getAdapterPosition()).getType().equals("Regalo")) {
                    Picasso.get().load(R.drawable.gift).into(otherHolder.type);
                    otherHolder.expire_date.setVisibility(View.GONE);
                } else {
                    Picasso.get().load(R.drawable.calendar).into(otherHolder.type);
                    RequestShareModel share = (RequestShareModel) requests.get(holder.getAdapterPosition());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALY);
                    otherHolder.expire_date.setText(sdf.format(share.getDate()));
                }
                break;
        }

        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(holder.book1_thumbnail);

        // Elementi in comune tra le due viste
        if (otherUser != null) {
            String nameOtherUser = otherUser.getFirstName();
            String surnameOtherUser = otherUser.getLastName();

            if (isCurrentUserReceiver(requests.get(holder.getAdapterPosition()))) {
                holder.user1_name.setText(R.string.you);
                holder.user2_name.setText(String.format("%s %s", nameOtherUser, surnameOtherUser));
                setUserPictures(Utils.CURRENT_USER, holder.user1_propic, holder.user1_gravatar, idReceiver);
                setUserPictures(otherUser, holder.user2_propic, holder.user2_gravatar, idSender);
            } else {
                holder.user1_name.setText(String.format("%s %s", nameOtherUser, surnameOtherUser));
                holder.user2_name.setText(R.string.you);
                setUserPictures(otherUser, holder.user1_propic, holder.user1_gravatar, idReceiver);
                setUserPictures(Utils.CURRENT_USER, holder.user2_propic, holder.user2_gravatar, idSender);
            }

            holder.card.setOnClickListener(view1 -> {
                if (holder.getAdapterPosition() != -1) {
                    Bundle args = new Bundle();
                    String toJson = Utils.getGsonParser().toJson(requests.get(holder.getAdapterPosition()).getOtherUser());
                    args.putString("otherChatUser", toJson);

                    //passo l'intera richiesta alla chat
                    String requestString = Utils.getGsonParser().toJson(requests.get(holder.getAdapterPosition()));
                    args.putString("requestModel", requestString);

                    args.putInt("position", holder.getAdapterPosition());
                    //nome della classe della richiesta
                    args.putString("requestClassName", requests.get(holder.getAdapterPosition()).getClass().getSimpleName());

                    if (isCurrentUserReceiver(requests.get(holder.getAdapterPosition())))
                        args.putString("otherUserId", requests.get(holder.getAdapterPosition()).getSender());
                    else
                        args.putString("otherUserId", requests.get(holder.getAdapterPosition()).getReceiver());

                    args.putString("requestID", requests.get(holder.getAdapterPosition()).getRequestId());
                    args.putParcelable("otherUserPic", otherUserPic[0]);
                    args.putString("receiverID", requests.get(holder.getAdapterPosition()).getReceiver());
                    Navigation.findNavController(holder.itemView).navigate(R.id.to_chat_fragment, args);

                }
            });


            holder.card.setOnLongClickListener(view -> {
                showActionsDialog(holder, requests.get(holder.getAdapterPosition()));
                return false;
            });

        }
    }

    private void setUserPictures(UserModel user, ImageView image, ClassicIdenticonView gravatar, String userRole) {
        if (user.isHasPicture()) {
            storageRef.child("profile_pics/").child(userRole).getDownloadUrl().addOnSuccessListener(uri -> {
                otherUserPic[0] = uri;
                Picasso.get().load(uri).into(image);
                image.setVisibility(View.VISIBLE);
            }).addOnFailureListener(exception -> {
                int code = ((StorageException) exception).getErrorCode();
                if (code == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    gravatar.setHash(user.getTelephone().hashCode());
                    gravatar.setVisibility(View.VISIBLE);
                }
            });
        } else {
            gravatar.setHash(user.getTelephone().hashCode());
            gravatar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * visualizza il popup con le opzioni disponibili
     *
     * @param holder: oggetto che contiene i binding dell'xml
     * @param request: richiesta selezionata
     */
    private void showActionsDialog(ViewHolder holder, RequestModel request) {
        //String[] options = {"Scatta foto", "Seleziona da galleria", "Elimina foto"};

        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(context);
        View view = View.inflate(context, R.layout.popup_longclick_requests, null);

        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();

        TextView title = view.findViewById(R.id.title_popup);

        TextView infoRequest = view.findViewById(R.id.info_request);
        //TextView feedback = view.findViewById(R.id.feedback);
        TextView closeRequest = view.findViewById(R.id.close_request);
        TextView cancelRequest = view.findViewById(R.id.cancel_request);
        TextView confirmBookGiven = view.findViewById(R.id.confirm_book_given);

        Log.d("CLASS", "" + requests.get(holder.getAdapterPosition()).getClass());


        if (request instanceof RequestShareModel) {
            if (request.getStatus().equals("ongoing")) {
                cancelRequest.setVisibility(View.GONE);
            } else {
                confirmBookGiven.setVisibility(View.VISIBLE);
                closeRequest.setVisibility(View.GONE);
            }
        }

        title.setText("Cosa vuoi fare?");

        //conferma riguardante il prestito del libro (se il proprietario ha dato il libro)
        confirmBookGiven.setOnClickListener(view1 -> {
            if (!Utils.USER_ID.equals(request.getReceiver()))
                return; //solo chi riceve il libro può confermare di averlo ricevuto
            /*
            da rivedere in ogni caso questo sistema, si potrebbe fare che serva una doppia conferma da parte di entrambi gli utenti
             */
            if (request.getStatus().equals("ongoing")) return;

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setTitle("Conferma");
            builder.setMessage(Html.fromHtml("Sei sicuro di voler confermare l'inizio del prestito di <br><b>" + request.getTitle() + "</b>", Html.FROM_HTML_MODE_LEGACY));
            builder.setPositiveButton("SI", (dialogInterface, i) -> {
                db.collection("requests").document(request.getRequestId()).update("status", "ongoing").addOnSuccessListener(task -> {
                    request.setStatus("ongoing");
                });
                dialogInterface.dismiss();
                dialog.dismiss();
            });
            builder.setNegativeButton("NO", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            });

            builder.show();

        });

        infoRequest.setOnClickListener(view1 -> {
            UserModel senderModel = requests.get(holder.getAdapterPosition()).getOtherUser();
            if (senderModel != null && holder.getAdapterPosition() != -1) {
                HashMap<String, Object> karma = senderModel.getKarma(); //HashMap<String, Long>
                Number points = (Number) karma.get("points");
                Number feedback_numbers = (Number) karma.get("numbers");
                Flag flag = UserFlag.getFlagFromUser(points, feedback_numbers);
                createNewContactDialog(holder, flag);

            }
            // createNewContactDialog(holder);
        });

        //richiesta (CONCLUDED) conclusa, dichiarata come finita da uno dei due utenti
        closeRequest.setOnClickListener(view1 -> {
            if (request instanceof RequestShareModel)
                if (!Utils.USER_ID.equals(request.getSender()))
                    return;  //è solo il sender che può confermare che la richiesta è satta effettivamente conclusa
                    /*
                    anche qua da rivedere, in questo caso chi presta il libro può confermare se l'altro utente glielo restituisce o meno.
                    Possiamo aggiungere una sorta di flag per far concludere la richiesta in caso l'utente non dovesse restituire il libro al suo proprietario
                     */
            dialog.dismiss();

            MaterialAlertDialogBuilder builderConfirm = new MaterialAlertDialogBuilder(context);
            builderConfirm.setTitle("Conferma");
            builderConfirm.setMessage(Html.fromHtml("Sei sicuro di voler segnare la richiesta di <br><b>" + requests.get(holder.getAdapterPosition()).getTitle() + "</b> come conlusa?", Html.FROM_HTML_MODE_LEGACY));
            builderConfirm.setPositiveButton("SI", (dialogInterface, i) -> {
                Date now = Timestamp.now().toDate();
                //richiesta di prestito, conttrolla che sia stata raggiunta la data di fine prestito
                if (request instanceof RequestShareModel && now.compareTo(((RequestShareModel) requests.get(holder.getAdapterPosition())).getDate()) < 0)
                    Toast.makeText(context, "Attenzione, il prestito non ha ancora superato la data prestabilita!", Toast.LENGTH_LONG).show();
                else {
                    //popup recensioni
                    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
                    View view2 = View.inflate(context, R.layout.popup_feedback, null);

                    TextView text = view2.findViewById(R.id.text);
                    String strTxt = "Dai una recensione da una a cinque stelle a " + request.getOtherUser().getFirstName() + " " + request.getOtherUser().getLastName() +
                            ".\nValuta correttamente, ricorda che il tuo voto influirà sulla sua reputazione.";
                    text.setText(strTxt);
                    builder.setView(view2);

                    AlertDialog starDialog = builder.create();
                    starDialog.show();

                    RatingBar mRatingBar = view2.findViewById(R.id.rating);
                    Button confirmFeedback = view2.findViewById(R.id.feedback_button);

                    mRatingBar.setOnRatingBarChangeListener((ratingBar, rating, isUser) -> {
                    });

                    confirmFeedback.setOnClickListener(click -> {

                        if (mRatingBar.getRating() <= 0) return;

                        String otherUserID = "";
                        if (request.getSender().equals(Utils.USER_ID)) {
                            otherUserID = request.getReceiver();
                        } else {
                            otherUserID = request.getSender();
                        }

                        sendFeedbackToUser(otherUserID, mRatingBar.getRating());
                        starDialog.dismiss();

                        closeRequest(holder);//chiude la richiesta in base al tipo

                        Toast.makeText(context, "Feedback inviato correttamente!", Toast.LENGTH_LONG).show();
                    });
                }
                dialogInterface.dismiss();
            }).setNegativeButton("NO", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            }).show();
        });

        //richiesta (CANCELLED) annullata mentre è ancora in corso
        cancelRequest.setOnClickListener(view1 -> {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setTitle("Conferma");
            builder.setMessage(Html.fromHtml("Sei sicuro di voler annullare la richiesta di <br><b>" + requests.get(holder.getAdapterPosition()).getTitle() + "</b>?", Html.FROM_HTML_MODE_LEGACY));
            builder.setPositiveButton("SI", (dialogInterface, i) -> {
                //TODO sistemare i permessi su firebase
                //se è uno scambio  devono tornare disponibili entrambi i libri
                if (requests.get(holder.getAdapterPosition()) instanceof RequestTradeModel) {
                    if (requests.get(holder.getAdapterPosition()).getReceiver().equals(Utils.USER_ID)) {
                        Utils.changeBookStatus(db, Utils.USER_ID, requests.get(holder.getAdapterPosition()).getRequestedBook());
                        Utils.changeBookStatus(db, requests.get(holder.getAdapterPosition()).getSender(), ((RequestTradeModel) requests.get(holder.getAdapterPosition())).getRequestTradeBook());
                    } else {
                        Utils.changeBookStatus(db, Utils.USER_ID, ((RequestTradeModel) requests.get(holder.getAdapterPosition())).getRequestTradeBook());
                        Utils.changeBookStatus(db, requests.get(holder.getAdapterPosition()).getReceiver(), requests.get(holder.getAdapterPosition()).getRequestedBook());
                    }

                } else {
                    Utils.changeBookStatus(db, requests.get(holder.getAdapterPosition()).getReceiver(), requests.get(holder.getAdapterPosition()).getRequestedBook());
                }

                db.collection("requests").document(requests.get(holder.getAdapterPosition()).getRequestId()).update("status", "cancelled");

                Toast.makeText(context, "Richiesta annullata!", Toast.LENGTH_LONG).show();
                requests.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());

                dialogInterface.dismiss();
            }).setNegativeButton("NO", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            }).show();

            dialog.dismiss();
        });

        dialogBuilder.setView(view);
        dialog.show();
    }

    /**
     * segna la richiesta come conclusa (status = concluded) sulla base del tipo
     * abilita il libro nella richiesta di prestito
     * elimina i libri nelle richieste di scambio e regalo
     *
     * @param holder: oggetto che contiene i binding all'xml
     */
    private void closeRequest(ViewHolder holder) {
        if (!(requests.get(holder.getAdapterPosition()) instanceof RequestShareModel)) {
            //la richiesta è segnata con status CONCLUDED
            db.collection("requests").document(requests.get(holder.getAdapterPosition()).getRequestId()).update("status", "concluded");

            if (requests.get(holder.getAdapterPosition()) instanceof RequestTradeModel) {
                //cancella i libri scambiati
                if (requests.get(holder.getAdapterPosition()).getReceiver().equals(Utils.USER_ID)) {
                    deleteUserBook(Utils.USER_ID, requests.get(holder.getAdapterPosition()).getRequestedBook());
                    deleteUserBook(requests.get(holder.getAdapterPosition()).getSender(), ((RequestTradeModel) requests.get(holder.getAdapterPosition())).getRequestTradeBook());
                } else {
                    deleteUserBook(Utils.USER_ID, ((RequestTradeModel) requests.get(holder.getAdapterPosition())).getRequestTradeBook());
                    deleteUserBook(requests.get(holder.getAdapterPosition()).getReceiver(), requests.get(holder.getAdapterPosition()).getRequestedBook());
                }
                Toast.makeText(context, "Scambio concluso, libro eliminato dalla libreria!", Toast.LENGTH_LONG).show();
            } else {
                //cancella il libro regalato
                deleteUserBook(requests.get(holder.getAdapterPosition()).getReceiver(), requests.get(holder.getAdapterPosition()).getRequestedBook());
                Toast.makeText(context, "Regalo concluso, libro eliminato dalla libreria!", Toast.LENGTH_LONG).show();
            }

            //riadattano gli item presenti sullo schermo
            requests.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
        } else {
            //richiesta di prestito
            /*Date now = Timestamp.now().toDate();

            if (now.compareTo(((RequestShareModel) requests.get(holder.getAdapterPosition())).getDate()) < 0)
                Toast.makeText(context, "Attenzione, il prestito non ha ancora superato la data prestabilita!", Toast.LENGTH_LONG).show();
            else {*/
            //cambia lo stato del libro
            Utils.changeBookStatus(db, Utils.USER_ID, requests.get(holder.getAdapterPosition()).getRequestedBook());
            //segna la richiesta come conlusa
            db.collection("requests").document(requests.get(holder.getAdapterPosition()).getRequestId()).update("status", "concluded");
            requests.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
            Toast.makeText(context, "Prestito concluso, il libro è nuovamente disponibile nella libreria!", Toast.LENGTH_LONG).show();
            // }
        }
        // dialog.dismiss();
    }

    /**
     * incrementa il punteggio di red flag dell'utente
     *
     * @param id: id dell'utente di riferimento
     * @param feedback: valore di incremento del feedback dell'utente
     */
    private void sendFeedbackToUser(String id, float feedback) {
        db.collection("users").document(id).update("karma.points", FieldValue.increment(feedback), "karma.numbers", FieldValue.increment(1));
    }

    /**
     * elimina un libro dalla libreria dell'utente, sulla base dell'isbn
     */
    void deleteUserBook(String userID, String bookRequested) {
        db.collection("users").document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Object arr = task.getResult().get("books"); //array dei books
                if (arr != null) //si assicura di cercare solo se esiste quache libro
                    for (Object o : (ArrayList<Object>) arr) {
                        HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                        if (map.get("isbn").equals(bookRequested)) {
                            db.collection("users").document(userID).update("books", FieldValue.arrayRemove(map));
                        }
                    }
            }
        });
    }

    /**
     * visualizza le informazioni relative alla richiesta selezionata
     */
    public void createNewContactDialog(ViewHolder holder, Flag flag) {
        View view = View.inflate(context, R.layout.popup, null);
        //crea il popup tramite la classe PopupInbox hce fornisce i metodi per settarne i valori
        PopupInbox dialogBuilder = new PopupInbox(context, view);
        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();

        dialogBuilder.setUpInformation(requests.get(holder.getAdapterPosition()));
        dialogBuilder.setReputationMessage(requests.get(holder.getAdapterPosition()), flag);

        if (requests.get(holder.getAdapterPosition()) instanceof RequestShareModel)
            dialogBuilder.setUpDate((RequestShareModel) requests.get(holder.getAdapterPosition()));

        dialogBuilder.setTextConfirmButton("OK, torna indietro");
        dialogBuilder.getConfirmButton().setOnClickListener(view1 -> dialog.dismiss());

        dialogBuilder.getRefuseButton().setVisibility(View.GONE);

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

    @Override
    public int getItemCount() {
        return requests.size();
    }

    // Vista con gli elementi in comune
    public static abstract class ViewHolder extends RecyclerView.ViewHolder {

        protected final ImageView book1_thumbnail;
        protected final TextView user1_name;
        protected final TextView user2_name;
        protected final ImageView user1_propic;
        protected final ImageView user2_propic;
        protected final ClassicIdenticonView user1_gravatar;
        protected final ClassicIdenticonView user2_gravatar;

        protected final CardView card;


        public ViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);

            book1_thumbnail = itemView.findViewById(R.id.book1_thumbnail);
            user1_name = itemView.findViewById(R.id.user1_name);
            user2_name = itemView.findViewById(R.id.user2_name);
            user1_propic = itemView.findViewById(R.id.user1_propic);
            user2_propic = itemView.findViewById(R.id.user2_propic);
            user1_gravatar = itemView.findViewById(R.id.user1_gravatar);
            user2_gravatar = itemView.findViewById(R.id.user2_gravatar);
            card = itemView.findViewById(R.id.card);
        }
    }

    // Vista per lo scambio
    public static class SwapViewHolder extends RequestsAccepted_RecycleViewAdapter.ViewHolder {

        protected final ImageView book2_thumbnail;

        public SwapViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            book2_thumbnail = itemView.findViewById(R.id.book2_thumbnail);
        }
    }

    // Vista per il prestito oppure regalo
    public static class BorrowOrGiftViewHolder extends RequestsAccepted_RecycleViewAdapter.ViewHolder {

        protected final TextView book_title;
        protected final ImageView type;
        protected final TextView expire_date;

        public BorrowOrGiftViewHolder(@androidx.annotation.NonNull View itemView) {
            super(itemView);
            book_title = itemView.findViewById(R.id.book_title);
            type = itemView.findViewById(R.id.type);
            expire_date = itemView.findViewById(R.id.expire_date);
        }
    }
}

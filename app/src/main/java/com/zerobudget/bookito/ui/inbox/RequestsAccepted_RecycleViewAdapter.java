package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.Requests.RequestShareModel;
import com.zerobudget.bookito.models.Requests.RequestTradeModel;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RequestsAccepted_RecycleViewAdapter extends RequestsReceived_RecycleViewAdapter {
    private final StorageReference storageRef;

    public RequestsAccepted_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests, TextView empty) {
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

        if (requests.get(holder.getAdapterPosition()).getOtherUser().isHasPicture()) {
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
                            Log.d("carico immaginme", "" + uri.getClass());
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

        holder.request_selected.setOnClickListener(view1 -> {
            if (otherUser != null && holder.getAdapterPosition() != -1) {
                Bundle args = new Bundle();
                String toJson = Utils.getGsonParser().toJson(requests.get(holder.getAdapterPosition()).getOtherUser());
                args.putString("otherChatUser", toJson);

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

        holder.request_selected.setOnLongClickListener(view -> {
            showActionsDialog(holder, requests.get(holder.getAdapterPosition()));
            return false;
        });
    }

    /**
     * visualizza il popup con le opzioni disponibili*/
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
            if (!Utils.USER_ID.equals(request.getReceiver())) return; //solo chi riceve il libro può confermare di averlo ricevuto
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
            createNewContactDialog(holder);
        });

        //richiesta (CONCLUDED) conclusa, dichiarata come finita da uno dei due utenti
        closeRequest.setOnClickListener(view1 -> {
            if (request instanceof RequestShareModel)
                if (!Utils.USER_ID.equals(request.getSender())) return;  //è solo il sender che può confermare che la richiesta è satta effettivamente conclusa
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
                        changeBookStatus(Utils.USER_ID, requests.get(holder.getAdapterPosition()).getRequestedBook());
                        changeBookStatus(requests.get(holder.getAdapterPosition()).getSender(), ((RequestTradeModel) requests.get(holder.getAdapterPosition())).getRequestTradeBook());
                    } else {
                        changeBookStatus(Utils.USER_ID, ((RequestTradeModel) requests.get(holder.getAdapterPosition())).getRequestTradeBook());
                        changeBookStatus(requests.get(holder.getAdapterPosition()).getReceiver(), requests.get(holder.getAdapterPosition()).getRequestedBook());
                    }

                } else {
                    changeBookStatus(requests.get(holder.getAdapterPosition()).getReceiver(), requests.get(holder.getAdapterPosition()).getRequestedBook());
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
     * elimina i libri nelle richieste di scambio e regalo*/
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
            changeBookStatus(Utils.USER_ID, requests.get(holder.getAdapterPosition()).getRequestedBook());
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
     * incrementa il punteggio di red flag dell'utente*/
    private void sendFeedbackToUser(String id, float feedback) {
        db.collection("users").document(id).update("karma.points", FieldValue.increment(feedback), "karma.numbers", FieldValue.increment(1));
    }

    /**
     * modifica lo stato di un libro (tramite isbn)
     * la modifica viene fatta rimuovendo il libro e inserendolo nuovamente con il nuovo stato
     * perché firebase non permette di modificare un valore all'interno della strutura dati in cui essi sono contenuti*/
    private void changeBookStatus(String userID, String isbn) {
        db.collection("users").document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Object arr = task.getResult().get("books"); //array dei books
                if (arr != null) //si assicura di cercare solo se esiste quache libro
                    for (Object o : (ArrayList<Object>) arr) {
                        HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                        if (map.get("isbn").equals(isbn)) {
                            BookModel oldBook = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
                            BookModel newBook = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), true);

                            //firebase non permette di modificare il valore, va rimosso l'elemento dell'array e inserito con i valori modificati
                            db.collection("users").document(userID).update("books", FieldValue.arrayRemove(oldBook));
                            db.collection("users").document(userID).update("books", FieldValue.arrayUnion(newBook));
                        }
                    }
            }
        });
    }

    /**
     * elimina un libro dalla libreria dell'utente, sulla base dell'isbn*/
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
     * visualizza le informazioni relative alla richiesta selezionata*/
    public void createNewContactDialog(ViewHolder holder) {
        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(context);

        View view = View.inflate(context, R.layout.popup, null);

        dialogBuilder.setView(view);

        AlertDialog dialog = dialogBuilder.create();

        loadPopupViewMembers(view);
        String strRequestType = "Richiesta " + requests.get(holder.getAdapterPosition()).getType();

        titlePopup.setText(strRequestType);
        noteText.setText(requests.get(holder.getAdapterPosition()).getNote());

        String strFirstAndLastName = "Da ";
        String strOwnerLocation;

        Number points;
        Number feedbacks;

        if(requests.get(holder.getAdapterPosition()).getSender().equals(Utils.USER_ID))
            strFirstAndLastName += Utils.CURRENT_USER.getFirstName() + " " + Utils.CURRENT_USER.getLastName()+"\na ";

        strFirstAndLastName += requests.get(holder.getAdapterPosition()).getOtherUser().getFirstName() + " " + requests.get(holder.getAdapterPosition()).getOtherUser().getLastName();
        strOwnerLocation = requests.get(holder.getAdapterPosition()).getOtherUser().getNeighborhood();

        points =  (Number) requests.get(holder.getAdapterPosition()).getOtherUser().getKarma().get("points");
        feedbacks = (Number) requests.get(holder.getAdapterPosition()).getOtherUser().getKarma().get("numbers");

        owner.setText(strFirstAndLastName);
        ownerLocation.setText(strOwnerLocation);

        assert feedbacks != null;
        if (feedbacks.longValue() >= 8L) {
            assert points != null;
            double karma = Utils.truncateDoubleValue(points.doubleValue()/feedbacks.doubleValue(), 2);
            String txtReputation = "Reputazione: " + karma + "/5.0\nFeedback:  " + feedbacks;
            reputation.setText(txtReputation);
        } else {
            String txtReputation = "UTENTE NUOVO";
            reputation.setText(txtReputation);
        }

        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(thumbnail);

        if (requests.get(holder.getAdapterPosition()) instanceof RequestShareModel) {
            Date date = ((RequestShareModel) requests.get(holder.getAdapterPosition())).getDate();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String dateString = "Data di restituzione:\n" + sdf.format(date);

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

    @Override
    public int getItemCount() {
        return requests.size();
    }
}

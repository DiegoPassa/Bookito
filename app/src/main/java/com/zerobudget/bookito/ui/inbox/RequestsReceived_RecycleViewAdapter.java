package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.lelloman.identicon.view.ClassicIdenticonView;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Chat.MessageModelWithImage;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.Requests.RequestShareModel;
import com.zerobudget.bookito.models.Requests.RequestTradeModel;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.PopupInbox;
import com.zerobudget.bookito.utils.UserFlag;
import com.zerobudget.bookito.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RequestsReceived_RecycleViewAdapter extends RecyclerView.Adapter<RequestsReceived_RecycleViewAdapter.ViewHolder> {

    protected final Context context;
    protected ArrayList<RequestModel> requests;
    private String isbn_trade;
    protected Button confirmButton;
    protected Button refuseButton;
    protected TextView titlePopup;
    protected TextView owner;
    protected TextView ownerLocation;
    protected TextView returnDate;
    protected ImageView thumbnail;
    protected TextView reputation;
    protected TextView noteText;

    // private AlertDialog.Builder dialogBuilder;
    // private AlertDialog dialog;

    protected FirebaseFirestore db;
    protected FirebaseAuth auth;
    private StorageReference storageRef;

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
        } else
            holder.user_name.setText("undefined");
        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(holder.book_image);
        holder.title.setText(requests.get(holder.getAdapterPosition()).getTitle());
        Log.d("AOAOOAOAOA", requests.get(holder.getAdapterPosition()).getOtherUser().getTelephone());

        String type = requests.get(holder.getAdapterPosition()).getType();

        setupIconType(holder, type);

        if (requests.get(holder.getAdapterPosition()).getOtherUser().isHasPicture()) {
            holder.user_gravatar.setVisibility(View.GONE);
            storageRef.child("profile_pics/").listAll().addOnSuccessListener(listResult -> {
                for (StorageReference item : listResult.getItems()) {
                    // All the items under listRef.
                    if (!item.getName().equals(Utils.USER_ID) && item.getName().equals(idSender)) {
                        //Log.d("item", item.getName());
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Utils.setUriPic(uri.toString());
                            //Log.d("PIC", Utils.URI_PIC);

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

    protected void setupIconType(ViewHolder holder, String type) {
        switch (type) {
            case "Regalo":
                Picasso.get().load(R.drawable.gift).into(holder.type);
                break;

            case "Prestito":
                Picasso.get().load(R.drawable.calendar).into(holder.type);
                break;

            case "Scambio":
                Picasso.get().load(R.drawable.swap).into(holder.type);
                break;

            default:
                break;
        }
    }

    //TODO DALLE RIGHE 113-127 C'È MOLTA RIPETIZIONE DI CODICE, MEGLIO FARE UN METOOD A POSTA DA RICBHIAMARE
    protected void loadPopupViewMembers(View view) {
        confirmButton = view.findViewById(R.id.acceptButton);
        refuseButton = view.findViewById(R.id.refuseButton);
        titlePopup = view.findViewById(R.id.title_popup);
        owner = view.findViewById(R.id.user);
        ownerLocation = view.findViewById(R.id.user_location);
        returnDate = view.findViewById(R.id.return_date);
        thumbnail = view.findViewById(R.id.imageView);
        reputation = view.findViewById(R.id.flag);
        noteText = view.findViewById(R.id.note_text);

        noteText.setMovementMethod(new ScrollingMovementMethod());
    }

    public void createNewContactDialog(ViewHolder holder, Flag flag) {
        checkIfStillExists(requests.get(holder.getAdapterPosition()));

        PopupInbox dialogBuilder = new PopupInbox(context);

        View view = View.inflate(context, R.layout.popup, null);

        dialogBuilder.setView(view);

        AlertDialog dialog = dialogBuilder.create();

        loadPopupViewMembers(view);

        noteText.setText(requests.get(holder.getAdapterPosition()).getNote());

        dialogBuilder.setReputationMessage(reputation, requests.get(holder.getAdapterPosition()), flag);
//        Number points = (Number) requests.get(holder.getAdapterPosition()).getOtherUser().getKarma().get("points");
//        Number feedbacks = (Number) requests.get(holder.getAdapterPosition()).getOtherUser().getKarma().get("numbers");
//
//        if (feedbacks.longValue() >= UserFlag.MIN_FEEDBACKS_FLAG) {
//            String reputationMessage = "Reputazione: " + points.doubleValue() / feedbacks.doubleValue() + "/5.0 ( " + feedbacks  + " )\n";
//            switch (flag) {
//                case GREEN_FLAG: {
//                    reputation.setTextColor(ContextCompat.getColor(context, R.color.green));
//                    reputationMessage += "Utente affidabile!";
//                    break;
//                }
//                case RED_FLAG: {
//                    //è già di default settato a red
////                    reputation.setTextColor(com.google.android.material.R.color.design_default_color_error);
//                    reputationMessage += "Attenzione! Utente inaffidabile!";
//                    break;
//                }
//                default: break;
//            }
//            reputation.setText(reputationMessage);
//        } else {
//            reputation.setText("UTENTE NUOVO");
//            reputation.setTextColor(ContextCompat.getColor(context, R.color.black));
 //       }




        String requestTypeStr = "Richiesta " + requests.get(holder.getAdapterPosition()).getType();
        titlePopup.setText(requestTypeStr);

//        String firstAndLastNameStr = requests.get(holder.getAdapterPosition()).getOtherUser().getFirstName() + " " + requests.get(holder.getAdapterPosition()).getOtherUser().getLastName();
//        owner.setText(firstAndLastNameStr);

        dialogBuilder.setUpUserFullName(owner, requests.get(holder.getAdapterPosition()));

        ownerLocation.setText(requests.get(holder.getAdapterPosition()).getOtherUser().getNeighborhood());


        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(thumbnail);
        if (requests.get(holder.getAdapterPosition()) instanceof RequestShareModel) {
//            Date date = ((RequestShareModel) requests.get(holder.getAdapterPosition())).getDate();
//
//            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
//            String dateString = "Data di restituzione:\n"+sdf.format(date);
//
//            returnDate.setText(dateString);
//            returnDate.setVisibility(View.VISIBLE);
            dialogBuilder.setUpDate((RequestShareModel) requests.get(holder.getAdapterPosition()), returnDate);
        }


        if (requests.get(holder.getAdapterPosition()) instanceof RequestTradeModel) {
            confirmButton.setText("Libreria Utente");
        }

        confirmButton.setOnClickListener(view1 -> {
            Log.d("Pos", "" + holder.getAdapterPosition());
            if (holder.getAdapterPosition() != -1) {
                if (exists) { //controlla che la richiesta esista ancora
                    if (requests.get(holder.getAdapterPosition()) instanceof RequestTradeModel) {

                        Bundle args = new Bundle();
                        String bookString = Utils.getGsonParser().toJson(requests.get(holder.getAdapterPosition()));
                        args.putString("BK", bookString);

                        checkIfTheBookIsAlreadyAcceptedSomewhere(requests.get(holder.getAdapterPosition()), holder, args);
                        //Navigation.findNavController(holder.itemView).navigate(R.id.action_request_page_nav_to_bookTradeFragment, args);
                    } else {
                        acceptRequest(requests.get(holder.getAdapterPosition()), holder);
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

        refuseButton.setOnClickListener(view1 -> {
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

    protected void deleteRequest(RequestModel r) {
        // Log.d("REQUEST_DELETED", r.getrequestId());
        db.collection("requests").document(r.getrequestId()).delete();
    }

    protected void acceptRequest(RequestModel r, ViewHolder holder) {
        changeBookStatus(r.getRequestedBook());

        //controlla prima che non esista già una richiesta accettata per il libro
        db.collection("requests")
                .whereEqualTo("receiver", Utils.USER_ID)
                .whereEqualTo("status", "accepted")
                .whereEqualTo("requestedBook", r.getRequestedBook())
                .get()
                .addOnCompleteListener(task -> {
                    boolean existsOther = task.getResult().size() > 0;
//                    for (QueryDocumentSnapshot doc : task.getResult()) {
//                        if (doc.get("requestedBook").equals(r.getRequestedBook()))
//                            existsOther = true;

                    if (!existsOther) {
                        //l'update ha successo solo se trova il documento, avviso all'utente in caso di insuccesso
                        db.collection("requests").document(r.getrequestId()).update("status", "accepted").addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                //requests.remove(holder.getAdapterPosition());
                                //notifyItemRemoved(holder.getAdapterPosition());
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/chatapp/"+r.getrequestId());

                                ref.child("user1").setValue(r.getReceiver());
                                if (r.getReceiver().equals(Utils.USER_ID))
                                    ref.child("user2").setValue(r.getSender());
                                else ref.child("user2").setValue(Utils.USER_ID);

                                Date now = Timestamp.now().toDate();

                                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                String currentTime = sdf.format(now);
                                SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                String currentDate = sdf1.format(now);

                                //messaggio di default per visualizzare il libro richiesto e per iniziare la conversazione
                                String messageTxt = "Ciao, ti contatto per il tuo libro in "+r.getType()+" dal titolo '"+r.getTitle()+"'!";
                                MessageModelWithImage defaultMsg = new MessageModelWithImage(r.getThumbnail(), r.getSender(), r.getReceiver(), messageTxt, currentTime, currentDate);
                                ref.push().setValue(defaultMsg);

                                Toast.makeText(context, "Richiesta accettata!", Toast.LENGTH_LONG).show();
                            } else
                                Toast.makeText(context, "Oh no, la richiesta è stata eliminata dal richiedente!", Toast.LENGTH_LONG).show();
                        });
                    } else
                        Toast.makeText(context, "Esiste già una richiesta accettata per il libro!\nAttendere o eliminare la richiesta.", Toast.LENGTH_LONG).show();
                });

    }

    private void checkIfStillExists(RequestModel r) {
        db.collection("requests").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult())
                    if (doc.getId().equals(r.getrequestId()))
                        exists = true;
            }
        });
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
                            BookModel newBook =  new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), false);

                            //firebase non permette di modificare il valore, va rimosso l'elemento dell'array e inserito con i valori modificati
                            db.collection("users").document(Utils.USER_ID).update("books", FieldValue.arrayRemove(oldBook));
                            db.collection("users").document(Utils.USER_ID).update("books", FieldValue.arrayUnion(newBook));
                        }
                    }
            }
        });
    }

    private void checkIfTheBookIsAlreadyAcceptedSomewhere(RequestModel r, ViewHolder holder, Bundle args) {
        db.collection("requests").get().addOnCompleteListener(task -> {
            boolean existsOther = false;
            for (QueryDocumentSnapshot doc : task.getResult()) {
                if (doc.get("requestedBook").equals(r.getRequestedBook()) && doc.get("status").equals("accepted"))
                    existsOther = true;

                if (doc.contains("requestTradeBook"))
                    if (doc.get("requestTradeBook").equals(r.getRequestedBook()))
                        existsOther = true;
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
        protected final ImageView book_image;
        protected final ClassicIdenticonView user_gravatar;
        protected final ImageView usr_pic;
        protected final ImageView type;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.requestTitle);
            user_name = itemView.findViewById(R.id.requester_name);
            book_image = itemView.findViewById(R.id.book_image_request);
            request_selected = itemView.findViewById(R.id.request);
            user_gravatar = itemView.findViewById(R.id.user_gravatar);
            usr_pic = itemView.findViewById(R.id.profile_pic);
            type = itemView.findViewById(R.id.icon_type2);
        }
    }


    public String getIsbn_trade() {
        return isbn_trade;
    }

    public void setIsbn_trade(String isbn_trade) {
        this.isbn_trade = isbn_trade;
    }
}

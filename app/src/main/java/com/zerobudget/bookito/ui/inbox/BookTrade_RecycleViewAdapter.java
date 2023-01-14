package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.chat.MessageModelTrade;
import com.zerobudget.bookito.models.chat.MessageModelWithImage;
import com.zerobudget.bookito.models.notification.NotificationModel;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.requests.RequestTradeModel;
import com.zerobudget.bookito.models.search.SearchResultsModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;
import com.zerobudget.bookito.utils.popups.PopupBook;

import java.util.ArrayList;
import java.util.HashMap;

public class BookTrade_RecycleViewAdapter extends RecyclerView.Adapter<BookTrade_RecycleViewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<SearchResultsModel> results;
    private final RequestTradeModel requestTradeModel;
    private AlertDialog dialog;
    private boolean exists;

    FirebaseFirestore db;
    FirebaseAuth auth;

    public BookTrade_RecycleViewAdapter(Context context, ArrayList<SearchResultsModel> bookModels, RequestTradeModel requestTradeModel) {
        this.context = context;
        this.results = bookModels;
        this.requestTradeModel = requestTradeModel;
        this.exists = false;

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }


    @NonNull
    @Override
    public BookTrade_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recycleview_library, parent, false);

        return new BookTrade_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookTrade_RecycleViewAdapter.ViewHolder holder, int position) {
        Picasso.get().load(results.get(position).getBook().getThumbnail()).into(holder.thumbnail);
        holder.title.setText(results.get(position).getBook().getTitle());
        holder.author.setText(results.get(position).getBook().getAuthor());

        holder.card_type.setVisibility(View.GONE);

        holder.book_selected.setOnClickListener(view -> {
            createNewSelectPopup(position, holder);
            //passaggio dei dati del new book al prossimo fragment
/*
            Bundle args = new Bundle();
            String bookString = Utils.getGsonParser().toJson(bookModels.get(position));
            args.putString("BK", bookString);
*/

            // Navigation.findNavController(holder.itemView).navigate(R.id.action_navigation_library_to_bookDeleteFragment, args);
        });
    }

    private void createNewSelectPopup(int position, BookTrade_RecycleViewAdapter.ViewHolder holder) {
        checkIfStillExists(requestTradeModel);

        View view = View.inflate(context, R.layout.popup_book, null);
        PopupBook dialogBuilder = new PopupBook(context, view);

        dialogBuilder.setUpInformation(results.get(holder.getAdapterPosition()).getBook());
        dialogBuilder.getBtnOther().setVisibility(View.VISIBLE);
        dialogBuilder.setTextBtnDefault("Scambia");
        dialogBuilder.setTextOtherBtn("Annulla");

        dialogBuilder.getBtnDefault().setOnClickListener(view1 -> {
            if (exists) { //controlla che la richiesta esista ancora
                checkIfTheBookTradeStillExists(requestTradeModel, results.get(holder.getAdapterPosition()).getBook(), holder);
            } else {
                Toast.makeText(context, "Oh no, la richiesta è stata eliminata dal richiedente!", Toast.LENGTH_LONG).show();
                Navigation.findNavController(holder.itemView).navigate(R.id.action_bookTradeFragment_to_request_page_nav);
            }
            dialog.dismiss();
        });

        dialogBuilder.getBtnOther().setOnClickListener(view2 -> {
            Toast.makeText(context, "Devi scegliere un libro!", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    private void sendNotification(RequestModel r, String status) {
        String otherUserId = r.getSender().equals(Utils.USER_ID) ? r.getReceiver() : r.getSender();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/notification/" + otherUserId);
        String body = status.equals("Accept") ? Utils.CURRENT_USER.getFirstName() + " ha accettato la tua richiesta!" : r.getOtherUser().getFirstName() + " ha rifiutato la tua richiesta!";
        String title = status.equals("Accept") ? "Richiesta accettata!" : "Richiesta rifiutata!";

        /*
        PROBLEMA, NON POSSIAMO RICHIAMARE LA SERIALIZE DEL CURRENT USER PERCHÉ SI TRATTA DI UN USERLIBRARY,
        QUINDI MI GENERA ANCHE TUTTI I SUOI LIBRI E QUINDI DA ERRORE
         */
        UserModel currentUser = new UserModel(Utils.CURRENT_USER.getFirstName(), Utils.CURRENT_USER.getLastName(),
                Utils.CURRENT_USER.getTelephone(), Utils.CURRENT_USER.getTownship(), Utils.CURRENT_USER.getCity(),
                Utils.CURRENT_USER.getKarma(), Utils.CURRENT_USER.isHasPicture(), Utils.CURRENT_USER.getNotificationToken());

        NotificationModel notificationModel = new NotificationModel(Utils.USER_ID, status, body, title, r.getThumbnail(), r, currentUser, Timestamp.now().getSeconds());
        ref.push().setValue(notificationModel.serialize());
    }

    protected void acceptRequest(RequestTradeModel r, BookModel bookTrade) {
        //l'update ha successo solo se trova il documento, avviso all'utente in caso di insuccesso
        db.collection("requests")
                .document(r.getRequestId())
                .update("status", "accepted")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        sendNotification(r, "Accept");

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/chatapp/" + r.getRequestId());

                        ref.child("user1").setValue(r.getReceiver());
                        if (r.getReceiver().equals(Utils.USER_ID))
                            ref.child("user2").setValue(r.getSender());
                        else ref.child("user2").setValue(Utils.USER_ID);

                        //messaggio di default da sender a receiver che viene inviato con il libro della richiesta
                        String messageTxtSender = "Ciao, ti contatto per il tuo libro '" + r.getTitle() + "'!";
                        MessageModelWithImage defaultMsgSender = new MessageModelWithImage(r.getThumbnail(), r.getSender(), Utils.USER_ID, messageTxtSender, "sent", Timestamp.now().getSeconds());
                        ref.push().setValue(defaultMsgSender);

                        //messaggio di default da receiver a sender inviato con il libro scelto per lo scambio dalla libreria del sender
                        String messageTxt = "Ciao, ho scelto il libro '" + bookTrade.getTitle() + "' da scambiare!";
                        MessageModelTrade defaultMsgReceiver = new MessageModelTrade(bookTrade.getIsbn(), bookTrade.getThumbnail(), Utils.USER_ID, r.getSender(), messageTxt, "sent", Timestamp.now().getSeconds());
                        ref.push().setValue(defaultMsgReceiver);

                        Toast.makeText(context, "Richiesta accettata!", Toast.LENGTH_LONG).show();
                        Utils.changeBookStatus(db, Utils.USER_ID, r.getRequestedBook(), false);
                        Utils.changeBookStatus(db, r.getSender(), bookTrade.getIsbn(), false);
                    } else
                        Toast.makeText(context, "Oh no, la richiesta è stata eliminata dal richiedente!", Toast.LENGTH_LONG).show();
                });
        db.collection("requests").document(r.getRequestId()).update("requestTradeBook", bookTrade.getIsbn());
        db.collection("requests").document(r.getRequestId()).update("thumbnailBookTrade", bookTrade.getThumbnail());
        db.collection("requests").document(r.getRequestId()).update("titleBookTrade", bookTrade.getTitle());
    }

    private void checkIfStillExists(RequestTradeModel r) {
        db.collection("requests").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult())
                    if (doc.getId().equals(r.getRequestId()))
                        exists = true;
            }
        });
    }

    /**
     * controlla se il libro esiste ancora nella libreria dell'altro utente per isbn e tipo
     *
     * @param r:         richiesta di riferimento
     * @param bookTrade: libro scelto per lo scambio
     * @param holder:    vista contente i riferimenti all'xml
     */
    private void checkIfTheBookTradeStillExists(RequestTradeModel r, BookModel bookTrade, ViewHolder holder) {
        db.collection("users").document(r.getSender()).get().addOnSuccessListener(documentSnapshot -> {
            Object arrBooks = documentSnapshot.get("books");
            boolean exists = false;

            for (Object o : (ArrayList<Object>) arrBooks) {
                HashMap<String, Object> map = (HashMap<String, Object>) o;
                if (bookTrade.getIsbn().equals(map.get("isbn")) && bookTrade.getType().equals(map.get("type"))) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                Toast.makeText(context, "Oh no, il libro è stato eliminato dal proprietario, non è possibile accettare la richiesta!.", Toast.LENGTH_LONG).show();
                results.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            } else {
                checkIfTheBookStillExists(r, holder);
            }
        });
    }

    /**
     * controlla se il libro dell'utente corrente esiste ancora, per isbn e tipo
     *
     * @param r:      richiesta di riferimento
     * @param holder: vista contente i riferimenti all'xml
     */
    private void checkIfTheBookStillExists(RequestModel r, ViewHolder holder) {
        db.collection("users").document(Utils.USER_ID).get().addOnSuccessListener(documentSnapshot -> {
            Object arrBooks = documentSnapshot.get("books");
            boolean exists = false;
            for (Object o : (ArrayList<Object>) arrBooks) {
                HashMap<String, Object> map = (HashMap<String, Object>) o;
                if (r.getRequestedBook().equals(map.get("isbn")) && r.getType().equals(map.get("type"))) {
                    exists = true;
                    break;
                }
            }

            if (!exists)
                Toast.makeText(context, "Oh no, il libro è stato eliminato dal proprietario, non è possibile accettare la richiesta!.", Toast.LENGTH_LONG).show();
            else {
                acceptRequest(requestTradeModel, results.get(holder.getAdapterPosition()).getBook());
                Navigation.findNavController(holder.itemView).navigate(R.id.action_bookTradeFragment_to_request_page_nav);
            }
        });
    }


    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Layout
        private final ConstraintLayout book_selected;
        private final ImageView thumbnail;
        private final TextView title;
        private final TextView author;
        private final CardView card_type;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Binding from xml layout to Class
            book_selected = itemView.findViewById(R.id.book);
            thumbnail = itemView.findViewById(R.id.book_thumbnail);
            title = itemView.findViewById(R.id.book_title);
            author = itemView.findViewById(R.id.book_author);
            card_type = itemView.findViewById(R.id.card_type);
        }
    }
}

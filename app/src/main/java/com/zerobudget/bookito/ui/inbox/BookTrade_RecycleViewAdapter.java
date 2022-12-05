package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
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
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Chat.MessageModelTrade;
import com.zerobudget.bookito.models.Chat.MessageModelWithImage;
import com.zerobudget.bookito.models.Requests.RequestTradeModel;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.ui.search.SearchResultsModel;
import com.zerobudget.bookito.utils.PopupBook;
import com.zerobudget.bookito.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class BookTrade_RecycleViewAdapter extends RecyclerView.Adapter<BookTrade_RecycleViewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<SearchResultsModel> results;
    private final RequestTradeModel requestTradeModel;

    private AlertDialog.Builder dialogBuilder;
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
                acceptRequest(requestTradeModel, results.get(holder.getAdapterPosition()).getBook());
                Navigation.findNavController(holder.itemView).navigate(R.id.action_bookTradeFragment_to_request_page_nav);
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

    protected void acceptRequest(RequestTradeModel r, BookModel bookTrade) {
        //l'update ha successo solo se trova il documento, avviso all'utente in caso di insuccesso
        db.collection("requests").document(r.getRequestId()).update("status", "accepted").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/chatapp/" + r.getRequestId());

                ref.child("user1").setValue(r.getReceiver());
                if (r.getReceiver().equals(Utils.USER_ID))
                    ref.child("user2").setValue(r.getSender());
                else ref.child("user2").setValue(Utils.USER_ID);

                Date now = Timestamp.now().toDate();

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = sdf.format(now);
                SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String currentDate = sdf1.format(now);

                //messaggio di default da sender a receiver che viene inviato con il libro della richiesta
                String messageTxtSender = "Ciao, ti contatto per il tuo libro '" + r.getTitle() + "'!";
                MessageModelWithImage defaultMsgSender = new MessageModelWithImage(r.getThumbnail(), r.getSender(), Utils.USER_ID, messageTxtSender, "sent", currentTime, currentDate);
                ref.push().setValue(defaultMsgSender);

                //messaggio di default da receiver a sender inviato con il libro scelto per lo scambio dalla libreria del sender
                String messageTxt = "Ciao, ho scelto il libro '" + bookTrade.getTitle() + "' da scambiare!";
                MessageModelTrade defaultMsgReceiver = new MessageModelTrade(bookTrade.getIsbn(), bookTrade.getThumbnail(), Utils.USER_ID, r.getSender(), "sent", messageTxt, currentTime, currentDate);
                ref.push().setValue(defaultMsgReceiver);

                Toast.makeText(context, "Richiesta accettata!", Toast.LENGTH_LONG).show();
                changeBookStatus(Utils.USER_ID, r.getRequestedBook());
                changeBookStatus(r.getSender(), bookTrade.getIsbn());
            } else
                Toast.makeText(context, "Oh no, la richiesta è stata eliminata dal richiedente!", Toast.LENGTH_LONG).show();
        });
        db.collection("requests").document(r.getRequestId()).update("requestTradeBook", bookTrade.getIsbn());
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

    private void changeBookStatus(String userID, String isbn) {
        Log.d("USER", userID);
        db.collection("users").document(userID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Object arr = task.getResult().get("books"); //array dei books
                if (arr != null) //si assicura di cercare solo se esiste quache libro
                    for (Object o : (ArrayList<Object>) arr) {
                        HashMap<Object, Object> map = (HashMap<Object, Object>) o;
                        if (map.get("isbn").equals(isbn)) {
                            BookModel oldBook = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
                            BookModel newBook = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), false);

                            //firebase non permette di modificare il valore, va rimosso l'elemento dell'array e inserito con i valori modificati
                            db.collection("users").document(userID).update("books", FieldValue.arrayRemove(oldBook));
                            db.collection("users").document(userID).update("books", FieldValue.arrayUnion(newBook));
                        }
                    }
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

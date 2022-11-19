package com.zerobudget.bookito.ui.search;

import android.content.Context;
import android.graphics.PorterDuff;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class Search_RecycleViewAdapter extends RecyclerView.Adapter<Search_RecycleViewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<SearchResultsModel> results;

    private FirebaseFirestore db;
    FirebaseAuth auth;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    public Search_RecycleViewAdapter(Context context, ArrayList<SearchResultsModel> bookModels) {
        this.context = context;
        this.results = bookModels;

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public Search_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recycleview_search_results, parent, false);

        return new Search_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Search_RecycleViewAdapter.ViewHolder holder, int position) {
        Picasso.get().load(results.get(position).getBook().getThumbnail()).into(holder.thumbnail);
        holder.title.setText(results.get(position).getBook().getTitle());
        holder.author.setText(results.get(position).getBook().getAuthor());
        String owner = results.get(position).getUser().getFirst_name()+" "+results.get(position).getUser().getLast_name();
        holder.book_owner.setText(owner);
        holder.neighborhood_owner.setText(results.get(position).getUser().getNeighborhood());
        holder.type.setText(results.get(position).getBook().getType());


        holder.book_selected.setOnClickListener(view -> {
            createNewSearchPopup(position, holder);

            /*Bundle args = new Bundle();
            String usrBookString = Utils.getGsonParser().toJson(results.get(position));
            args.putString("USR_BK", usrBookString);*/

            //Navigation.findNavController(holder.itemView).navigate(R.id.action_navigation_search_to_bookRequestFragment, args);

        });
    }

    private void createNewSearchPopup(int position, ViewHolder holder){
        dialogBuilder = new MaterialAlertDialogBuilder(context);
        View view = View.inflate(context, R.layout.fragment_request_book, null);

        TextView bookTitle = view.findViewById(R.id.book_title);
        TextView bookAuthor = view.findViewById(R.id.book_author);
        TextView bookDescription = view.findViewById(R.id.book_description);
        TextView bookOwner = view.findViewById(R.id.book_owner);
        TextView bookType = view.findViewById(R.id.book_type);

        Button btnRequest = view.findViewById(R.id.btn_request);

        ImageView bookThumbnail = view.findViewById(R.id.book_thumbnail);
        ImageView bookmark = view.findViewById(R.id.bookmark);
        ImageView bookmarkOutline = view.findViewById(R.id.bookmark_outline);

        bookTitle.setText(results.get(holder.getAdapterPosition()).getBook().getTitle());
        bookAuthor.setText(results.get(holder.getAdapterPosition()).getBook().getAuthor());
        bookDescription.setText(results.get(holder.getAdapterPosition()).getBook().getDescription());
        bookDescription.setMovementMethod(new ScrollingMovementMethod());

        String owner = results.get(holder.getAdapterPosition()).getUser().getFirst_name() + " " + results.get(holder.getAdapterPosition()).getUser().getLast_name();
        bookOwner.setText(owner);
        bookType.setText(results.get(holder.getAdapterPosition()).getBook().getType());
        Picasso.get().load(results.get(holder.getAdapterPosition()).getBook().getThumbnail()).into(bookThumbnail);

        switch (results.get(holder.getAdapterPosition()).getBook().getType()) {
            case "Scambio":
                bookmarkOutline.setColorFilter(context.getColor(R.color.bookmark_outline_scambio), PorterDuff.Mode.SRC_ATOP);
                bookmark.setColorFilter(context.getColor(R.color.bookmark_scambio), PorterDuff.Mode.SRC_ATOP);
                break;
            case "Prestito":
                bookmarkOutline.setColorFilter(context.getColor(R.color.bookmark_outline_prestito), PorterDuff.Mode.SRC_ATOP);
                bookmark.setColorFilter(context.getColor(R.color.bookmark_prestito), PorterDuff.Mode.SRC_ATOP);
                break;
            case "Regalo":
                bookmarkOutline.setColorFilter(context.getColor(R.color.bookmark_outine_regalo), PorterDuff.Mode.SRC_ATOP);
                bookmark.setColorFilter(context.getColor(R.color.bookmark_regalo), PorterDuff.Mode.SRC_ATOP);
                break;
            default:
                Picasso.get().load(R.drawable.bookmark_template).into(bookmark);
                break;
        }

        btnRequest.setOnClickListener(view1 -> {
            FirebaseUser currentUser = auth.getCurrentUser();
            //TODO: in attesa dell'autenticazione dell'utente qusto resta commentato, cambiare anche id nei set sotto
            //if (currentUser != null) {
            //   String id = currentUser.getUid();

            //preleva l'id dell'utente dal database
            db.collection("users").get().addOnCompleteListener(task -> {
                RequestModel rm = new RequestModel();
                rm.setRequestedBook(results.get(holder.getAdapterPosition()).getBook().getIsbn());
                rm.setTitle(results.get(holder.getAdapterPosition()).getBook().getTitle());
                rm.setThumbnail(results.get(holder.getAdapterPosition()).getBook().getThumbnail());
                rm.setStatus("undefined");
                rm.setType(results.get(holder.getAdapterPosition()).getBook().getType());
                rm.setSender(Utils.USER_ID);

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        if (doc.get("telephone") == null) Log.d("ERRORE", "SUPREMOOOO");
                        if (doc.get("telephone").equals(results.get(holder.getAdapterPosition()).getUser().getTelephone())) {
                            rm.setReceiver(doc.getId());
                            Log.d("REC", rm.getReceiver());
                            requestBook(rm, view, holder); //prova a inserire la richiesta del libro

                        }
                    }
                }
            });

            dialog.dismiss();
        });

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();
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

    private void requestBook(RequestModel rm, View view, ViewHolder holder) {

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
                    Toast.makeText(context, "Attenzione! La richiesta per "+results.get(holder.getAdapterPosition()).getBook().getTitle()+" esiste già!", Toast.LENGTH_LONG).show();
                } else {
                    db.collection("requests").add(rm.serialize()).addOnSuccessListener(documentReference -> {
                        Log.d("OKK", documentReference.getId());
                    }).addOnFailureListener(e -> Log.w("ERROR", "Error adding document", e));

                    Log.d("Sent to: ", results.get(holder.getAdapterPosition()).getUser().getNotificationToken());
                    Utils.sendPushNotification("Richiesta di libro", UserModel.getCurrentUser().getFirst_name() + " ti ha richiesto il libro: " + rm.getTitle(), results.get(holder.getAdapterPosition()).getUser().getNotificationToken());
                    Toast.makeText(context, "La richiesta è andata a buon fine!", Toast.LENGTH_LONG).show();

                }

            } else {
                Log.d("ERR", "Error getting documents: ", task.getException());
            }
        });
        //}

    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Layout
        private final RelativeLayout book_selected;
        private final ImageView thumbnail;
        private final TextView title;
        private final TextView author;
        private final TextView book_owner;
        private final TextView neighborhood_owner;
        private final TextView type;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Binding from xml layout to Class
            book_selected = itemView.findViewById(R.id.book);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.book_title);
            author = itemView.findViewById(R.id.book_author);
            book_owner = itemView.findViewById(R.id.book_owner);
            neighborhood_owner = itemView.findViewById(R.id.neighborhood_owner);
            type = itemView.findViewById(R.id.type);
        }
    }

}


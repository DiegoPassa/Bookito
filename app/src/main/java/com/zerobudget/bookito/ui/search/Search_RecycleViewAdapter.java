package com.zerobudget.bookito.ui.search;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Notifications;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.requests.RequestShareModel;
import com.zerobudget.bookito.models.requests.RequestTradeModel;
import com.zerobudget.bookito.models.search.SearchResultsModel;
import com.zerobudget.bookito.utils.Utils;
import com.zerobudget.bookito.utils.popups.PopupSearchBook;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Search_RecycleViewAdapter extends RecyclerView.Adapter<Search_RecycleViewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<SearchResultsModel> results;

    private final FirebaseFirestore db;
    FirebaseAuth auth;

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
        String owner = results.get(position).getUser().getFirstName() + " " + results.get(position).getUser().getLastName();
        holder.book_owner.setText(owner);
        holder.neighborhood_owner.setText(context.getString(R.string.user_location, results.get(position).getUser().getTownship(), results.get(position).getUser().getCity()));
        //holder.type.setText(results.get(position).getBook().getType());

        Utils.setUpIconBookType(results.get(holder.getAdapterPosition()).getBook().getType(), holder.book_type);

        holder.book_selected.setOnClickListener(view -> {
            createNewSearchPopup(holder);

            /*Bundle args = new Bundle();
            String usrBookString = Utils.getGsonParser().toJson(results.get(position));
            args.putString("USR_BK", usrBookString);*/

            //Navigation.findNavController(holder.itemView).navigate(R.id.action_navigation_search_to_bookRequestFragment, args);

        });
    }

    /**
     * crea il popup per la richiesta del libro, utilizzando la classe PopupSearchBook che
     * eredita alcuni metodi dal PopupBook ed evita la ripetizione di righe di codice
     *
     * @param holder: oggetto contente i binding del layout xml
     */
    private void createNewSearchPopup(ViewHolder holder) {
        View view = View.inflate(context, R.layout.popup_book, null);
        PopupSearchBook dialogBuilder = new PopupSearchBook(context, view);

        dialogBuilder.setUpInformation(results.get(holder.getAdapterPosition()));
        dialogBuilder.getBtnOther().setVisibility(View.GONE);
        dialogBuilder.setTextBtnDefault("Richiedi");

        dialogBuilder.getBtnDefault().setOnClickListener(view1 -> {
            String type = results.get(holder.getAdapterPosition()).getBook().getType();
            //preleva l'id dell'utente dal database
            db.collection("users").get().addOnCompleteListener(task -> {
                RequestModel rm;
                if (type.equals("Prestito"))
                    rm = new RequestShareModel();
                else if (type.equals("Scambio")) rm = new RequestTradeModel();
                else rm = new RequestModel();

                rm.setRequestedBook(results.get(holder.getAdapterPosition()).getBook().getIsbn());
                rm.setTitle(results.get(holder.getAdapterPosition()).getBook().getTitle());
                rm.setThumbnail(results.get(holder.getAdapterPosition()).getBook().getThumbnail());
                rm.setStatus("undefined");
                rm.setType(results.get(holder.getAdapterPosition()).getBook().getType());
                rm.setSender(Utils.USER_ID);
                rm.setNote(dialogBuilder.getTxtRequestNote().getText().toString());

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        if (doc.get("telephone").equals(results.get(holder.getAdapterPosition()).getUser().getTelephone())) {
                            rm.setReceiver(doc.getId());
                            if (rm instanceof RequestShareModel)
                                openCalendarPopup((RequestShareModel) rm, holder, dialog);
                            else
                                requestBook(rm, holder, dialog); //prova a inserire la richiesta del libro
                        }
                    }
                }
            });

        });

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * apre il popup con il calendario per selezionare la data di restituzione del libro preso in prestito
     *
     * @param rm:     richiesta di prestito, definta dalla classe RequestShareModel
     * @param holder: oggetto contente i binding del layout xml
     * @param dialog: finestra di alert dialog
     */
    private void openCalendarPopup(RequestShareModel rm, ViewHolder holder, AlertDialog dialog) {
        MaterialAlertDialogBuilder calendarPopup = new MaterialAlertDialogBuilder(context);
        View popup_view = View.inflate(context, R.layout.popup_datepicker, null);

        DatePicker datePicker = popup_view.findViewById(R.id.date_picker);
        datePicker.setMinDate(System.currentTimeMillis());

        calendarPopup.setView(popup_view);
        AlertDialog builderDate = calendarPopup.create();

        Button acceptButton = popup_view.findViewById(R.id.acceptButton);
        Button refuseButton = popup_view.findViewById(R.id.refuseButton);

        acceptButton.setOnClickListener(click -> {
            Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            rm.setDate(new Timestamp(calendar.getTime()));
            requestBook(rm, holder, dialog);
            builderDate.dismiss();
            dialog.dismiss();
        });

        refuseButton.setOnClickListener(click -> {
            Toast.makeText(context, "Richiesta non effettuata", Toast.LENGTH_LONG).show();
            builderDate.dismiss();
            dialog.dismiss();
        });

        builderDate.show();

    }

    /**
     * controlla se non esista già una richiesta in corso per lo stesso libro
     *
     * @param rDoc: la richiesta prelevata dal database, definita dalla classe RequestModel
     * @param rm:la richiesta che l'utente corrente sta effettuando, definita dalla classe RequestModel
     * @return boolean: false se le due richieste non coincidono per i controlli fatti all'interno, true altrimenti
     */
    private boolean checkRequests(RequestModel rDoc, RequestModel rm) {
        boolean exists = false;

        //controlla se esiste già una richiesta fatta dal current user per quel libro in stato undefined
        if (rDoc.getRequestedBook().equals(rm.getRequestedBook())
                && rDoc.getStatus().equals("undefined")
                && rDoc.getSender().equals(Utils.USER_ID)
                && rDoc.getReceiver().equals(rm.getReceiver())) {
            exists = true;
            Toast.makeText(context, "Attenzione! Hai già effettuato la richiesta per " + rm.getTitle() + "!", Toast.LENGTH_LONG).show();
        }


        if (rDoc.getStatus().equals("accepted") || rDoc.getStatus().equals("ongoing")) {
            if (rDoc.getReceiver().equals(rm.getReceiver()) && rDoc.getRequestedBook().equals(rm.getRequestedBook())) {
                exists = true;
                Toast.makeText(context, "Attenzione! Esista già una richiesta in corso per '" + rm.getTitle() + "'!", Toast.LENGTH_LONG).show();
            }

            if (rDoc instanceof RequestTradeModel) {
                if (((RequestTradeModel) rDoc).getRequestTradeBook().equals(rm.getRequestedBook())) {
                    exists = true;
                    Toast.makeText(context, "Attenzione! Il libro" + rm.getTitle() + " è già un una richiesta in corso!", Toast.LENGTH_LONG).show();
                }
            }

        } else {
            if (rDoc.getStatus().equals(rm.getSender()) && rDoc.getRequestedBook().equals(rm.getRequestedBook()) && rDoc.getStatus().equals("undefined")) {
                exists = true;
                Toast.makeText(context, "Attenzione! Esista già una richiesta in corso per '" + rm.getTitle() + "'!", Toast.LENGTH_LONG).show();
            }
        }
        return exists;
    }


    /**
     * effettua la richiesta del libro
     *
     * @param rm:     richiesta effettuata dall'utente corrente
     * @param holder: oggetto contente i binding del layout xml
     * @param dialog: finestra di alert dialog
     */
    private void requestBook(RequestModel rm, ViewHolder holder, AlertDialog dialog) {
        dialog.dismiss();
        db.collection("requests").get().addOnCompleteListener(task -> {
            boolean err = false;
            if (task.isSuccessful()) {

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    RequestModel rDoc;
                    if (doc.contains("requestTradeBook"))
                        rDoc = doc.toObject(RequestTradeModel.class);
                        //controlla se esiste già una richiesta uguale, non posso usare serialize di request model perchè ho lo status che varia
                    else rDoc = doc.toObject(RequestModel.class);

                    if (checkRequests(rDoc, rm)) {
                        err = true;
                        break;
                    }
                }
                //se esiste già una richiesta da errore nella checkRequests
                if (!err) {
                    db.collection("requests").add(rm.serialize()).addOnFailureListener(e -> Log.e(TAG, "Error adding document", e));

                    int position = holder.getAdapterPosition();
                    //rimuove il libro dai visualizzati
                    try {
                        Notifications.sendPushNotification(
                                Utils.CURRENT_USER.getFirstName() + " ti ha richiesto il libro: \"" + rm.getTitle() + "\"",
                                "Nuova richiesta",
                                results.get(position).getUser().getNotificationToken());
                    } catch (Exception e) {
                        Log.e("Errore", e.getMessage());
                    }
                    results.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "La richiesta è andata a buon fine!", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "Error getting documents: ", task.getException());
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
        private final ImageView book_type;
        private final TextView title;
        private final TextView author;
        private final TextView book_owner;
        private final TextView neighborhood_owner;
        //private final TextView type;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Binding from xml layout to Class
            book_selected = itemView.findViewById(R.id.book);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.book_title);
            author = itemView.findViewById(R.id.book_author);
            book_owner = itemView.findViewById(R.id.book_owner);
            neighborhood_owner = itemView.findViewById(R.id.neighborhood_owner);
            //type = itemView.findViewById(R.id.type);
            book_type = itemView.findViewById(R.id.icon_type);
        }
    }

}


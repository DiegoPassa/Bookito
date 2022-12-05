package com.zerobudget.bookito.ui.library;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jp.wasabeef.picasso.transformations.BlurTransformation;

public class Book_RecycleViewAdapter extends RecyclerView.Adapter<Book_RecycleViewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<BookModel> bookModels;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;

    private final TextView emptyWarning;
    private String items[];
    ArrayAdapter<String> adapterItems;

    FirebaseFirestore db;
    FirebaseAuth auth;

    public Book_RecycleViewAdapter(Context context, ArrayList<BookModel> bookModels, TextView emptyWarning) {
        this.context = context;
        this.bookModels = bookModels;
        this.emptyWarning = emptyWarning;


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public Book_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recycleview_library, parent, false);

        return new Book_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Book_RecycleViewAdapter.ViewHolder holder, int position) {
        holder.title.setText(bookModels.get(position).getTitle());

        //int book_number = holder.getAdapterPosition()+1;
        //holder.book_id.setText(book_number+"");
        if (bookModels.get(holder.getAdapterPosition()).getStatus()) {
            holder.wait_icon.setVisibility(View.GONE);
            Picasso.get().load(bookModels.get(position).getThumbnail()).into(holder.thumbnail);
        } else {
            Picasso.get().load(bookModels.get(position).getThumbnail()).transform(new BlurTransformation(context, 3, 2)).into(holder.thumbnail);
            holder.wait_icon.setVisibility(View.VISIBLE);
        }

        holder.author.setText(bookModels.get(position).getAuthor());

        loadIconBookType(holder, holder.book_type);

        holder.book_selected.setOnClickListener(view -> {
            createNewDeletePopup(holder);
        });


        //modifica del tipo del libro, solo se esso è abilitato
        if (bookModels.get(holder.getAdapterPosition()).getStatus())
            holder.touchAreaEditType.setOnClickListener(view12 -> {
                db.collection("users")
                        .document(Utils.USER_ID)
                        .get()
                        .addOnSuccessListener(task -> {
                            Object books = task.get("books");
                            for (Object o : (ArrayList<Object>) books) {
                                HashMap<String, Object> map = (HashMap<String, Object>) o;
                                if (map.get("isbn").equals(bookModels.get(holder.getAdapterPosition()).getIsbn())) {
                                    createNewEditTypePopup(holder);
                                }
                            }
                        });
            });
    }

    /**
     * popup creato qunado l'utente seleziona un libro
     * permette di visualizzare le informazioni relative ad esso
     * permette di eliminare il libro se esso è abilitato (status = true)
     */
    private void createNewDeletePopup(ViewHolder holder) {
        dialogBuilder = new MaterialAlertDialogBuilder(context);
        View view = View.inflate(context, R.layout.popup_delete_book, null);

        TextView bookTitle = view.findViewById(R.id.book_title);
        TextView bookAuthor = view.findViewById(R.id.book_author);
        TextView bookDescription = view.findViewById(R.id.book_description);
        Button btnDelete = view.findViewById(R.id.btn_delete);
        ImageView bookThumbnail = view.findViewById(R.id.book_thumbnail);
        ImageView bookType = view.findViewById(R.id.icon_type);

        bookTitle.setText(bookModels.get(holder.getAdapterPosition()).getTitle());
        bookAuthor.setText(bookModels.get(holder.getAdapterPosition()).getAuthor());
        bookDescription.setText(bookModels.get(holder.getAdapterPosition()).getDescription());
        bookDescription.setMovementMethod(new ScrollingMovementMethod());
        Picasso.get().load(bookModels.get(holder.getAdapterPosition()).getThumbnail()).into(bookThumbnail);

        loadIconBookType(holder, bookType);

        //se il libro è in una richiesta accettata non può essere eliminato
        if (!bookModels.get(holder.getAdapterPosition()).getStatus()) {
            btnDelete.setEnabled(false);
        }

        btnDelete.setOnClickListener(view1 -> {
            //conferma dell'eliminazione
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
            builder.setTitle("Conferma eliminazione");
            builder.setMessage(Html.fromHtml("Sei sicuro di voler eliminare il libro: <br><b>" + bookModels.get(holder.getAdapterPosition()).getTitle() + "</b>?", Html.FROM_HTML_MODE_LEGACY));
            builder.setPositiveButton("SI", (dialogInterface, i) -> {
                Log.d("ELIMINO:", "" + bookModels.get(holder.getAdapterPosition()));
                //rimuove il libro selezionato
                db.collection("users").document(Utils.USER_ID).update("books", FieldValue.arrayRemove(bookModels.get(holder.getAdapterPosition())));

                //rimuove la richiesta relativa a quel libro se esiste ed è undefined
                db.collection("requests").whereEqualTo("status", "undefined").whereEqualTo("receiver", Utils.USER_ID).whereEqualTo("requestedBook", bookModels.get(holder.getAdapterPosition())).get().addOnCompleteListener(task -> {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    for (DocumentSnapshot document : documents) {
                        DocumentReference documentReference = document.getReference();
                        documentReference.delete();
                    }
                });

                Toast.makeText(context, bookModels.get(holder.getAdapterPosition()).getTitle() + " eliminato!", Toast.LENGTH_LONG).show();
                bookModels.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                Utils.toggleEmptyWarning(emptyWarning, Utils.CURRENT_USER.getLibrary().size());
                dialogInterface.dismiss();
            }).setNegativeButton("NO", (dialogInterface, i) -> {
                dialogInterface.dismiss();
            }).show();

            dialog.dismiss();
        });

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * crea il popup per la modifica delle informazioni del libro*/
    private void createNewEditTypePopup(ViewHolder holder) {
        dialogBuilder = new MaterialAlertDialogBuilder(context);
        View view = View.inflate(context, R.layout.popup_edit_book, null);

        TextView bookTitle = view.findViewById(R.id.book_title);
        TextView bookAuthor = view.findViewById(R.id.book_author);
        TextView bookDescription = view.findViewById(R.id.book_description);
        Button btnRefuse = view.findViewById(R.id.btn_refuse);
        Button btnConfirm = view.findViewById(R.id.btn_confirm);
        ImageView bookThumbnail = view.findViewById(R.id.book_thumbnail);
        ImageView bookType = view.findViewById(R.id.icon_type);

        AutoCompleteTextView choosenType = view.findViewById(R.id.autoCompleteTextView);
        TextInputLayout inputText = view.findViewById(R.id.InputText);

        bookTitle.setText(bookModels.get(holder.getAdapterPosition()).getTitle());
        bookAuthor.setText(bookModels.get(holder.getAdapterPosition()).getAuthor());
        bookDescription.setText(bookModels.get(holder.getAdapterPosition()).getDescription());
        bookDescription.setMovementMethod(new ScrollingMovementMethod());
        Picasso.get().load(bookModels.get(holder.getAdapterPosition()).getThumbnail()).into(bookThumbnail);

        loadIconBookType(holder, bookType);
        loadItems(holder, choosenType);

        btnConfirm.setOnClickListener(view1 -> {
            String action = choosenType.getText().toString();

            if (!action.equals("Regalo") && !action.equals("Scambio") && !action.equals("Prestito")) {
                inputText.setError("Devi selezionare un'azione!");
                inputText.setDefaultHintTextColor(ColorStateList.valueOf(context.getResources().getColor(R.color.md_theme_light_error)));
            } else {
                changeBookType(holder, action);
                dialog.dismiss();
                Toast.makeText(context, "Modifica avvenuta con successo!", Toast.LENGTH_LONG).show();
            }
        });

        btnRefuse.setOnClickListener(view1 -> {
            dialog.dismiss();
            Toast.makeText(context, "Modifica annullata!", Toast.LENGTH_LONG).show();
        });
        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    /**
     * carica l'icona sulla base del tipo del libro
     */
    private void loadIconBookType(ViewHolder holder, ImageView icon) {
        switch (bookModels.get(holder.getAdapterPosition()).getType()) {
            case "Scambio":
                Picasso.get().load(R.drawable.swap).into(icon);
                break;
            case "Prestito":
                Picasso.get().load(R.drawable.calendar).into(icon);
                break;
            case "Regalo":
                Picasso.get().load(R.drawable.gift).into(icon);
                break;
            default:
                break;
        }
    }

    /**
     * carica i valori dei tipi con cui rempiere il menu a tendina nella modifica del libro*/
    private void loadItems(ViewHolder holder, AutoCompleteTextView txt) {
        String type = bookModels.get(holder.getAdapterPosition()).getType();

        switch (type) {
            case "Scambio":
                items = new String[]{"Prestito", "Regalo"};
                break;
            case "Prestito":
                items = new String[]{"Scambio", "Regalo"};
                break;
            case "Regalo":
                items = new String[]{"Prestito", "Scambio"};
                break;
            default:
                items = context.getResources().getStringArray(R.array.azioni_libro);
        }

        adapterItems = new ArrayAdapter<>(context, R.layout.dropdown_item, items);
        txt.setAdapter(adapterItems);
    }

    /**
     * modifica il tipo del libro sul database, rimozione libro precedente e inserimento nuovo
     * firebase non permette la modifica del campo di un'array di mappe
     */
    private void changeBookType(ViewHolder holder, String newType) {
        db.collection("users")
                .document(Utils.USER_ID)
                .get()
                .addOnSuccessListener(task -> {
                    Object books = task.get("books");
                    for (Object o : (ArrayList<Object>) books) {
                        HashMap<String, Object> map = (HashMap<String, Object>) o;
                        if (map.get("isbn").equals(bookModels.get(holder.getAdapterPosition()).getIsbn())) {
                            BookModel oldBook = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), (String) map.get("type"), (boolean) map.get("status"));
                            BookModel newBook = new BookModel((String) map.get("thumbnail"), (String) map.get("isbn"), (String) map.get("title"), (String) map.get("author"), (String) map.get("description"), newType, (boolean) map.get("status"));

                            //firebase non permette di modificare il valore, va rimosso l'elemento dell'array e inserito con i valori modificati
                            db.collection("users").document(Utils.USER_ID).update("books", FieldValue.arrayRemove(oldBook));
                            db.collection("users").document(Utils.USER_ID).update("books", FieldValue.arrayUnion(newBook));

                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return bookModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ConstraintLayout book_selected;
        private final ImageView thumbnail;
        private final TextView title;
        private final TextView author;
        private final ImageView wait_icon;
        private final ImageView book_type;
        private final ConstraintLayout touchAreaEditType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.book_thumbnail);
            title = itemView.findViewById(R.id.book_title);
            author = itemView.findViewById(R.id.book_author);
            book_selected = itemView.findViewById(R.id.book);
            wait_icon = itemView.findViewById(R.id.wait_icon);
            book_type = itemView.findViewById(R.id.icon_type);
            touchAreaEditType = itemView.findViewById(R.id.touchAreaEditType);
        }
    }

}

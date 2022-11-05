package com.zerobudget.bookito.ui.add;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.ui.library.BookModel;
import com.zerobudget.bookito.ui.users.UserModel;

public class BookDetails_RecycleViewAdapter extends RecyclerView.Adapter<BookDetails_RecycleViewAdapter.ViewHolder> {

    private final Context context;
    protected static  BookModel bookModel;

    public BookDetails_RecycleViewAdapter(Context context, BookModel bookModel) {
        this.context = context;
        this.bookModel = bookModel;
    }

    @NonNull
    @Override
    public BookDetails_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycleview_book_details, parent, false);

        return new BookDetails_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookDetails_RecycleViewAdapter.ViewHolder holder, int position) {
        holder.title.setText(bookModel.getTitle());

        Picasso.get().load(bookModel.getThumbnail()).into(holder.thumbnail);
        holder.author.setText(bookModel.getAuthor());
        holder.description.setText(bookModel.getDescription());
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final ImageView thumbnail;
        private final TextView title;
        private final TextView author;
        private final TextView description;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.book_thumbnail);
            title = itemView.findViewById(R.id.book_title);
            author = itemView.findViewById(R.id.book_author);
            description = itemView.findViewById(R.id.book_description);

            itemView.findViewById(R.id.btn_confirm).setOnClickListener(view -> {
                addBook(); //aggiunge il libro al database
                Navigation.findNavController(view).navigate(R.id.to_navigation_library);
            });

            itemView.findViewById(R.id.btn_cancel).setOnClickListener(view -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this.title.getContext());
                builder.setTitle("Result");
                builder.setMessage("Inserimento annullato correttamente");
                builder.setPositiveButton("OK",  (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    Navigation.findNavController(view).navigate(R.id.to_navigation_library);
                }).show();
            });
        }


    }

    //aggiunge il libro bookModel al database
    protected static void addBook() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //TODO: in attesa dell'autenticazione dell'utente qusto resta commentato
        //if (currentUser != null) {
        //   String id = currentUser.getUid();

        db.collection("users").document("AZLYEN9WqTOVXiglkPJT")
                .update("books", FieldValue.arrayUnion(bookModel.serialize())).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                        UserModel.getCurrentUser().appendBook(bookModel);
                });
        // }
    }
}



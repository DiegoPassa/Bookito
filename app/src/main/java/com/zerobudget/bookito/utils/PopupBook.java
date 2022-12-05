package com.zerobudget.bookito.utils;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.book.BookModel;

public class PopupBook extends MaterialAlertDialogBuilder {
    private TextView txtBookTitle;
    private TextView txtBookAuthor;
    private TextView txtBookDescription;
    private ImageView imgBookThumbnail;
    private ImageView imgBookIconType;
    private Button btnDefault;

    public PopupBook(@NonNull Context context, View view) {
        super(context);
        this.txtBookTitle = view.findViewById(R.id.book_title);
        this.txtBookAuthor = view.findViewById(R.id.book_author);
        this.txtBookDescription = view.findViewById(R.id.book_description);
        this.imgBookThumbnail = view.findViewById(R.id.book_thumbnail);
        this.imgBookIconType = view.findViewById(R.id.icon_type);
        this.btnDefault = view.findViewById(R.id.btn_default);
    }

    public PopupBook(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }

    public void setUpInformation(BookModel b) {
        this.txtBookTitle.setText(b.getTitle());
        this.txtBookAuthor.setText(b.getAuthor());
        this.txtBookDescription.setText(b.getDescription());
        this.txtBookDescription.setMovementMethod(new ScrollingMovementMethod());
        Picasso.get().load(b.getThumbnail()).into(this.imgBookThumbnail);
        loadIconBookType(b.getType());
    }

    public void setUpButtons(BookModel b, boolean isDeleteBook) {
        if (isDeleteBook) {
            this.btnDefault.setText("Elimina");
            //se il libro è in una richiesta accettata non può essere eliminato
            if (!b.getStatus())
                this.btnDefault.setEnabled(false);
        } else
            this.btnDefault.setText("Conferma");

    }


    /**
     * carica l'icona sulla base del tipo del libro
     */
    private void loadIconBookType(String type) {
        switch (type) {
            case "Scambio":
                Picasso.get().load(R.drawable.swap).into(imgBookIconType);
                break;
            case "Prestito":
                Picasso.get().load(R.drawable.calendar).into(imgBookIconType);
                break;
            case "Regalo":
                Picasso.get().load(R.drawable.gift).into(imgBookIconType);
                break;
            default:
                break;
        }
    }

    public Button getBtnDefault() {
        return btnDefault;
    }
}

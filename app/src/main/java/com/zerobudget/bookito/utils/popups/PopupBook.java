package com.zerobudget.bookito.utils.popups;

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
import com.zerobudget.bookito.utils.Utils;

public class PopupBook extends MaterialAlertDialogBuilder {
    private TextView txtBookTitle;
    private TextView txtBookAuthor;
    private TextView txtBookDescription;
    private ImageView imgBookThumbnail;
    private ImageView imgBookIconType;
    private Button btnDefault;
    private Button btnOther;

    public PopupBook(@NonNull Context context, View view) {
        super(context);
        this.txtBookTitle = view.findViewById(R.id.book_title);
        this.txtBookAuthor = view.findViewById(R.id.book_author);
        this.txtBookDescription = view.findViewById(R.id.book_description);
        this.imgBookThumbnail = view.findViewById(R.id.book_thumbnail);
        this.imgBookIconType = view.findViewById(R.id.icon_type);
        this.btnDefault = view.findViewById(R.id.btn_default);
        this.btnOther = view.findViewById(R.id.btn_other);
    }

    public PopupBook(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }

    /**
     * inserisce le informazioni negli elementi xml
     *
     * @param b: bookModel di riferimento da cui prelevare i dati per visualizzarne le informazioni
     */
    public void setUpInformation(BookModel b) {
        this.txtBookTitle.setText(b.getTitle());
        this.txtBookAuthor.setText(b.getAuthor());
        this.txtBookDescription.setText(b.getDescription());
        this.txtBookDescription.setMovementMethod(new ScrollingMovementMethod());
        Picasso.get().load(b.getThumbnail()).into(this.imgBookThumbnail);
        loadIconBookType(b.getType());
    }

    /**
     * carica l'icona sulla base del tipo del libro
     *
     * @param type: stringa contente il tipo del libro, vincolata ai valori Scambio, Prestito, Regalo
     */
    private void loadIconBookType(String type) {
        Utils.setUpIconBookType(type, imgBookIconType);
    }

    public Button getBtnDefault() {
        return btnDefault;
    }

    public Button getBtnOther() {
        return btnOther;
    }

    public void setTextBtnDefault(String txt) {
        this.btnDefault.setText(txt);
    }

    public void setTextOtherBtn(String txt) {
        this.btnOther.setText(txt);
    }
}

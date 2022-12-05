package com.zerobudget.bookito.utils;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.book.BookModel;

public class PopupEditBook extends PopupBook {
    private Button btnOther;
    private AutoCompleteTextView choosenType;
    private TextInputLayout inputText;
    private String items[];
    private Context context;
    private ArrayAdapter<String> adapterItems;


    public PopupEditBook(@NonNull Context context, View view) {
        super(context, view);
        this.context = context;
        this.btnOther = view.findViewById(R.id.btn_other);
        this.choosenType = view.findViewById(R.id.autoCompleteTextView);
        ConstraintLayout c = view.findViewById(R.id.constr);
        c.setVisibility(View.VISIBLE);
        this.inputText = view.findViewById(R.id.InputText);
    }

    public PopupEditBook(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }

    @Override
    public void setUpInformation(BookModel b) {
        super.setUpInformation(b);
        this.choosenType.setVisibility(View.VISIBLE);
        this.btnOther.setVisibility(View.VISIBLE);
        loadItems(b);
    }

    @Override
    public void setUpButtons(BookModel b, boolean isDeleteBook) {
        super.setUpButtons(b, isDeleteBook);
        this.btnOther.setText("Annulla");
    }

    /**
     * carica i valori dei tipi con cui rempiere il menu a tendina nella modifica del libro
     */
    private void loadItems(BookModel b) {
        String type = b.getType();

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
        this.choosenType.setAdapter(adapterItems);
    }

    public Button getBtnOther() {
        return btnOther;
    }

    public AutoCompleteTextView getChoosenType() {
        return choosenType;
    }

    public TextInputLayout getInputText() {
        return inputText;
    }
}

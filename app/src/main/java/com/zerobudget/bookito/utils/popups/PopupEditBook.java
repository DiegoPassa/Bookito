package com.zerobudget.bookito.utils.popups;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.book.BookModel;

public class PopupEditBook extends PopupBook {
    private AutoCompleteTextView choosenType;
    private TextInputLayout inputText;
    private String items[];
    private Context context;
    private ArrayAdapter<String> adapterItems;


    public PopupEditBook(@NonNull Context context, View view) {
        super(context, view);
        ConstraintLayout c = view.findViewById(R.id.constr_edit);
        c.setVisibility(View.VISIBLE);
        this.context = context;
        this.choosenType = view.findViewById(R.id.autoCompleteTextView);
        this.inputText = view.findViewById(R.id.InputText);
    }

    public PopupEditBook(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }

    @Override
    public void setUpInformation(BookModel b) {
        super.setUpInformation(b);
        super.getBtnOther().setVisibility(View.VISIBLE);
        super.getBtnOther().setText("Annulla");
        this.choosenType.setVisibility(View.VISIBLE);
        loadItems(b);
    }


    /**
     * carica i valori dei tipi con cui rempiere il menu a tendina nella modifica del libro
     *
     * @param b: bookModel da cui prelevare le informazioni
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

    public AutoCompleteTextView getChoosenType() {
        return choosenType;
    }

    public TextInputLayout getInputText() {
        return inputText;
    }
}

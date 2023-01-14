package com.zerobudget.bookito.utils.popups;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.search.SearchResultsModel;

public class PopupSearchBook extends PopupBook {
    private TextView txtBookOwner;
    private TextView txtRequestNote;

    public PopupSearchBook(@NonNull Context context, View view) {
        super(context, view);
        ConstraintLayout c = view.findViewById(R.id.constr_request_note);
        c.setVisibility(View.VISIBLE);
        this.txtBookOwner = view.findViewById(R.id.book_owner);
        this.txtBookOwner.setVisibility(View.VISIBLE);
        this.txtRequestNote = view.findViewById(R.id.request_note);
    }

    public PopupSearchBook(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }


    public void setUpInformation(SearchResultsModel s) {
        super.setUpInformation(s.getBook());
        String owner = s.getUser().getFirstName() + " " + s.getUser().getLastName();
        this.txtBookOwner.setText(owner);
    }

    public TextView getTxtRequestNote() {
        return txtRequestNote;
    }
}

package com.zerobudget.bookito.utils.popups;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.requests.RequestTradeModel;
import com.zerobudget.bookito.utils.Utils;

public class PopupInboxTradeAcc extends PopupInbox {
    private ImageView imgThumbnailBookTrade;
    private TextView txt;

    public PopupInboxTradeAcc(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }

    public PopupInboxTradeAcc(@NonNull Context context, View view) {
        super(context, view);
        imgThumbnailBookTrade = view.findViewById(R.id.img_book_trade);
        txt = view.findViewById(R.id.txt);
    }

    public void setUpInformationTrade(RequestTradeModel r) {
        Picasso.get().load(r.getThumbnailBookTrade()).into(this.imgThumbnailBookTrade);

        String txt = "";
        if (r.getReceiver().equals(Utils.USER_ID))
            txt = "Hai scelto il libro '" + r.getTitleBookTrade() + "' di " + r.getOtherUser().getFirstName() + " per lo scambio!";

        this.txt.setText(txt);
    }
}

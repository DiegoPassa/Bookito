package com.zerobudget.bookito.utils;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.Requests.RequestShareModel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PopupInbox extends MaterialAlertDialogBuilder {


    public PopupInbox(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }

    public PopupInbox(@NonNull Context context) {
        super(context);
    }

    /**
     * prepara il testo da visualizzare nella textview reputation*/
    public void setReputationMessage(TextView reputation, RequestModel request, Flag flag) {
        Number points = (Number) request.getOtherUser().getKarma().get("points");
        Number feedbacks = (Number) request.getOtherUser().getKarma().get("numbers");

        assert feedbacks != null;
        if (feedbacks.longValue() >= UserFlag.MIN_FEEDBACKS_FLAG) {
            assert points != null;
            double karma = Utils.truncateDoubleValue(points.doubleValue()/feedbacks.doubleValue(), 2);
            String reputationMessage = "Reputazione: " + karma + "/5.0\nFeedback:  " + feedbacks;

            switch (flag) {
                case GREEN_FLAG: {
                    reputation.setTextColor(ContextCompat.getColor(this.getContext(), R.color.green));
                    reputationMessage += "\nUtente affidabile!";
                    break;
                }
                case RED_FLAG: {
                    //è già di default settato a red
//                    reputation.setTextColor(com.google.android.material.R.color.design_default_color_error);
                    reputationMessage += "\nAttenzione! Utente inaffidabile!";
                    break;
                }
                default:
                    break;
            }
            reputation.setText(reputationMessage);
        } else {
            String txtReputation = "UTENTE NUOVO";
            reputation.setText(txtReputation);
            reputation.setTextColor(ContextCompat.getColor(this.getContext(), R.color.black));
        }
    }

    /**prepara il testo da visalizzare nella textview owner*/
    public void setUpUserFullName(TextView owner, RequestModel request) {
        String firstAndLastNameStr = request.getOtherUser().getFirstName() + " " + request.getOtherUser().getLastName();
        owner.setText(firstAndLastNameStr);
    }

    /**prepara il testo da visualizzare nella textview della data di fine prestito*/
    public void setUpDate(RequestShareModel request, TextView returnDate) {
        Date date = request.getDate();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "Data di restituzione:\n" + sdf.format(date);

        returnDate.setText(dateString);
        returnDate.setVisibility(View.VISIBLE);
    }

}

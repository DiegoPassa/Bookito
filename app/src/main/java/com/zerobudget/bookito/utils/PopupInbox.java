package com.zerobudget.bookito.utils;

import android.content.Context;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Requests.RequestModel;

import org.w3c.dom.Text;

public class PopupInbox extends MaterialAlertDialogBuilder {



    public PopupInbox(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }

    public PopupInbox(@NonNull Context context) {
        super(context);
    }

    public void setReputationMessage(TextView reputation, RequestModel request, Flag flag) {
        Number points = (Number) request.getOtherUser().getKarma().get("points");
        Number feedbacks = (Number) request.getOtherUser().getKarma().get("numbers");

        if (feedbacks.longValue() >= UserFlag.MIN_FEEDBACKS_FLAG) {
            String reputationMessage = "Reputazione: " + points.doubleValue() / feedbacks.doubleValue() + "/5.0 ( " + feedbacks  + " )\n";
            switch (flag) {
                case GREEN_FLAG: {
                    reputation.setTextColor(ContextCompat.getColor(this.getContext(), R.color.green));
                    reputationMessage += "Utente affidabile!";
                    break;
                }
                case RED_FLAG: {
                    //è già di default settato a red
//                    reputation.setTextColor(com.google.android.material.R.color.design_default_color_error);
                    reputationMessage += "Attenzione! Utente inaffidabile!";
                    break;
                }
                default: break;
            }
            reputation.setText(reputationMessage);
        } else {
            reputation.setText("UTENTE NUOVO");
            reputation.setTextColor(ContextCompat.getColor(this.getContext(), R.color.black));
        }
    }

}

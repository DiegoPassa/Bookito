package com.zerobudget.bookito.utils.popups;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.requests.RequestShareModel;
import com.zerobudget.bookito.utils.UserFlag;
import com.zerobudget.bookito.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PopupInbox extends MaterialAlertDialogBuilder {
    private Button confirmButton;
    private Button refuseButton;
    private TextView titlePopup;
    private TextView owner;
    private TextView ownerLocation;
    private TextView returnDate;
    private ImageView thumbnail;
    private TextView reputation;
    private TextView noteText;

    public PopupInbox(@NonNull Context context, int overrideThemeResId) {
        super(context, overrideThemeResId);
    }

    public PopupInbox(@NonNull Context context, View view) {
        super(context);
        loadPopupViewMembers(view);
    }

    /**
     * prepara il testo da visualizzare nella textview reputation, calcolandone il valore tramite i points e i feedback
     *
     * @param request: richiesta di riferimento per la visualizzazione della reputazione
     * @param flag:    flag usata per determinare il livello dell'utente (affidabile o non affidabile
     */
    public void setReputationMessage(RequestModel request, Flag flag) {
        Number points = (Number) request.getOtherUser().getKarma().get("points");
        Number feedbacks = (Number) request.getOtherUser().getKarma().get("numbers");

        assert feedbacks != null;
        if (feedbacks.longValue() >= UserFlag.MIN_FEEDBACKS_FLAG) {
            assert points != null;
            double karma = Utils.truncateDoubleValue(points.doubleValue() / feedbacks.doubleValue(), 2);
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
            this.reputation.setText(reputationMessage);
        } else {
            String txtReputation = "UTENTE NUOVO";
            this.reputation.setText(txtReputation);
            this.reputation.setTextAppearance(R.style.selected_filter_text);
        }
    }

    /**
     * prepara il testo da visualizzare nella textview della data di fine prestito
     */
    public void setUpDate(RequestShareModel request) {
        Date date = request.getDate();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "Data di restituzione:\n" + sdf.format(date);

        this.returnDate.setText(dateString);
        this.returnDate.setVisibility(View.VISIBLE);
    }

    /**
     * prepara il popup con le informazioni della richiesta
     *
     * @param r: richiesta di riferimeto
     */
    public void setUpInformation(RequestModel r) {
        String requestTypeStr = "Richiesta " + r.getType();
        this.titlePopup.setText(requestTypeStr);
        this.ownerLocation.setText(getContext().getString(R.string.user_location, r.getOtherUser().getTownship(), r.getOtherUser().getCity()));
        if (r.getNote().isEmpty()) {
            this.noteText.setVisibility(View.GONE);
        } else {
            this.noteText.setText(r.getNote());
        }
        Picasso.get().load(r.getThumbnail()).into(this.thumbnail);
        setUpUserFullName(r);
    }

    /**
     * prepara il testo da visalizzare nella textview owner
     *
     * @param request: richiesta di riferimento
     */
    private void setUpUserFullName(RequestModel request) {
        String strFirstAndLastNameStr;

        if (request.getSender().equals(Utils.USER_ID)) {
            if (request.getStatus().equals("accepted"))
                strFirstAndLastNameStr = "Da: " + Utils.CURRENT_USER.getFirstName() + " " + Utils.CURRENT_USER.getLastName() + "\na ";
            else
                strFirstAndLastNameStr = "A: ";
        } else {
            strFirstAndLastNameStr = "Da: ";
        }

        strFirstAndLastNameStr += request.getOtherUser().getFirstName() + " " + request.getOtherUser().getLastName();
        this.owner.setText(strFirstAndLastNameStr);
    }

    /**
     * assegna ai campi della classe le viste prelevate dalla view
     *
     * @param view: view di riferimento
     */
    private void loadPopupViewMembers(View view) {
        this.confirmButton = view.findViewById(R.id.acceptButton);
        this.refuseButton = view.findViewById(R.id.refuseButton);
        this.titlePopup = view.findViewById(R.id.title_popup);
        this.owner = view.findViewById(R.id.user);
        this.ownerLocation = view.findViewById(R.id.user_location);
        this.returnDate = view.findViewById(R.id.return_date);
        this.thumbnail = view.findViewById(R.id.imageView);
        this.reputation = view.findViewById(R.id.flag);
        this.noteText = view.findViewById(R.id.note_text);

        this.noteText.setMovementMethod(new ScrollingMovementMethod());
    }

    public Button getConfirmButton() {
        return confirmButton;
    }

    public Button getRefuseButton() {
        return refuseButton;
    }

    public void setTextConfirmButton(String txt) {
        this.confirmButton.setText(txt);
    }

    public void setTextRefuseButton(String txt) {
        this.refuseButton.setText(txt);
    }
}

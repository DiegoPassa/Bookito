package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.users.UserModel;

import java.util.ArrayList;

public class RequestSent_RecycleViewAdapter extends Inbox_RecycleViewAdapter{
    public RequestSent_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests) {
        super(ctx, requests);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel otherModel = requests.get(holder.getAdapterPosition()).getOtherUser();

        if (otherModel != null) {
            String other_user = requests.get(holder.getAdapterPosition())
                    .getOtherUser()
                    .getFirst_name() + " " + requests.get(holder.getAdapterPosition())
                                                .getOtherUser()
                                                .getLast_name();

            holder.user_name.setText(other_user);
        } else holder.user_name.setText("undefined");

        holder.title.setText(requests.get(holder.getAdapterPosition()).getTitle());
        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(holder.book_image);

        holder.request_selected.setOnClickListener(view -> {
            if (otherModel != null && holder.getAdapterPosition() != -1) {
                createNewContactDialog(position, holder, null);
            }
        });
    }

    @Override
    public void createNewContactDialog(int posiiton, ViewHolder holder, Flag flag) {
        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(context);
        View view = View.inflate(context, R.layout.popup, null);

        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();

        Button refuseButton = view.findViewById(R.id.refuseButton);
        Button acceptButton = view.findViewById(R.id.acceptButton);
        acceptButton.setVisibility(View.GONE);
        TextView titlePopup = view.findViewById(R.id.title_popup);
        TextView owner = view.findViewById(R.id.user);
        TextView ownerLocation = view.findViewById(R.id.user_location);
        TextView returnDate = view.findViewById(R.id.return_date);
        ImageView thumbnail = view.findViewById(R.id.imageView);

        String requestTypeStr = "Richiesta "+requests.get(holder.getAdapterPosition()).getType();
        titlePopup.setText(requestTypeStr);
        String firstAndLastNameStr = requests.get(holder.getAdapterPosition()).getOtherUser().getFirst_name()+" "+requests.get(holder.getAdapterPosition()).getOtherUser().getLast_name();
        owner.setText(firstAndLastNameStr);
        ownerLocation.setText(requests.get(holder.getAdapterPosition()).getOtherUser().getNeighborhood());
        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(thumbnail);
        refuseButton.setText("Annulla richiesta");

        refuseButton.setOnClickListener(view1 -> {
            if (holder.getAdapterPosition() != -1) {
                dialog.dismiss();

                AlertDialog.Builder newDialog = new MaterialAlertDialogBuilder(context);
                newDialog.setTitle("Conferma cancellazione");
                newDialog.setMessage(Html.fromHtml("Sei sicuro di voler <b>annullare</b> la richiesta per <b>" +
                                requests.get(holder.getAdapterPosition()).getTitle() + "</b>?",
                        Html.FROM_HTML_MODE_LEGACY));
                newDialog.setPositiveButton("SI", (dialogInterface, i) -> {
                    super.deleteRequest(requests.get(holder.getAdapterPosition()));
                    requests.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    dialogInterface.dismiss();
                    Toast.makeText(context, "Richiesta annullata correttamente!", Toast.LENGTH_LONG).show();
                });

                newDialog.setNegativeButton("NO", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });

                newDialog.show();
            }
        });


        dialog.show();

    }

}

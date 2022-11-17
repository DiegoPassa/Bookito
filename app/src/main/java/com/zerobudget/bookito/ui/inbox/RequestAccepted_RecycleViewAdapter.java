package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.UserFlag;
import com.zerobudget.bookito.utils.Utils;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class RequestAccepted_RecycleViewAdapter extends Inbox_RecycleViewAdapter{
    public RequestAccepted_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests) {
        super(ctx, requests);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel otherUser = requests.get(holder.getAdapterPosition()).getOtherUser();

        if (otherUser != null) {
            String nameOtherUser = otherUser.getFirst_name();
            String surnameOtherUser = otherUser.getLast_name();

            if (isCurrentUserReceiver(requests.get(holder.getAdapterPosition()))) {
                holder.user_name.setText(nameOtherUser + " " + surnameOtherUser);
            } else {
                holder.user_name.setText(Html.fromHtml("<b> ( TU ) -> </b>" +nameOtherUser + surnameOtherUser, Html.FROM_HTML_MODE_LEGACY));
            }
        }

        holder.title.setText(requests.get(holder.getAdapterPosition()).getTitle());
        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(holder.book_image);

        holder.user_gravatar.setHash(requests.get(holder.getAdapterPosition()).getOtherUser().getTelephone().hashCode());

        holder.request_selected.setOnClickListener(view1 -> {
            if (otherUser != null && holder.getAdapterPosition() != -1) {
                if (isCurrentUserReceiver(requests.get(holder.getAdapterPosition())))
                    createNewContactDialog(holder.getAdapterPosition(), holder, UserFlag.getFlagFromUser((Long) otherUser.getKarma().get("numbers"), (Long) otherUser.getKarma().get("points")));
                else createNewContactDialog(holder.getAdapterPosition(), holder, null);

            }
        });
    }

    @Override
    public void createNewContactDialog(int position, ViewHolder holder, Flag user) {

    }

    protected boolean isCurrentUserReceiver(RequestModel r) {
        return r.getReceiver().equals(Utils.USER_ID) ? true : false;
    }
}

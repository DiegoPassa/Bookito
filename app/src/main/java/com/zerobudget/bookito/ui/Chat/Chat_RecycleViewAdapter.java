package com.zerobudget.bookito.ui.Chat;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Chat.MessageModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class Chat_RecycleViewAdapter extends RecyclerView.Adapter<Chat_RecycleViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MessageModel> messages;
    private UserModel otherUser;

    public Chat_RecycleViewAdapter(Context context, ArrayList<MessageModel> messages, UserModel otherUser) {
        this.context = context;
        this.messages = messages;
        this.otherUser = otherUser;

        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "Ciao!", null));
        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "Mi mandi foto piedini?", null));
        messages.add(new MessageModel("PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", Utils.USER_ID, "Ciao! No, direi di no", null));
        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "Ma come no", null));
        messages.add(new MessageModel("PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", Utils.USER_ID, "Ma chi sei", null));
        messages.add(new MessageModel("PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", Utils.USER_ID, "Chi ti conosce", null));
        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "Sono marco e mi piacciono i piedi", null));
        messages.add(new MessageModel("PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", Utils.USER_ID, "Io sono giorgio e mi piacciono i treni", null));
        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "ciao giorgio,posso foto piedi?", null));
        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "Ti prego", null));
    }

    @NonNull
    @Override
    public Chat_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_holder, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.messageSent.setText(messages.get(position).getMessage());

        if (messages.get(position).getSender().equals(Utils.USER_ID)) {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.constraintLayout);
            constraintSet.clear(R.id.chat_profile_card_view, ConstraintSet.LEFT);
            constraintSet.clear(R.id.message_content, ConstraintSet.LEFT);
            constraintSet.connect(R.id.chat_profile_card_view, ConstraintSet.RIGHT, R.id.chat_layout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.message_content, ConstraintSet.RIGHT, R.id.chat_profile_card_view, ConstraintSet.LEFT, 0);
            constraintSet.applyTo(holder.constraintLayout);
            loadUserProfilePicture(UserModel.getCurrentUser(), holder.profileImg);
        } else {
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.constraintLayout);
            constraintSet.clear(R.id.chat_profile_card_view, ConstraintSet.RIGHT);
            constraintSet.clear(R.id.message_content, ConstraintSet.RIGHT);
            constraintSet.connect(R.id.chat_profile_card_view, ConstraintSet.LEFT, R.id.chat_layout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.message_content, ConstraintSet.LEFT, R.id.chat_profile_card_view, ConstraintSet.RIGHT, 0);
            constraintSet.applyTo(holder.constraintLayout);
            holder.messageSent.setBackgroundResource(R.drawable.enemy_message);
            loadUserProfilePicture(otherUser, holder.profileImg);

            if (isNightMode(context)) {
                holder.messageSent.setTextColor(R.color.black);
            }
        }
    }

    protected boolean isNightMode(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void loadUserProfilePicture(UserModel user, ImageView img) {
        if (user != null) {
            if (user.isHasPicture()) {

            } else {

            }
        }
    }


    @Override
    public int getItemCount() {
        return 10;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected ConstraintLayout constraintLayout;
        protected TextView messageSent;
        protected ImageView profileImg;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.chat_layout);
            messageSent = itemView.findViewById(R.id.message_content);
            profileImg = itemView.findViewById(R.id.small_profile_img);

        }
    }

}

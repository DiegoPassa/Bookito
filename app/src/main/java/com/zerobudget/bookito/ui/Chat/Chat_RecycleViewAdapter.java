package com.zerobudget.bookito.ui.Chat;

import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
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

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lelloman.identicon.view.ClassicIdenticonView;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Chat.MessageModel;
import com.zerobudget.bookito.models.Chat.MessageModelTrade;
import com.zerobudget.bookito.models.Chat.MessageModelWithImage;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Chat_RecycleViewAdapter extends RecyclerView.Adapter<Chat_RecycleViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MessageModel> messages;
    private UserModel otherUser;
    private String otherUserId;
    private Uri otherUserPic;

    private StorageReference storageRef;

    public Chat_RecycleViewAdapter(Context context, ArrayList<MessageModel> messages, UserModel otherUser, String otherUserId, Uri otherUserPic) {
        this.context = context;
        this.messages = messages;
        this.otherUser = otherUser;
        this.otherUserId = otherUserId;
        this.otherUserPic = otherUserPic;
        this.storageRef = FirebaseStorage.getInstance().getReference();


//        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "Ciao!", null));
//        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "Mi mandi foto piedini?", null));
//        messages.add(new MessageModel("PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", Utils.USER_ID, "Ciao! No, direi di no", null));
//        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "Ma come no", null));
//        messages.add(new MessageModel("PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", Utils.USER_ID, "Ma chi sei", null));
//        messages.add(new MessageModel("PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", Utils.USER_ID, "Chi ti conosce", null));
//        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "Sono marco e mi piacciono i piedi", null));
//        messages.add(new MessageModel("PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", Utils.USER_ID, "Io sono giorgio e mi piacciono i treni", null));
//        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "ciao giorgio,posso foto piedi?", null));
//        messages.add(new MessageModel(Utils.USER_ID, "PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "Ti prego", null));
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

        //viene visualizzato un messaggio di default con il libro scelto dal ricevente della richiesta in caso di scambio
        //in caso di prestito/regalo viene visualizzato solo il messaggio col libro scelto per il prestito/regalo
        if(messages.get(position) instanceof MessageModelTrade) {
            holder.book_thumbnail.setVisibility(View.VISIBLE);
            Picasso.get().load(((MessageModelTrade) messages.get(position)).getThumbnailBookTrade()).into(holder.book_thumbnail);
        }else if(messages.get(position) instanceof MessageModelWithImage){
            holder.book_thumbnail.setVisibility(View.VISIBLE);
            Picasso.get().load(((MessageModelWithImage) messages.get(position)).getThumbnailBookRequested()).into(holder.book_thumbnail);
        }else{
            holder.book_thumbnail.setVisibility(View.GONE);
        }

        holder.messagesDate.setText(messages.get(position).getMessageDate());

        //visualizzazione della data solo nel caso essa sia diversa da quella del messaggio precedente
        boolean isShowedDate = haveToShowDate(holder, position);



        if(messages.get(position).getStatus()!= null)
        if(messages.get(position).getStatus().equals("read"))
            holder.messageStauts.setImageResource(R.drawable.ic_baseline_done_all_16);
        else
            holder.messageStauts.setImageResource(R.drawable.ic_baseline_done_16);


        if(messages.get(position).getMessageTime() != null)
            holder.messageSentAt.setText(messages.get(holder.getAdapterPosition()).getMessageTime());

        if (messages.get(position).getSender().equals(Utils.USER_ID)) {
            holder.messageStauts.setVisibility(View.VISIBLE);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.constraintLayout);
            constraintSet.clear(R.id.chat_profile_card_view, ConstraintSet.LEFT);
            constraintSet.clear(R.id.message_content, ConstraintSet.LEFT);
            constraintSet.connect(R.id.chat_profile_card_view, ConstraintSet.RIGHT, R.id.chat_layout, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.message_content, ConstraintSet.RIGHT, R.id.chat_profile_card_view, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.book_thumbnail, ConstraintSet.RIGHT, R.id.chat_profile_card_view, ConstraintSet.LEFT, 0);

            if(isShowedDate) {
                constraintSet.connect(R.id.chat_profile_card_view, ConstraintSet.TOP, R.id.messages_date, ConstraintSet.BOTTOM, 0);
                constraintSet.connect(R.id.message_content, ConstraintSet.TOP, R.id.messages_date, ConstraintSet.BOTTOM, 0);
            }

            constraintSet.connect(R.id.message_sent_at, ConstraintSet.RIGHT, R.id.message_content, ConstraintSet.LEFT, 0);

            //se il current user apre la chat ha visibile la spunta che indica quando il messaggio è stato letto
            holder.messageSent.setPadding(70, 14, 40, 14);
            constraintSet.connect(R.id.message_status, ConstraintSet.LEFT, R.id.message_content, ConstraintSet.LEFT, 0);

            constraintSet.applyTo(holder.constraintLayout);
            loadUserProfilePicture(Utils.CURRENT_USER, holder, position);
            holder.messageSent.setBackgroundResource(R.drawable.message_view);

        } else {
            holder.messageStauts.setVisibility(View.GONE);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(holder.constraintLayout);
            constraintSet.clear(R.id.chat_profile_card_view, ConstraintSet.RIGHT);
            constraintSet.clear(R.id.message_content, ConstraintSet.RIGHT);
            constraintSet.connect(R.id.chat_profile_card_view, ConstraintSet.LEFT, R.id.chat_layout, ConstraintSet.LEFT, 0);
            constraintSet.connect(R.id.message_content, ConstraintSet.LEFT, R.id.chat_profile_card_view, ConstraintSet.RIGHT, 0);
            constraintSet.connect(R.id.book_thumbnail, ConstraintSet.LEFT, R.id.chat_profile_card_view, ConstraintSet.RIGHT, 0);

            if(isShowedDate) {
                constraintSet.connect(R.id.chat_profile_card_view, ConstraintSet.TOP, R.id.messages_date, ConstraintSet.BOTTOM, 0);
                constraintSet.connect(R.id.message_content, ConstraintSet.TOP, R.id.messages_date, ConstraintSet.BOTTOM, 0);
            }

            constraintSet.connect(R.id.message_sent_at, ConstraintSet.LEFT, R.id.message_content, ConstraintSet.RIGHT, 0);
            holder.messageSent.setPadding(40, 14, 40, 14);

            constraintSet.applyTo(holder.constraintLayout);
            holder.messageSent.setBackgroundResource(R.drawable.enemy_message);
            loadUserProfilePicture(otherUser, holder, position);
        }

    }

    private boolean haveToShowDate(ViewHolder holder, int position){
        if(position > 0 ){
            Date previousMsgDate = new Date();
            Date currentMsgDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            try {
                previousMsgDate = dateFormat.parse(messages.get(position-1).getMessageDate());
                currentMsgDate = dateFormat.parse(messages.get(position).getMessageDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            assert currentMsgDate != null;
            if(currentMsgDate.after(previousMsgDate)) {
                holder.messagesDate.setVisibility(View.VISIBLE);
                return true;
            }else {
                holder.messagesDate.setVisibility(View.GONE);
                return false;
            }
        }else{
            holder.messagesDate.setVisibility(View.VISIBLE);
            return true;
        }
    }

    protected boolean isNightMode(Context context) {
        int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void loadUserProfilePicture(UserModel user, ViewHolder holder, int position) {
        if (user.isHasPicture()) {
            holder.profileImg.setVisibility(View.VISIBLE);
            if (user == Utils.CURRENT_USER)
                Picasso.get().load(Utils.URI_PIC).into(holder.profileImg);
            else Picasso.get().load(otherUserPic).into(holder.profileImg);

        } else {
            holder.gravatarImg.setVisibility(View.VISIBLE);
            holder.gravatarImg.setHash(user.getTelephone().hashCode());
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ConstraintLayout constraintLayout;
        private final TextView messageSent;
        private final ImageView profileImg;
        private final ClassicIdenticonView gravatarImg;
        private final TextView messageSentAt;
        private final ImageView book_thumbnail;
        private final TextView messagesDate;
        private final ImageView messageStauts;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.chat_layout);
            messageSent = itemView.findViewById(R.id.message_content);
            profileImg = itemView.findViewById(R.id.small_profile_img);
            gravatarImg = itemView.findViewById(R.id.gravater_pic);
            messageSentAt = itemView.findViewById(R.id.message_sent_at);
            book_thumbnail = itemView.findViewById(R.id.book_thumbnail);
            messagesDate = itemView.findViewById(R.id.messages_date);
            messageStauts = itemView.findViewById(R.id.message_status);
        }
    }

}

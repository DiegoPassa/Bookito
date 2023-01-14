package com.zerobudget.bookito.ui.chat;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lelloman.identicon.view.ClassicIdenticonView;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.chat.MessageModel;
import com.zerobudget.bookito.models.chat.MessageModelTrade;
import com.zerobudget.bookito.models.chat.MessageModelWithImage;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Chat_RecycleViewAdapter extends RecyclerView.Adapter<Chat_RecycleViewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<MessageModel> messages;
    private final UserModel otherUser;
    private final String otherUserId;
    private final Uri otherUserPic;

    private final StorageReference storageRef;

    public Chat_RecycleViewAdapter(Context context, ArrayList<MessageModel> messages, UserModel otherUser, String otherUserId, Uri otherUserPic) {
        this.context = context;
        this.messages = messages;
        this.otherUser = otherUser;
        this.otherUserId = otherUserId;
        this.otherUserPic = otherUserPic;
        this.storageRef = FirebaseStorage.getInstance().getReference();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getSender().equals(Utils.USER_ID)) {
            return 1;
        } else {
            return 0;
        }
    }

    @NonNull
    @Override
    public Chat_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.message_sent, parent, false));
            case 0:
                return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.message_received, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.messageSent.setText(messages.get(position).getMessage());

        //viene visualizzato un messaggio di default con il libro scelto dal ricevente della richiesta in caso di scambio
        //in caso di prestito/regalo viene visualizzato solo il messaggio col libro scelto per il prestito/regalo
        if (messages.get(position) instanceof MessageModelTrade) {
            holder.book_thumbnail.setVisibility(View.VISIBLE);
            Picasso.get().load(((MessageModelTrade) messages.get(position)).getThumbnailBookTrade()).into(holder.book_thumbnail);
        } else if (messages.get(position) instanceof MessageModelWithImage) {
            holder.book_thumbnail.setVisibility(View.VISIBLE);
            Picasso.get().load(((MessageModelWithImage) messages.get(position)).getThumbnailBookRequested()).into(holder.book_thumbnail);
        } else {
            holder.book_thumbnail.setVisibility(View.GONE);
        }

        Date date = new Date(messages.get(holder.getAdapterPosition()).getMessageSentAt()*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String strMsgTime = sdf.format(date);
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String strMsgDate = sdf1.format(date);

        holder.messagesDate.setText(strMsgDate);

        //visualizzazione della data solo nel caso essa sia diversa da quella del messaggio precedente
        boolean isShowedDate = haveToShowDate(holder, position);

        if (messages.get(position).getSender().equals(Utils.USER_ID)) {
            loadUserProfilePicture(Utils.CURRENT_USER, holder);
            if (messages.get(position).getStatus() != null) {
                if (messages.get(position).getStatus().equals("read"))
                    holder.messageStatus.setImageResource(R.drawable.ic_baseline_done_all_16);
                else
                    holder.messageStatus.setImageResource(R.drawable.ic_baseline_done_16);
            }
        } else {
            loadUserProfilePicture(otherUser, holder);
        }

        if (messages.get(position).getMessageSentAt() != 0)
            holder.messageSentAt.setText(strMsgTime);

    }

    /**
     * controlla se la data del messaggio deve essere visualizzata oppure no
     * restituisce il risultato di tale controllo
     *
     * @param holder: oggetto contenente i binding dell'xml
     * @param position: posizione del messaggio all'interno della recycleview
     *
     * @return boolean: true se deve mostrare la data, false altrimenti
     */
    private boolean haveToShowDate(ViewHolder holder, int position) {

        if (position > 0) {
            Date previousMsgDate = new Date(messages.get(position-1).getMessageSentAt()*1000);
            Date currentMsgDate = new Date(messages.get(position).getMessageSentAt());

            if (currentMsgDate.after(previousMsgDate)) {
                holder.messagesDateCard.setVisibility(View.VISIBLE);
                return true;
            } else {
                holder.messagesDateCard.setVisibility(View.GONE);
                return false;
            }
        } else {
            holder.messagesDateCard.setVisibility(View.VISIBLE);
            return true;
        }
    }

    /**
     * carica le immagini di profilo dei due utenti
     *
     * @param user: modello dell'altro utente (non il current User)
     * @param holder: oggetto contenente i binding dell'xml
     */
    private void loadUserProfilePicture(UserModel user, ViewHolder holder) {
        if (user.isHasPicture()) {
            holder.profileImg.setVisibility(View.VISIBLE);
            if (user == Utils.CURRENT_USER)
                Picasso.get().load(Utils.URI_PIC).into(holder.profileImg);
           // else Picasso.get().load(otherUserPic).into(holder.profileImg);
            else{
                    storageRef.child("profile_pics/").child(otherUserId).getDownloadUrl().addOnSuccessListener(uri -> {
                        Picasso.get().load(uri).into(holder.profileImg);
                    }).addOnFailureListener(e -> {

                    });
            }
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

        private final TextView messageSent;
        private final ImageView profileImg;
        private final ClassicIdenticonView gravatarImg;
        private final TextView messageSentAt;
        private final ImageView book_thumbnail;
        private final TextView messagesDate;
        private final ImageView messageStatus;
        private final CardView messagesDateCard;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            messageSent = itemView.findViewById(R.id.message_body);
            profileImg = itemView.findViewById(R.id.small_profile_img);
            gravatarImg = itemView.findViewById(R.id.gravater_pic);
            messageSentAt = itemView.findViewById(R.id.message_time);
            book_thumbnail = itemView.findViewById(R.id.message_book_thumbnail);
            messagesDate = itemView.findViewById(R.id.messages_date);
            messageStatus = itemView.findViewById(R.id.message_read);
            messagesDateCard = itemView.findViewById(R.id.messages_date_card);
        }
    }

}

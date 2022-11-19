package com.zerobudget.bookito.ui.Chat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Chat.MessageModel;

import java.util.ArrayList;

public class Chat_RecycleViewAdapter extends RecyclerView.Adapter<Chat_RecycleViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<MessageModel> messages;
    private String senderImg;
    private String receiverImg;

    public Chat_RecycleViewAdapter(Context context, ArrayList<MessageModel> messages, String senderImg, String receiverImg) {
        this.context = context;
        this.messages = messages;
        this.senderImg = senderImg;
        this.receiverImg = receiverImg;

        for (int i = 0; i < 10; i++) {
            messages.add(new MessageModel("PkxM2m4pXZeEgdyPLUXq0qAdKLZ2", "EnKAs9DVThSOFRjDHalzWJCGZpZ2", "Posso foto piedini?", null));
        }
    }

    @NonNull
    @Override
    public Chat_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_holder, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d("CHATTT", "ENTRO NEL BIND");
        holder.messageSent.setText(messages.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected ConstraintLayout constraintLayout;
        protected TextView messageSent;
        ImageView profileImg;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.chat_layout);
            messageSent = itemView.findViewById(R.id.message_content);
            profileImg = itemView.findViewById(R.id.small_profile_img);

        }
    }

}

package com.zerobudget.bookito.ui.Chat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zerobudget.bookito.models.Chat.MessageModel;

import java.util.ArrayList;

public class Chat_RecycleViewAdapter extends RecyclerView.Adapter<Chat_RecycleViewAdapter.ViewHolder> {

    Context context;
    ArrayList<MessageModel> messages;

    public Chat_RecycleViewAdapter(Context context, ArrayList<MessageModel> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public Chat_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}

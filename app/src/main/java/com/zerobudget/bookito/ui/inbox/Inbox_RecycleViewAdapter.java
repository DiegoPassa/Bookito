package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.ui.Requests.RequestModel;
import com.zerobudget.bookito.ui.library.Book_RecycleViewAdapter;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class Inbox_RecycleViewAdapter extends RecyclerView.Adapter<Inbox_RecycleViewAdapter.ViewHolder>{

    private final Context context;
    ArrayList<RequestModel> requests;

    public Inbox_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests) {
        this.context = ctx;
        this.requests = requests;
    }

    @NonNull
    @Override
    public Inbox_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recycleview_requests, parent, false);

        return new Inbox_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //TODO GET MORE INFORMATION ABOUT THE REQUESTER (HIS NAME INSTEAD OF HIS ID)
        holder.user_name.setText(requests.get(position).getReceiver());
        Picasso.get().load(requests.get(position).getThumbnail()).into(holder.book_image);
        holder.title.setText(requests.get(position).getTitle());

    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView title;
        private final ConstraintLayout request_selected;
        private final TextView user_name;
        private final ImageView book_image;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.requestTitle);
            user_name = itemView.findViewById(R.id.requester_name);
            book_image= itemView.findViewById(R.id.book_image_request);
            request_selected = itemView.findViewById(R.id.request);
        }
    }
}
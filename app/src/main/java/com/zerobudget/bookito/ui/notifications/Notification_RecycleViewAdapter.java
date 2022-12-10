package com.zerobudget.bookito.ui.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.notification.NotificationModel;

import java.util.ArrayList;

public class Notification_RecycleViewAdapter extends RecyclerView.Adapter<Notification_RecycleViewAdapter.ViewHolder> {

    private ArrayList<NotificationModel> notification;
    private Context context;



    public Notification_RecycleViewAdapter(@NonNull Context context, ArrayList<NotificationModel> notification) {
        this.context = context;
        this.notification = notification;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.notification_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(notification.get(position).getTitle());
        holder.body.setText(notification.get(position).getBody());

        FirebaseStorage.getInstance().getReference().child("profile_pics/").listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                if (item.getName().equals(notification.get(position).getActionerId()))
                    item.getDownloadUrl().addOnSuccessListener(uri -> {
                        Picasso.get().load(uri).into(holder.actioner_image);
                    });
            }
        });
        Picasso.get().load(notification.get(position).getBook_thumb()).into(holder.book_victim);
    }


    @Override
    public int getItemCount() {
        return notification.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView body;
        private final ImageView book_victim;
        private final ConstraintLayout item_selected;
        private final ImageView actioner_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.not_title);
            body = itemView.findViewById(R.id.not_body);
            book_victim = itemView.findViewById(R.id.book_victim);
            item_selected = itemView.findViewById(R.id.item_selected);
            actioner_image = itemView.findViewById(R.id.actioner_image);
        }
    }

}

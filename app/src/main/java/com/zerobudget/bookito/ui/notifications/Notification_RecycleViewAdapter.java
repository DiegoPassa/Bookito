package com.zerobudget.bookito.ui.notifications;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.lelloman.identicon.view.ClassicIdenticonView;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.notification.NotificationModel;

import java.util.ArrayList;

public class Notification_RecycleViewAdapter extends RecyclerView.Adapter<Notification_RecycleViewAdapter.ViewHolder> {

    private ArrayList<NotificationModel> notification;
    private final Context context;

    public Notification_RecycleViewAdapter(@NonNull Context context, ArrayList<NotificationModel> notification) {
        this.context = context;
        this.notification = notification;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.recycleview_notifications, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(notification.get(position).getTitle());
        holder.body.setText(notification.get(position).getBody());

        if (notification.get(position).getTimestamp() != 0) {
            holder.date.setVisibility(View.VISIBLE);

            String elapsedTime = (String) DateUtils.getRelativeTimeSpanString(notification.get(position).getTimestamp() * 1000);

            holder.date.setText(elapsedTime);
        }

        FirebaseStorage.getInstance().getReference()
                .child("profile_pics/")
                .child(notification.get(position).getActionerId())
                .getDownloadUrl()
                .addOnSuccessListener(uri ->
                        Picasso.get().load(uri).into(holder.actioner_image)
                )
                .addOnFailureListener(runnable -> {
                    holder.actioner_gravatar.setHash(notification.get(position).getActioner().getTelephone().hashCode());
                    holder.actioner_gravatar.setVisibility(View.VISIBLE);
                    holder.actioner_image.setVisibility(View.GONE);
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
        private final ImageView actioner_image;
        private final ClassicIdenticonView actioner_gravatar;
        private final TextView date;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.not_title);
            body = itemView.findViewById(R.id.not_body);
            book_victim = itemView.findViewById(R.id.book_victim);
            actioner_image = itemView.findViewById(R.id.actioner_image);
            date = itemView.findViewById(R.id.date);
            actioner_gravatar = itemView.findViewById(R.id.user1_gravatar);
        }
    }

}

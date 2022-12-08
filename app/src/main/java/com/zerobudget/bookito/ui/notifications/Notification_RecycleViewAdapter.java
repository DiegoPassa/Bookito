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

import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Notification.NotificationModel;

import java.util.ArrayList;
import java.util.zip.Inflater;

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
    }

    @Override
    public int getItemCount() {
        return notification.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView body;
        private final ImageView imageActioner;
        private final ConstraintLayout item_selected;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.not_title);
            body = itemView.findViewById(R.id.not_body);
            imageActioner = itemView.findViewById(R.id.image_actioner);
            item_selected = itemView.findViewById(R.id.item_selected);
        }
    }

}

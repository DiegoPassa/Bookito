package com.zerobudget.bookito.ui.inbox;

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
import com.zerobudget.bookito.ui.Requests.RequestModel;
import com.zerobudget.bookito.ui.library.Book_RecycleViewAdapter;

import java.util.ArrayList;

public class Inbox_RecycleViewAdapter extends RecyclerView.Adapter<Inbox_RecycleViewAdapter.ViewHolder>{

    private final Context context;
    ArrayList<RequestModel> requests;

    public Inbox_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests) {
        this.context = ctx;
        requests.add(new RequestModel());
        requests.add(new RequestModel());
        requests.add(new RequestModel());
        requests.add(new RequestModel());
        requests.add(new RequestModel());
        requests.add(new RequestModel());
        requests.add(new RequestModel());
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
        holder.request.setText("SUPER TEST");
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView request;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            request = itemView.findViewById(R.id.requestTitle);
        }
    }
}

package com.zerobudget.bookito.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;

import java.util.ArrayList;

public class Search_RecycleViewAdapter extends RecyclerView.Adapter<Search_RecycleViewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<SearchResultsModel> results;

    public Search_RecycleViewAdapter(Context context, ArrayList<SearchResultsModel> bookModels) {
        this.context = context;
        this.results = bookModels;
    }

    @NonNull
    @Override
    public Search_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recycleview_search_results, parent, false);

        return new Search_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Search_RecycleViewAdapter.ViewHolder holder, int position) {
        Picasso.get().load(results.get(position).getBook().getThumbnail()).into(holder.thumbnail);
        holder.title.setText(results.get(position).getBook().getTitle());
        holder.author.setText(results.get(position).getBook().getAuthor());
        holder.book_owner.setText(results.get(position).getUser().getFirst_name());
        holder.type.setText(results.get(position).getBook().getType());
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Layout
        private final ImageView thumbnail;
        private final TextView title;
        private final TextView author;
        private final TextView book_owner;
        private final TextView type;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Binding from xml layout to Class
            thumbnail = itemView.findViewById(R.id.book_thumbnail);
            title = itemView.findViewById(R.id.book_title);
            author = itemView.findViewById(R.id.book_author);
            book_owner = itemView.findViewById(R.id.book_owner);
            type = itemView.findViewById(R.id.type);
        }
    }

}


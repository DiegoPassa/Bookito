package com.zerobudget.bookito.ui.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zerobudget.bookito.R;

import java.util.ArrayList;

public class Book_RecycleViewAdapter extends RecyclerView.Adapter<Book_RecycleViewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<BookModel> bookModels;

    public Book_RecycleViewAdapter(Context context, ArrayList<BookModel> bookModels) {
        this.context = context;
        this.bookModels = bookModels;
    }

    @NonNull
    @Override
    public Book_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycleview_row, parent, false);
        return new Book_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Book_RecycleViewAdapter.ViewHolder holder, int position) {
        holder.title.setText(bookModels.get(position).getTitle());
        holder.thumbnail.setImageResource(R.drawable.content2);
    }

    @Override
    public int getItemCount() {
        return bookModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final ImageView thumbnail;
        private final TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.book_thumbnail);
            title = itemView.findViewById(R.id.book_title);
        }
    }
}

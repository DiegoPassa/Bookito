package com.zerobudget.bookito.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zerobudget.bookito.R;
import com.zerobudget.bookito.ui.library.BookModel;

import java.util.ArrayList;

public class Search_RecycleViewAdapter extends RecyclerView.Adapter<Search_RecycleViewAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<BookModel> bookModels;

    public Search_RecycleViewAdapter(Context context, ArrayList<BookModel> bookModels) {
        this.context = context;
        this.bookModels = bookModels;
    }

    @NonNull
    @Override
    public Search_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recycleview_orizzontal, parent, false);

        return new Search_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Search_RecycleViewAdapter.ViewHolder holder, int position) {

        // holder.title.setText(bookModels.get(position).getTitle());

        // Picasso.get().load(bookModels.get(position).getThumbnail()).resize(110*4 , 160*4).into(holder.thumbnail);

        //TODO: visualizare l'owner del libro e il tipo (scambio, prestito, regalo)
        //if(this.kind.equals("search"))
    }

    @Override
    public int getItemCount() {
        return bookModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Layout
        // private final ImageView thumbnail;
        // private final TextView title;
        // ...

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Binding from xml layout to Class
            // thumbnail = itemView.findViewById(R.id.book_thumbnail);
            // title = itemView.findViewById(R.id. ...);
            // ...
        }
    }

}


package com.zerobudget.bookito.ui.inbox;

import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

public class RequestObserver extends ObservableList.OnListChangedCallback<ObservableList> {

    private RequestsReceived_RecycleViewAdapter adapter;
    private RecyclerView recyclerView;

    public RequestObserver(RequestsReceived_RecycleViewAdapter adapter, RecyclerView recyclerView) {
        this.adapter = adapter;
        this.recyclerView = recyclerView;
    }

    @Override
    public void onChanged(ObservableList sender) {
    }

    @Override
    public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
        adapter.notifyItemRangeChanged(positionStart, itemCount);
    }

    @Override
    public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
        adapter.notifyItemRangeInserted(positionStart, itemCount);
        recyclerView.scrollToPosition(0);
    }

    @Override
    public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
    }

    @Override
    public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
        adapter.notifyItemRangeRemoved(positionStart, itemCount);

    }
}

package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.utils.CustomLinearLayoutManager;
import com.zerobudget.bookito.utils.Utils;

public class RequestsReceivedFragment extends InboxFragment {

    private FragmentInboxBinding binding;

    private ProgressBar spinner;
    private TextView empty;

    private RequestsReceived_RecycleViewAdapter adapter;

    private RequestObserver observer;

    public RequestsReceivedFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinner = binding.progressBar;
        spinner.setVisibility(View.GONE);
        empty = binding.empty;
        recyclerView = binding.recycleViewInbox;

        binding.textView.setVisibility(View.VISIBLE);
        binding.chipGroup.setVisibility(View.GONE);

        //permette di ricaricare la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            // do nothing
        });

        return root;
    }

    protected void setUpRecycleView() {
        if (getView() != null) {
            adapter = new RequestsReceived_RecycleViewAdapter(this.getContext(), Utils.incomingRequests, empty);
            recyclerView.setAdapter(adapter);
            //recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            recyclerView.setLayoutManager(new CustomLinearLayoutManager(this.getContext()));
            observer = new RequestObserver(adapter, recyclerView);
            Utils.incomingRequests.addOnListChangedCallback(observer);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpRecycleView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        Utils.incomingRequests.removeOnListChangedCallback(observer);
    }

}
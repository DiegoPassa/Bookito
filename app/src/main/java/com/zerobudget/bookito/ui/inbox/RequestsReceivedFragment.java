package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.utils.CustomLinearLayoutManager;
import com.zerobudget.bookito.utils.Utils;

public class RequestsReceivedFragment extends InboxFragment {

    private FragmentInboxBinding binding;

    private FirebaseFirestore db;
    private ProgressBar spinner;
    private BadgeDrawable badge;
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
        empty = binding.empty;
        recyclerView = binding.recycleViewInbox;

        db = FirebaseFirestore.getInstance();

        BottomNavigationView navView = requireActivity().findViewById(R.id.nav_view);
        if(navView != null) {
            int menuItemId = navView.getMenu().getItem(0).getItemId();
            badge = navView.getOrCreateBadge(menuItemId);
        }

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
            observer = new RequestObserver(adapter);
            recyclerView.setAdapter(adapter);
            //recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            recyclerView.setLayoutManager(new CustomLinearLayoutManager(this.getContext()));

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
package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

public class RequestsReceivedFragment extends InboxFragment {

    private FragmentInboxBinding binding;

    private FirebaseFirestore db;
    private ProgressBar spinner;
    private BadgeDrawable badge;
    private TextView empty;

    private RequestsReceived_RecycleViewAdapter adapter;

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
        int menuItemId = navView.getMenu().getItem(0).getItemId();
        badge = navView.getOrCreateBadge(menuItemId);


        binding.textView.setVisibility(View.VISIBLE);
        binding.filterBar.setVisibility(View.INVISIBLE);

        //permette di ricaricare la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            // do nothing
        });

        return root;
    }

    protected void setUpRecycleView() {
        if (getView() != null) {
            requests.clear();
            adapter = new RequestsReceived_RecycleViewAdapter(this.getContext(), requests, empty);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        setUpRecycleView();
        getRequestsRealTime();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected void getRequestsRealTime() {
        spinner.setVisibility(View.VISIBLE);
        db.collection("requests")
                .whereEqualTo("receiver", Utils.USER_ID)
                .whereEqualTo("status", "undefined")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (value != null) {
                        if (value.isEmpty()) {
                            spinner.setVisibility(View.GONE);
                        }
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            Log.d("OOI", "getRequestsRealTime: " + dc.getDocument().toObject(RequestModel.class));
                            setRequestBadgeNumber(); //aggiorna anche il badge delle notifiche in real time
                            spinner.setVisibility(View.VISIBLE);
                            switch (dc.getType()) {
                                case ADDED:
                                    RequestModel addedRequestModel = RequestModel.getRequestModel(dc.getDocument().toObject(RequestModel.class).getType(), dc.getDocument());
                                    requests.add(dc.getNewIndex(), addedRequestModel);
                                    getUserByRequest(addedRequestModel, dc.getNewIndex());
                                    break;
                                case REMOVED:
                                    requests.remove(dc.getOldIndex());
                                    adapter.notifyItemRemoved(dc.getOldIndex());
                                    spinner.setVisibility(View.GONE);
                                    break;
                            }
                        }
                        Utils.toggleEmptyWarning(empty, Utils.EMPTY_INBOX, requests.size());
                    }
                });

    }

    protected void getUserByRequest(RequestModel r, int position) {
        db.collection("users").document(r.getSender())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel u = task.getResult().toObject(UserModel.class);
                        r.setOtherUser(u);
                        adapter.notifyItemInserted(position);
                        recyclerView.scrollToPosition(position);
                        spinner.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * preleva le richieste ricevute dal database e inserisce il numero nel badge della bottom bar*/
    private void setRequestBadgeNumber(){
        db.collection("requests")
                .whereEqualTo("status", "undefined")
                .whereEqualTo("receiver", Utils.USER_ID)
                .get().addOnCompleteListener(task -> {
                    int numReq = task.getResult().size();
                    badge.setNumber(numReq);
                    badge.setVisible(numReq > 0);
                });
    }

}
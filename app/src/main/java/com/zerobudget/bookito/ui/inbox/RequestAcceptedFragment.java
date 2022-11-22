package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class RequestAcceptedFragment extends Fragment {
    private FragmentInboxBinding binding;
    private ArrayList<RequestModel> requests = new ArrayList<>();
    private FirebaseFirestore db;

    private TextView emptyWarning;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        emptyWarning = binding.empty;

        db = FirebaseFirestore.getInstance();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            addRequestsOnPage(new ArrayList<>());
            requests = new ArrayList<>();
            binding.swipeRefreshLayout.setRefreshing(false);
            loadCompletedRequests();
        });

        if (requests.size() == 0)
            loadCompletedRequests();
        else {
            binding.progressBar.setVisibility(View.GONE);
            addRequestsOnPage(requests);
        }

        return root;
    }

    protected void loadCompletedRequests() {
//        requests = new ArrayList<>();
        binding.progressBar.setVisibility(View.VISIBLE);
        Task<QuerySnapshot> requestSent = db.collection("requests").whereEqualTo("status", "accepted")
                .whereEqualTo("sender", Utils.USER_ID).get();

        Task<QuerySnapshot> requestReceived = db.collection("requests").whereEqualTo("status", "accepted")
                .whereEqualTo("receiver", Utils.USER_ID).get();

        Tasks.whenAllSuccess(requestSent, requestReceived).addOnSuccessListener(list -> {
           QuerySnapshot queryRequestSent = (QuerySnapshot) list.get(0);
           QuerySnapshot queryRequestReceived = (QuerySnapshot) list.get(1);

           for (QueryDocumentSnapshot doc : queryRequestReceived) {
               requests.add(RequestModel.getRequestModel( (String) doc.get("type"), doc) );
           }

           for (QueryDocumentSnapshot doc : queryRequestSent) {
               requests.add(RequestModel.getRequestModel( (String) doc.get("type"), doc));
           }

           addOtherUsers(requests);

        });
    }

    private void addOtherUsers(ArrayList<RequestModel> req) {
        ArrayList<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (RequestModel r : requests) {
            if (Utils.USER_ID.equals(r.getReceiver())) {
                tasks.add(r.queryOtherUser(db, r.getSender())); //se user_id == sender allora prendo id di chi la manda
            } else {
                tasks.add(r.queryOtherUser(db, r.getReceiver())); //altrimenti prendo id di chi la riceve (in questo caso current user sta mandando la richiesta)
            }
        } //voglio ottenre informazioni sull'ALTRO utente
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            addRequestsOnPage(requests);
        });
    }

    private void addRequestsOnPage(ArrayList<RequestModel> req) {

        RecyclerView recyclerView = binding.recycleViewInbox;

        Inbox_RecycleViewAdapter adapter = new RequestAccepted_RecycleViewAdapter(this.getContext(), req, emptyWarning);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

    }
}

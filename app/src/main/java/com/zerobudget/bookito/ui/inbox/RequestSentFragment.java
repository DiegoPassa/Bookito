package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.util.Log;
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
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class RequestSentFragment extends Fragment {
    private FragmentInboxBinding binding;

    private FirebaseFirestore db;

    private ArrayList<RequestModel> requests;

    private TextView emptyWarning;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        emptyWarning = binding.empty;

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            requests = new ArrayList<>();
            getRequests(requests);
        });
        requests = new ArrayList<>();
        getRequests(requests);

        return root;
    }

    private void getRequests(ArrayList<RequestModel> req) {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("requests").whereEqualTo("sender", Utils.USER_ID)
                .whereEqualTo("status", "undefined")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            RequestModel r = RequestModel.getRequestModel((String) doc.get("type"), doc);
                            if (r != null) req.add(r);
                        }
                        addOtherUsers(req);
                    }
                });
    }


    protected void addOtherUsers(ArrayList<RequestModel> requests) {
        ArrayList<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (RequestModel r : requests) {
            tasks.add(r.queryOtherUser(db, r.getReceiver()));
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            addRequestsOnPage(requests);
        });
    }

    private void addRequestsOnPage(ArrayList<RequestModel> req) {
        if (getView() != null) {
            Log.d("SENDED", "SONO ENTRATO");
            RecyclerView recyclerView = binding.recycleViewInbox;

            Inbox_RecycleViewAdapter adapter = new RequestSent_RecycleViewAdapter(this.getContext(), req, emptyWarning);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            Utils.toggleEmptyWarning(emptyWarning, Utils.EMPTY_SEND, requests.size());
        }
    }

}

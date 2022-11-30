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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.Requests.RequestShareModel;
import com.zerobudget.bookito.models.Requests.RequestTradeModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class InboxFragment extends Fragment {

    private FragmentInboxBinding binding;

    private FirebaseFirestore db;
    private ProgressBar spinner;

    private TextView empty;

    private RecyclerView recyclerView;
    private ArrayList<RequestModel> requests = new ArrayList<>();

    private Inbox_RecycleViewAdapter adapter;

    public InboxFragment() {
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

        binding.textView.setVisibility(View.VISIBLE);
        binding.filterBar.setVisibility(View.INVISIBLE);
        // getRequests();


        //permette di ricaricare la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            getRequests();
        });

        return root;
    }

    protected void setUpRecycleView() {
        if (getView() != null) {
            adapter = new Inbox_RecycleViewAdapter(this.getContext(), requests, empty);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getRequests();

        setUpRecycleView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected void getRequests() {
        spinner.setVisibility(View.VISIBLE);
        db.collection("requests").whereEqualTo("receiver", Utils.USER_ID)
                .whereEqualTo("status", "undefined")
                .addSnapshotListener((value, error) -> {
                    if (error != null){
                        return;
                    }
                    if (value != null){
                        requests.clear();
                        for (DocumentSnapshot doc : value) {
                            switch ((String) doc.get("type")) {
                                case "Prestito": {
                                    requests.add(doc.toObject(RequestShareModel.class));
                                    break;
                                }
                                case "Scambio": {
                                    requests.add(doc.toObject(RequestTradeModel.class));
                                    break;
                                }
                                default: {
                                    requests.add(doc.toObject(RequestModel.class));
                                    break;
                                }
                            }
//                            requests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));

                        }
                        getUserByRequest(requests);
                    }
                });
    }

    protected void getUserByRequest(ArrayList<RequestModel> arr) {
        ArrayList<Task<DocumentSnapshot>> t = new ArrayList<>();
        for (RequestModel r : arr) {
            t.add(r.queryOtherUser(db, r.getSender()));
        }

        Tasks.whenAllSuccess(t).addOnCompleteListener(task -> {
            spinner.setVisibility(View.GONE);
            Utils.toggleEmptyWarning(empty, Utils.EMPTY_INBOX, requests.size());
            adapter.notifyDataSetChanged();
            Utils.toggleEmptyWarning(empty, Utils.EMPTY_INBOX, requests.size());
        });
    }

}
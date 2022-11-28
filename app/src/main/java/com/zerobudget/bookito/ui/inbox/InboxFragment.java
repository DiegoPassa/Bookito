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
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class InboxFragment extends Fragment {

    private FragmentInboxBinding binding;

    private FirebaseFirestore db;
    private ProgressBar spinner;

    private TextView empty;

    public InboxFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        spinner = root.findViewById(R.id.progressBar);
        empty = binding.empty;

        db = FirebaseFirestore.getInstance();

        binding.textView.setVisibility(View.VISIBLE);
        binding.filterBar.setVisibility(View.INVISIBLE);

        //permette di ricaricare la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            getRequests();
        });
        getRequests();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected ArrayList<Object> getRequests() {
        spinner.setVisibility(View.VISIBLE);
        db.collection("requests").whereEqualTo("receiver", Utils.USER_ID)
                .whereEqualTo("status", "undefined")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<RequestModel> req = new ArrayList<>();

                        QuerySnapshot result = task.getResult();

                        for (DocumentSnapshot o : result) {
                            String type = (String) o.get("type");
                            RequestModel r = RequestModel.getRequestModel(type, o);
                            Log.d("RICHIESTA", r.getTitle());
                            if (r != null) req.add(r);
                        }
                        // Log.d("COSASUCCEDE", ""+req.get(0).getThumbnail());
                        getUserByRequest(req);
                    }
                });
        return null;
    }

    protected void getUserByRequest(ArrayList<RequestModel> arr) {
        ArrayList<Task<DocumentSnapshot>> t = new ArrayList<>();
        for (RequestModel r : arr) {
            t.add(r.queryOtherUser(db, r.getSender()));
        }

        Tasks.whenAllComplete(t).addOnCompleteListener(task -> {
            spinner.setVisibility(View.GONE);
            addRequestsOnPage(arr);
        });
    }

    protected void addRequestsOnPage(ArrayList<RequestModel> requests) {
        if (getView() != null) {

            RecyclerView recyclerView = binding.recycleViewInbox;
            Inbox_RecycleViewAdapter adapter = new Inbox_RecycleViewAdapter(this.getContext(), requests, empty);

            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

            Utils.toggleEmptyWarning(empty, Utils.EMPTY_INBOX, requests.size());
        }
    }

}
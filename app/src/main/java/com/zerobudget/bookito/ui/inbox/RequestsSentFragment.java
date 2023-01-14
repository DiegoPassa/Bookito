package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.utils.CustomLinearLayoutManager;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class RequestsSentFragment extends InboxFragment {
    private FragmentInboxBinding binding;

    private FirebaseFirestore db;

    private TextView emptyWarning;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();

        binding.textView.setVisibility(View.VISIBLE);
        binding.chipGroup.setVisibility(View.GONE);

        emptyWarning = binding.empty;

        //to fix error E/Recyclerview: No Adapter Attached; Skipping Layout
        RecyclerView recyclerView = binding.recycleViewInbox;
        RequestsReceived_RecycleViewAdapter adapter = new RequestsSent_RecycleViewAdapter(this.getContext(), new ArrayList<>(), emptyWarning);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        //end of fixing it

        Utils.toggleEmptyWarning(emptyWarning, Utils.EMPTY_SEND, requests.size());

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            requests = new ArrayList<>();
            getRequests(requests);
        });
        requests = new ArrayList<>();
        getRequests(requests);

        return root;
    }

    /**
     * preleva le richieste dell'utente corrente dal database
     *
     * @param req: arraylist di richieste nel quale inserire le richieste trobate
     */
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


    /**
     * aggiunge le informazioni dell'altro utente alle richieste
     *
     * @param requests: array list delle richieste di riferimento
     */
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

    /**
     * aggiunge le richiestealla pagina
     *
     * @param req: array list di richieste da visualizzare
     */
    private void addRequestsOnPage(ArrayList<RequestModel> req) {
        if (getView() != null) {
            RecyclerView recyclerView = binding.recycleViewInbox;
            RequestsReceived_RecycleViewAdapter adapter = new RequestsSent_RecycleViewAdapter(this.getContext(), req, emptyWarning);
            recyclerView.setAdapter(adapter);
            //recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
            recyclerView.setLayoutManager(new CustomLinearLayoutManager(this.getContext()));
            Utils.toggleEmptyWarning(emptyWarning, Utils.EMPTY_SEND, requests.size());
        }
    }

}

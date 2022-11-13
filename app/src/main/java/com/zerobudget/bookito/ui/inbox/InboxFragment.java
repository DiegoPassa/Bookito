package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class InboxFragment extends Fragment {

    private FragmentInboxBinding binding;
    private ArrayList<RequestModel> requests;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public InboxFragment() {}


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //permette di ricaricare la pagina con lo swipe verso il basso
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            binding.swipeRefreshLayout.setRefreshing(false);
            addRequestsOnPage(new ArrayList<>());
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
        //TODO I TIMESTAMP NON POSSONO ESSERE CASTATI A STRING, QUINDI FARE UNA FUNZIONE PER CONVERTIRE TIMESTAMP A STRING O PER CONTROLLARNE I VALORI
        FirebaseUser currentUs = mAuth.getCurrentUser();
        //            String id = currentUs.getUid();
        //TODO: cambiare id quando abbiamo un current user
        //VISUALIZZA RICHIESTE CHE L'UTENTE ATTUALE HA RICEVUTO
        binding.progressBar.setVisibility(View.VISIBLE);
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
        int index = 0;
        for (RequestModel r : arr) {
            t.add(r.queryOtherUser(db, r.getSender()));
        }

        Tasks.whenAllComplete(t).addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            addRequestsOnPage(arr);
        });
    }

    protected void addRequestsOnPage(ArrayList<RequestModel> requests) {
        RecyclerView recyclerView = binding.recycleViewInbox;

        Inbox_RecycleViewAdapter adapter = new Inbox_RecycleViewAdapter(this.getContext(), requests, "RequestFragment");

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }

}
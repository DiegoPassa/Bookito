package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.requests.RequestTradeModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class RequestsAcceptedFragment extends InboxFragment {
    private FragmentInboxBinding binding;
    private ArrayList<RequestModel> requests = new ArrayList<>();
    private ArrayList<RequestModel> requestsSent = new ArrayList<>();
    private ArrayList<RequestModel> requestsReceived = new ArrayList<>();

    private FirebaseFirestore db;

    private TextView emptyWarning;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        /*int textSize = 14;
        int clicked_textSize = 17;*/

        binding.textView.setVisibility(View.GONE);
        binding.chipGroup.setVisibility(View.VISIBLE);
        /*binding.filterBar.setVisibility(View.VISIBLE);

        binding.seeAllReq.setTextSize(clicked_textSize);
        binding.seeAllReq.setTextAppearance(R.style.selected_filter_text);*/

        emptyWarning = binding.empty;

        //to fix error E/Recyclerview: No Adapter Attached; Skipping Layout
        RecyclerView recyclerView = binding.recycleViewInbox;
        RequestsAccepted_RecycleViewAdapter adapter = new RequestsAccepted_RecycleViewAdapter(this.getContext(), new ArrayList<>(), emptyWarning);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        //end of fixing it

        db = FirebaseFirestore.getInstance();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            /*binding.seeAllReq.setTextSize(clicked_textSize);
            binding.currentUsrReq.setTextSize(textSize);
            binding.otherUsrReq.setTextSize(textSize);
            binding.seeAllReq.setTextAppearance(R.style.selected_filter_text);
            binding.currentUsrReq.setTextAppearance(R.style.filter_text);
            binding.otherUsrReq.setTextAppearance(R.style.filter_text);*/

            addRequestsOnPage(new ArrayList<>());
            requests = new ArrayList<>();
            requestsSent = new ArrayList<>();
            requestsReceived = new ArrayList<>();
            binding.swipeRefreshLayout.setRefreshing(false);
            loadCompletedRequests();
        });

        //notifica l'adapter della recycle view quando avviene una modifica
        //dall'interno della chat ( menu a tendina )
        Bundle args = getArguments();
        if(args !=null){
            RequestsAccepted_RecycleViewAdapter adapteer = new RequestsAccepted_RecycleViewAdapter(this.getContext(), requests, emptyWarning);

            switch (args.getString("type")){
                case "changed":
                    adapteer.notifyItemChanged(args.getInt("position"));
                    break;
                case "removed":
                    adapteer.notifyItemRemoved(args.getInt("position"));
                    break;
                default:
                    break;
            }
        }


        if (requests.size() == 0)
            loadCompletedRequests();
        else {
            binding.progressBar.setVisibility(View.GONE);
            addRequestsOnPage(requests);
        }


        //tutte le richieste, inivate e ricevute
        binding.chipSeeAll.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());

        /*binding.seeAllReq.setOnClickListener(view -> {
            binding.seeAllReq.setTextSize(clicked_textSize);
            binding.currentUsrReq.setTextSize(textSize);
            binding.otherUsrReq.setTextSize(textSize);
            binding.seeAllReq.setTextAppearance(R.style.selected_filter_text);
            binding.currentUsrReq.setTextAppearance(R.style.filter_text);
            binding.otherUsrReq.setTextAppearance(R.style.filter_text);

            addRequestsOnPage(requests);
        });*/

        //richieste inviate, libri da ricevere
        binding.chipToReceive.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());

        /*binding.currentUsrReq.setOnClickListener(view -> {
            binding.currentUsrReq.setTextSize(clicked_textSize);
            binding.seeAllReq.setTextSize(textSize);
            binding.otherUsrReq.setTextSize(textSize);
            binding.currentUsrReq.setTextAppearance(R.style.selected_filter_text);
            binding.seeAllReq.setTextAppearance(R.style.filter_text);
            binding.otherUsrReq.setTextAppearance(R.style.filter_text);

            addRequestsOnPage(requestsSent);
        });*/

        //richieste ricevute, libri da consegnare
        binding.chipToGive.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());

        /*
        binding.otherUsrReq.setOnClickListener(view -> {
            binding.otherUsrReq.setTextSize(clicked_textSize);
            binding.seeAllReq.setTextSize(textSize);
            binding.currentUsrReq.setTextSize(textSize);
            binding.otherUsrReq.setTextAppearance(R.style.selected_filter_text);
            binding.seeAllReq.setTextAppearance(R.style.filter_text);
            binding.currentUsrReq.setTextAppearance(R.style.filter_text);

            addRequestsOnPage(requestsReceived);
        });*/

        return root;
    }

    /**
     * visualizza le richieste in base al tipo selezionato*/
    private void checkAllChips(){
        if(binding.chipSeeAll.isChecked()){
            addRequestsOnPage(requests);
        }

        if(binding.chipToReceive.isChecked()){
            addRequestsOnPage(requestsSent);
        }

        if(binding.chipToGive.isChecked()){
            addRequestsOnPage(requestsReceived);
        }
    }

    /**
     * carica le richieste accettate dell'utente corrente*/
    protected void loadCompletedRequests() {
//        requests = new ArrayList<>();
        binding.progressBar.setVisibility(View.VISIBLE);
        Task<QuerySnapshot> requestSent = db.collection("requests").whereEqualTo("status", "accepted")
                .whereEqualTo("sender", Utils.USER_ID).get();

        Task<QuerySnapshot> requestReceived = db.collection("requests").whereEqualTo("status", "accepted")
                .whereEqualTo("receiver", Utils.USER_ID).get();

        Task<QuerySnapshot> requestSentOnGoing = db.collection("requests").whereEqualTo("status", "ongoing")
                .whereEqualTo("sender", Utils.USER_ID).get();

        Task<QuerySnapshot> requestReceivedOnGoing = db.collection("requests").whereEqualTo("status", "ongoing")
                .whereEqualTo("receiver", Utils.USER_ID).get();

        Tasks.whenAllSuccess(requestSent, requestReceived, requestSentOnGoing, requestReceivedOnGoing).addOnSuccessListener(list -> {
            QuerySnapshot queryRequestSent = (QuerySnapshot) list.get(0);
            QuerySnapshot queryRequestReceived = (QuerySnapshot) list.get(1);
            QuerySnapshot queryRequestSentOnGoing = (QuerySnapshot) list.get(2);
            QuerySnapshot queryRequestReceivedOnGoing = (QuerySnapshot) list.get(3);

            for (QueryDocumentSnapshot doc : queryRequestReceived) {
                //salvo le richieste ricevute per poterle filtrare
                requestsReceived.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                requests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
            }

            for (QueryDocumentSnapshot doc : queryRequestSent) {
                //salvo le richieste inviate per poterle filtrare
                requestsSent.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                requests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
            }

            for (QueryDocumentSnapshot doc : queryRequestSentOnGoing) {
                //salvo le richieste inviate per poterle filtrare
                requestsSent.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                requests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
            }

            for (QueryDocumentSnapshot doc : queryRequestReceivedOnGoing) {
                //salvo le richieste inviate per poterle filtrare
                requestsReceived.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                requests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
            }


            for (int i = 0; i < requests.size(); i++)
                if (requests.get(i) instanceof RequestTradeModel)

            addOtherUsers(requestsSent, false);
            addOtherUsers(requestsReceived, false);
            addOtherUsers(requests, true);

        });
    }

    private void addOtherUsers(ArrayList<RequestModel> req, boolean all) {
        ArrayList<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (RequestModel r : req) {
            if (Utils.USER_ID.equals(r.getReceiver())) {
                //se user_id == sender allora prendo id di chi la manda
                tasks.add(r.queryOtherUser(db, r.getSender()));
            } else {
                //altrimenti prendo id di chi la riceve (in questo caso current user sta mandando la richiesta)
                tasks.add(r.queryOtherUser(db, r.getReceiver()));
            }
        }
        //voglio ottenre informazioni sull'ALTRO utente
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(task -> {
            binding.progressBar.setVisibility(View.GONE);

                checkAllChips();
                //addRequestsOnPage(req);

        });
    }

    /**
     * permette la viualizzazione delle richieste sulla pagina
     *
     * @param req: array di richieste da visualizzare*/
    private void addRequestsOnPage(ArrayList<RequestModel> req) {

        RecyclerView recyclerView = binding.recycleViewInbox;

        RequestsAccepted_RecycleViewAdapter adapter = new RequestsAccepted_RecycleViewAdapter(this.getContext(), req, emptyWarning);

        recyclerView.setAdapter(adapter);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

    }

}


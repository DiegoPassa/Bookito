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
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class RequestsAcceptedFragment extends InboxFragment {
    private FragmentInboxBinding binding;
    private ArrayList<RequestModel> arrRequests = new ArrayList<>();
    private ArrayList<RequestModel> arrRequestsSent = new ArrayList<>();
    private ArrayList<RequestModel> arrRequestsReceived = new ArrayList<>();
    private ArrayList<RequestModel> arrRequestsTrade = new ArrayList<>();

    private ArrayList<RequestModel> arrRequestsSentOngoing = new ArrayList<>();
    private ArrayList<RequestModel> arrRequestsReceivedOngoing = new ArrayList<>();


    private FirebaseFirestore db;

    private TextView emptyWarning;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.textView.setVisibility(View.GONE);
        binding.chipGroup.setVisibility(View.VISIBLE);

        emptyWarning = binding.empty;

        //to fix error E/Recyclerview: No Adapter Attached; Skipping Layout
        RecyclerView recyclerView = binding.recycleViewInbox;
        RequestsAccepted_RecycleViewAdapter adapter = new RequestsAccepted_RecycleViewAdapter(this.getContext(), new ArrayList<>(), emptyWarning);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        //end of fixing it

        db = FirebaseFirestore.getInstance();

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {

            addRequestsOnPage(new ArrayList<>());
            arrRequests = new ArrayList<>();
            arrRequestsSent = new ArrayList<>();
            arrRequestsReceived = new ArrayList<>();
            arrRequestsTrade = new ArrayList<>();
            arrRequestsSentOngoing = new ArrayList<>();
            arrRequestsReceivedOngoing = new ArrayList<>();
            binding.swipeRefreshLayout.setRefreshing(false);
            loadCompletedRequests();
        });

        //notifica l'adapter della recycle view quando avviene una modifica
        //dall'interno della chat ( menu a tendina )
        Bundle args = getArguments();
        if (args != null) {
            RequestsAccepted_RecycleViewAdapter adapteer = new RequestsAccepted_RecycleViewAdapter(this.getContext(), arrRequests, emptyWarning);

            switch (args.getString("type")) {
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


        if (arrRequests.size() == 0)
            loadCompletedRequests();
        else {
            binding.progressBar.setVisibility(View.GONE);
            addRequestsOnPage(arrRequests);
        }


        //tutte le richieste, inivate e ricevute
        binding.chipSeeAll.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());

        binding.chipToReceive.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());
        binding.chipToGive.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());
        binding.chipToTrade.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());

        binding.chipToGiveOngoing.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());
        binding.chipToReceiveOngoing.setOnCheckedChangeListener((compoundButton, b) -> checkAllChips());


        return root;
    }

    /**
     * visualizza le richieste in base al tipo selezionato
     */
    private void checkAllChips() {
        if (binding.chipSeeAll.isChecked()) {
            addRequestsOnPage(arrRequests);
        }

        if (binding.chipToReceive.isChecked()) {
            addRequestsOnPage(arrRequestsSent);
        }

        if (binding.chipToGive.isChecked()) {
            addRequestsOnPage(arrRequestsReceived);
        }

        if (binding.chipToTrade.isChecked()) {
            addRequestsOnPage(arrRequestsTrade);
        }

        if (binding.chipToGiveOngoing.isChecked()) {
            addRequestsOnPage(arrRequestsSentOngoing);
        }

        if (binding.chipToReceiveOngoing.isChecked()) {
            addRequestsOnPage(arrRequestsReceivedOngoing);
        }
    }

    /**
     * carica le richieste accettate dell'utente corrente
     */
    protected void loadCompletedRequests() {
//        requests = new ArrayList<>();
        binding.progressBar.setVisibility(View.VISIBLE);
        Task<QuerySnapshot> requestSentTrade = db.collection("requests").whereEqualTo("status", "accepted")
                .whereEqualTo("sender", Utils.USER_ID).whereEqualTo("type", "Scambio").get();

        Task<QuerySnapshot> requestReceivedTrade = db.collection("requests").whereEqualTo("status", "accepted")
                .whereEqualTo("receiver", Utils.USER_ID).whereEqualTo("type", "Scambio").get();

        Task<QuerySnapshot> requestSent = db.collection("requests").whereEqualTo("status", "accepted")
                .whereEqualTo("sender", Utils.USER_ID).whereNotEqualTo("type", "Scambio").get();

        Task<QuerySnapshot> requestReceived = db.collection("requests").whereEqualTo("status", "accepted")
                .whereEqualTo("receiver", Utils.USER_ID).whereNotEqualTo("type", "Scambio").get();

        Task<QuerySnapshot> requestSentOnGoing = db.collection("requests").whereEqualTo("status", "ongoing")
                .whereEqualTo("sender", Utils.USER_ID).get();

        Task<QuerySnapshot> requestReceivedOnGoing = db.collection("requests").whereEqualTo("status", "ongoing")
                .whereEqualTo("receiver", Utils.USER_ID).get();

        Tasks.whenAllSuccess(requestSent, requestReceived, requestSentOnGoing, requestReceivedOnGoing, requestSentTrade, requestReceivedTrade).addOnSuccessListener(list -> {
            QuerySnapshot queryRequestSent = (QuerySnapshot) list.get(0);
            QuerySnapshot queryRequestReceived = (QuerySnapshot) list.get(1);
            QuerySnapshot queryRequestSentOnGoing = (QuerySnapshot) list.get(2);
            QuerySnapshot queryRequestReceivedOnGoing = (QuerySnapshot) list.get(3);
            QuerySnapshot queryRequestSentTrade = (QuerySnapshot) list.get(4);
            QuerySnapshot queryRequestReceivedTrade = (QuerySnapshot) list.get(5);


            for (QueryDocumentSnapshot doc : queryRequestReceived) {
                //salvo le richieste ricevute per poterle filtrare
                arrRequestsReceived.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                arrRequests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
            }

            for (QueryDocumentSnapshot doc : queryRequestSent) {
                //salvo le richieste inviate per poterle filtrare
                arrRequestsSent.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                arrRequests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
            }

            for (QueryDocumentSnapshot doc : queryRequestSentOnGoing) {
                //salvo le richieste inviate per poterle filtrare
                //arrRequestsSent.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                arrRequestsSentOngoing.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                arrRequests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
            }

            for (QueryDocumentSnapshot doc : queryRequestReceivedOnGoing) {
                //salvo le richieste ricevute per poterle filtrare
                //arrRequestsReceived.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                arrRequestsReceivedOngoing.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                arrRequests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
            }

            //se è una richiesta di scambio l'utente deve sia dare che ricevere un libro
            for (QueryDocumentSnapshot doc : queryRequestSentTrade) {
                arrRequestsTrade.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                arrRequests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
            }

            for (QueryDocumentSnapshot doc : queryRequestReceivedTrade) {
                arrRequestsTrade.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
                arrRequests.add(RequestModel.getRequestModel((String) doc.get("type"), doc));
            }

            addOtherUsers(arrRequests);
            addOtherUsers(arrRequestsSent);
            addOtherUsers(arrRequestsReceived);
            addOtherUsers(arrRequestsTrade);
            addOtherUsers(arrRequestsSentOngoing);
            addOtherUsers(arrRequestsReceivedOngoing);
        });
    }

    private void addOtherUsers(ArrayList<RequestModel> req) {
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
     * @param req: array di richieste da visualizzare
     */
    private void addRequestsOnPage(ArrayList<RequestModel> req) {

        RecyclerView recyclerView = binding.recycleViewInbox;
        RequestsAccepted_RecycleViewAdapter adapter = new RequestsAccepted_RecycleViewAdapter(this.getContext(), req, emptyWarning);
        recyclerView.setAdapter(adapter);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

    }

}


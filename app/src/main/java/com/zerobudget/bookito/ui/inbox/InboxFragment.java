package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.ui.Requests.RequestModel;
import com.zerobudget.bookito.ui.Requests.RequestShareModel;
import com.zerobudget.bookito.ui.Requests.RequestTradeModel;
import com.zerobudget.bookito.ui.library.Book_RecycleViewAdapter;
import com.zerobudget.bookito.ui.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class InboxFragment extends Fragment {

    private FragmentInboxBinding binding;
    private ArrayList<RequestModel> requests;

    FirebaseFirestore db;
    FirebaseAuth mAuth;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        InboxViewModel inboxViewModel = new ViewModelProvider(this).get(InboxViewModel.class);

        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

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
        //TODO I TIMESTAMP NON POSSONO ESSERE CASTATI A STRING, QUINDI FARE UNA FUNZIONE PER CONVERTIRE TIMESTAMP A STRING O PER CONTROLLARNE I VALORI
        FirebaseUser currentUs = mAuth.getCurrentUser();
        //            String id = currentUs.getUid();
        //TODO: cambiare id quando abbiamo un current user
        //VISUALIZZA RICHIESTE CHE L'UTENTE ATTUALE HA RICEVUTO
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("requests").whereEqualTo("receiver", Utils.USER_ID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<RequestModel> req = new ArrayList<>();

                        QuerySnapshot result = task.getResult();

                        for (DocumentSnapshot o : result) {
                            String type = (String) o.get("type");
                            RequestModel r = getRequestModel(type, o);
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
            t.add(db.collection("users").document(r.getSender())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            UserModel u = UserModel.getUserFromDocument(task.getResult());
                            r.setSenderModel(u);
                        }
                    }));
        }

        Tasks.whenAllComplete(t).addOnCompleteListener(task -> {
            binding.progressBar.setVisibility(View.GONE);
            addRequestsOnPage(arr);
        });
    }

    protected void addRequestsOnPage(ArrayList<RequestModel> requests) {
        RecyclerView recyclerView = binding.recycleViewInbox;

        Inbox_RecycleViewAdapter adapter = new Inbox_RecycleViewAdapter(this.getContext(), requests);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }

    protected RequestModel getRequestModel(String type, DocumentSnapshot o) {
        Log.d("SCAMBIO", ""+o);
        switch (type) {
            case ("Regalo"): {
                return new RequestModel((String) o.get("book"), (String) o.get("sender"), (String) o.get("receiver"), (String) o.get("status"), (String)o.get("thumbnail"), (String) o.get("type"), (String)o.get("title"), (String)o.getId());
            }
            case("Prestito"): {
                return new RequestShareModel((String) o.get("book"), (String) o.get("sender"), (String) o.get("receiver"), (String) o.get("status"), (String)o.get("thumbnail"), (String)o.get("type"), (String) o.get("title"), (String)o.getId(), new Date((String)o.get("date")));
            }

            case("Scambio"): {
                return new RequestTradeModel((String) o.get("book"), (String) o.get("sender"), (String) o.get("receiver"), (String) o.get("status"), (String) o.get("thumbnail"), (String)o.get("type"), (String) o.get("title"), (String)o.getId(), (String) o.get("requested_book"));
            }
        }
        return null;
    }
}
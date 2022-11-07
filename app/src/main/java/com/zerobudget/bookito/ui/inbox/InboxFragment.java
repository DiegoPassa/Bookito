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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.ui.Requests.RequestModel;
import com.zerobudget.bookito.ui.library.Book_RecycleViewAdapter;

import java.util.ArrayList;
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

//        RecyclerView recyclerView = binding.recycleViewInbox;
//        ArrayList<RequestModel> a = new ArrayList<RequestModel>();
//        a.add(new RequestModel());
//        a.add(new RequestModel());
//        Inbox_RecycleViewAdapter adapter = new Inbox_RecycleViewAdapter(this.getContext(), a);
//
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        getRequests();


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected ArrayList<Object> getRequests() {
        FirebaseUser currentUs = mAuth.getCurrentUser();
        if (true) {
//            String id = currentUs.getUid();
            db.collection("requests").whereEqualTo("recipient", "AZLYEN9WqTOVXiglkPJT")
                    .get()
                    .addOnCompleteListener(task -> {
                        //TODO CAMBIARE DA ARRAY DI OBJECT AD ARRAY DI REQUEST MODEL
                        ArrayList<Object> req = new ArrayList<>();

                        QuerySnapshot result = task.getResult();

                        for (DocumentSnapshot o : result) {
                            //TODO CONTROLLARE ANCHE IL TIPO DI FLAG ED IN VASE A QUELLO AGGIUNGERE UN REQUEST MODEL DIVERSO
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("recipient", (String) o.get("recipient"));
                            map.put("requester", (String)o.get("requester"));
                            map.put("book", (String)o.get("book"));
                            map.put("status", (String)o.get("status"));
                            map.put("flag", (String)o.get("flag"));
                            req.add(map);
                        }
                        Log.d("REQUEST", ""+req);


                    });
        }
        return null;
    }
}
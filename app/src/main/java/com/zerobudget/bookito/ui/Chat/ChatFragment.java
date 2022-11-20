package com.zerobudget.bookito.ui.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.ChatFragmentBinding;
import com.zerobudget.bookito.models.Chat.MessageModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.ui.inbox.Inbox_RecycleViewAdapter;
import com.zerobudget.bookito.utils.Utils;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class ChatFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ChatFragmentBinding binding;
    private UserModel otherUser;
    private DatabaseReference realTimedb;
    private String requestID;

    private RecyclerView recyclerView;

    private Chat_RecycleViewAdapter adapter;

    private ArrayList<MessageModel> messages = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ChatFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Bundle args = getArguments();

        String otherId = args.getString("otherChatUser");
        otherUser = Utils.getGsonParser().fromJson(otherId, UserModel.class);
        requestID = args.getString("requestID");
        realTimedb = FirebaseDatabase.getInstance().getReference("/chatapp/" + requestID);

        recyclerView = binding.ChatRecycleView;

        adapter = new Chat_RecycleViewAdapter(this.getContext(), messages, null);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        setUpChatRoom();

        binding.sendMessage.setOnClickListener(view -> {
            String message = binding.inputMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                realTimedb.push().setValue(new MessageModel(Utils.USER_ID, args.getString("otherUserId"), message, null));
                binding.inputMessage.setText("");
            }
        });


        return root;
    }

    protected void setUpChatRoom() {
        realTimedb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    messages.add(dataSnapshot.getValue(MessageModel.class));
                }
//                adapter = new Chat_RecycleViewAdapter(ChatFragment.this.getContext(), messages, null);
//                recyclerView.setAdapter(adapter);
//                recyclerView.setLayoutManager(new LinearLayoutManager(ChatFragment.this.getContext()));
                adapter.notifyDataSetChanged();

                recyclerView.scrollToPosition(messages.size()-1);
                recyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("ENTRO", "SONO ENTRATO IN CANCELLAZIONE");
            }
        });

    }
}

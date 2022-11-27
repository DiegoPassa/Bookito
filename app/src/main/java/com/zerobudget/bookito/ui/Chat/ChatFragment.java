package com.zerobudget.bookito.ui.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.databinding.ChatFragmentBinding;
import com.zerobudget.bookito.models.Chat.MessageModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ChatFragmentBinding binding;
    private UserModel otherUser;
    private DatabaseReference realTimedb;
    private String requestID;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private Chat_RecycleViewAdapter adapter;

    private ArrayList<MessageModel> messages = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ChatFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar = binding.progressBar;

        progressBar.setVisibility(View.VISIBLE);

        Bundle args = getArguments();

        String otherId = args.getString("otherChatUser");
        otherUser = Utils.getGsonParser().fromJson(otherId, UserModel.class);
        requestID = args.getString("requestID");
        realTimedb = FirebaseDatabase.getInstance().getReference("/chatapp/" + requestID);
        String otherUserId = args.getString("otherUserId");

        recyclerView = binding.ChatRecycleView;

        adapter = new Chat_RecycleViewAdapter(this.getContext(), messages, otherUser, otherUserId, args.getParcelable("otherUserPic"));

        recyclerView.setAdapter(adapter);
        LinearLayoutManager l = new LinearLayoutManager(this.getContext());
        l.setStackFromEnd(true);
        recyclerView.setLayoutManager(l);

        setUpChatRoom();


        binding.sendMessage.setOnClickListener(view -> {
            String message = binding.inputMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                Date now = Timestamp.now().toDate();

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = sdf.format(now);

                SimpleDateFormat sdf1 = new SimpleDateFormat("hh/MM/yyyy", Locale.getDefault());
                String currentDate = sdf.format(now);

                realTimedb.push().setValue(new MessageModel(Utils.USER_ID, args.getString("otherUserId"), message, currentTime, currentDate));
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
                    if (!dataSnapshot.getKey().equals("user1") && !dataSnapshot.getKey().equals("user2")){

                        MessageModel msg = new MessageModel();
                        msg.setMessage(dataSnapshot.child("message").getValue(String.class));
                        msg.setSender(dataSnapshot.child("sender").getValue(String.class));
                        msg.setReceiver(dataSnapshot.child("receiver").getValue(String.class));
                        //il timestamp dava problemi brutti perché non ha un costruttore senza argomenti
                        msg.setMessageTime(dataSnapshot.child("messageTime").getValue(String.class));
                        msg.setMessageDate(dataSnapshot.child("messageDate").getValue(String.class));
                        //messages.add(dataSnapshot.getValue(MessageModel.class));
                        messages.add(msg);
                    }
                }

                adapter.notifyDataSetChanged();

                recyclerView.scrollToPosition(messages.size()-1);
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("ENTRO", "SONO ENTRATO IN CANCELLAZIONE");
            }
        });
    }
}

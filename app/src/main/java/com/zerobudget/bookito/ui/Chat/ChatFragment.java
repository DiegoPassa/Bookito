package com.zerobudget.bookito.ui.Chat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentChatBinding;
import com.zerobudget.bookito.models.Chat.MessageModel;
import com.zerobudget.bookito.models.Chat.MessageModelTrade;
import com.zerobudget.bookito.models.Chat.MessageModelWithImage;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FragmentChatBinding binding;
    private UserModel otherUser;
    private DatabaseReference realTimedb;
    private String requestID;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private Chat_RecycleViewAdapter adapter;

    private final ArrayList<MessageModel> messages = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        progressBar = binding.progressBar;

        progressBar.setVisibility(View.VISIBLE);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

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
                //inserisce il messaggio nel realtime database

                Date now = Timestamp.now().toDate();

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = sdf.format(now);
                SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String currentDate = sdf1.format(now);

                realTimedb.push().setValue(new MessageModel(Utils.USER_ID, args.getString("otherUserId"), message, "sent", currentTime, currentDate));
                binding.inputMessage.setText("");
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialToolbar toolbar = (MaterialToolbar) getActivity().findViewById(R.id.topAppBar);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    /**
     * in realtime prende i messaggi dal database e ne permette la visualizzazione sulla chat
     */
    protected void setUpChatRoom() {
        realTimedb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!dataSnapshot.getKey().equals("user1") && !dataSnapshot.getKey().equals("user2")) {

                        MessageModel msg;
                        if (dataSnapshot.hasChild("isbnBookTrade")) {
                            msg = new MessageModelTrade();
                            ((MessageModelTrade) msg).setIsbnBookTrade(dataSnapshot.child("isbnBookTrade").getValue(String.class));
                            ((MessageModelTrade) msg).setThumbnailBookTrade(dataSnapshot.child("thumbnailBookTrade").getValue(String.class));
                        } else if (dataSnapshot.hasChild("thumbnailBookRequested")) {
                            msg = new MessageModelWithImage();
                            ((MessageModelWithImage) msg).setThumbnailBookRequested(dataSnapshot.child("thumbnailBookRequested").getValue(String.class));
                        } else {
                            msg = new MessageModel();
                        }

                        //se lo status del messaggio dell'altro utente è segnato come sent,
                        //appena l'utente corrente apre la chat esso passa a read
                        if (dataSnapshot.hasChild("status"))
                            if (dataSnapshot.child("receiver").getValue(String.class).equals(Utils.USER_ID)
                                    && dataSnapshot.child("status").getValue(String.class).equals("sent")) {
                                realTimedb.child(dataSnapshot.getKey()).child("status").setValue("read");
                            }

                        msg.setStatus(dataSnapshot.child("status").getValue(String.class));
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
                //checkStatusPreviousMessages();

                adapter.notifyDataSetChanged();

                recyclerView.scrollToPosition(messages.size() - 1);
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

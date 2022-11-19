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
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.databinding.ChatFragmentBinding;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.ui.inbox.Inbox_RecycleViewAdapter;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class ChatFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ChatFragmentBinding binding;
    private UserModel otherUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ChatFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Bundle args = getArguments();

        assert args != null;
        String otherId = args.getString("otherChatUser");
        otherUser = Utils.getGsonParser().fromJson(otherId, UserModel.class);

        RecyclerView recyclerView = binding.ChatRecycleView;

        Chat_RecycleViewAdapter adapter = new Chat_RecycleViewAdapter(this.getContext(), new ArrayList<>(), null);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        return root;
    }
}

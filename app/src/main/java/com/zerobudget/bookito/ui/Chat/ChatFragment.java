package com.zerobudget.bookito.ui.Chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.databinding.ChatFragmentBinding;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

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
        String otherId = args.getString("otherChatUser");
        otherUser = Utils.getGsonParser().fromJson(otherId, UserModel.class);

        //l'utente glielo passiamo dal fragment delle richieste accettate, tanto dentro l'array delle richieste contiene anche l'altro user

        return root;
    }
}

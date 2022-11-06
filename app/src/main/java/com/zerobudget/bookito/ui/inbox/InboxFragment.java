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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.ui.Requests.RequestModel;

import java.util.ArrayList;

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

        binding.requestFloatingAction.setOnClickListener(view -> {
            Log.d("LOG", "CLICCATO BOTTONE +");
        });
        



        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
package com.zerobudget.bookito.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.zerobudget.bookito.databinding.FragmentUserProfileBinding;
import com.zerobudget.bookito.utils.Utils;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // BottomNavigationView navBar;

    public UserProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        //TODO: cambiare con id current user
        db.collection("users").document(Utils.USER_ID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                binding.usrFirstName.setText((String) task.getResult().get("first_name"));
                binding.usrLastName.setText((String) task.getResult().get("last_name"));
                binding.usrTelephone.setText((String) task.getResult().get("telephone"));
                binding.usrNeighborhood.setText((String) task.getResult().get("neighborhood"));
            }
        });

        // navBar = getActivity().findViewById(R.id.nav_view);
        // navBar.setVisibility(View.INVISIBLE);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // navBar.setVisibility(View.VISIBLE);
        binding = null;
    }

}
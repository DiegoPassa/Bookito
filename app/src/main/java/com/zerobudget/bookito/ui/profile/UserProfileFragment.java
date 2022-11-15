package com.zerobudget.bookito.ui.profile;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentUserProfileBinding;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String[] items;
    ArrayAdapter<String> adapterItems;

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

        UserModel user = UserModel.getCurrentUser();

        binding.usrFirstName.setText(user.getFirst_name());
        binding.usrLastName.setText(user.getLast_name());
        binding.usrTelephone.setText(user.getTelephone());
        binding.usrNeighborhood.setText(user.getNeighborhood());

        binding.floatingActionButton.setOnClickListener(view -> {
            changeVisibility();
        });

        binding.btnConfirmEdit.setOnClickListener(view -> {
            String new_neighborood = binding.autoCompleteTextView.getText().toString();

            if (!new_neighborood.equals("Venezia") && !new_neighborood.equals("Mestre") && !new_neighborood.equals("Campalto")
                    && !new_neighborood.equals("Favaro") && !new_neighborood.equals("Marghera")) {
                binding.editNeighborhood.setError("Devi selezionare un'azione!");
                binding.editNeighborhood.setDefaultHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.md_theme_light_error)));
            }else {
                //TODO: cambia id con current user
                db.collection("users").document(Utils.USER_ID).update("neighborood", new_neighborood).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        user.setNeighborhood(new_neighborood);
                        Toast.makeText(getContext(), "Fatto! Ora sei una persona nuova!", Toast.LENGTH_LONG).show();
                        //aggiorna la pagina
                        Navigation.findNavController(view).navigate(R.id.userProfileFragment);
                    }
                });
                changeVisibility();
            }
        });

        binding.btnAnnulla.setOnClickListener(view -> {
            changeVisibility();
        });


        return root;
    }

    private void changeVisibility(){
        if(binding.floatingActionButton.isShown()){
            binding.usrNeighborhood.setVisibility(View.INVISIBLE);
            binding.editNeighborhood.setVisibility(View.VISIBLE);
            binding.btnConfirmEdit.setVisibility(View.VISIBLE);
            binding.btnAnnulla.setVisibility(View.VISIBLE);
            binding.floatingActionButton.setVisibility(View.INVISIBLE);
        }else{
            binding.floatingActionButton.setVisibility(View.VISIBLE);
            binding.usrNeighborhood.setVisibility(View.VISIBLE);
            binding.editNeighborhood.setVisibility(View.INVISIBLE);
            binding.btnConfirmEdit.setVisibility(View.INVISIBLE);
            binding.btnAnnulla.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        items = getResources().getStringArray(R.array.quartieri);
        adapterItems = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, items);
        binding.autoCompleteTextView.setAdapter(adapterItems);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // navBar.setVisibility(View.VISIBLE);
        binding = null;
    }

}
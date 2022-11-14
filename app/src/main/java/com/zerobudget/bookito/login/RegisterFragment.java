package com.zerobudget.bookito.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentRegisterBinding;

import java.util.List;


public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private String phoneNumber,name,zone,surname;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(RegisterFragment.this)
                        .navigate(R.id.action_registerFragment_to_loginFragment);
            }
        });

        binding.registerConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                if(validateInput())
                    db.collection("users").whereEqualTo("telephone", phoneNumber)
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    List<DocumentSnapshot> x = task.getResult().getDocuments();
                                    if(x.isEmpty()){
                                        register();
                                    }else{
                                        binding.phoneNumberRegister.setError("Il numero inserito e gia registrato");
                                        binding.phoneNumberRegister.requestFocus();
                                    }

                                }
                            });
            }
        });
    }

    private boolean validateInput() {
        phoneNumber = binding.phoneNumberRegister.getText().toString().trim();
        name = binding.name.getText().toString().trim();
        zone = binding.zone.getText().toString().trim();
        surname = binding.surname.getText().toString().trim();
        if(name.isEmpty()){
            binding.name.setError("Il campo nome deve essere compilato");
            binding.name.requestFocus();
            return false;
        }
        if(surname.isEmpty()){
            binding.surname.setError("Il campo cognome deve essere compilato");
            binding.surname.requestFocus();
            return false;
        }
        if(phoneNumber.isEmpty()){
            binding.phoneNumberRegister.setError("Deve inserire il suo numero di telefono");
            binding.phoneNumberRegister.requestFocus();
            return false;
        }

        if(phoneNumber.length() != 10 || phoneNumber.charAt(0) != '3'){
            binding.phoneNumberRegister.setError("Il numero inserito non e valido");
            binding.phoneNumberRegister.requestFocus();
            return false;
        }

        //Possible change of input insertion mode
        if(zone.isEmpty()){
            binding.zone.setError("Deve specificare il quartiere dove abbita");
            binding.zone.requestFocus();
            return false;
        }
        return true;
    }

    public void register(){
        Bundle bundle = new Bundle();
        bundle.putString("phone_number", phoneNumber);
        bundle.putBoolean("register",true);
        bundle.putString("name",name);
        bundle.putString("surname",surname);
        bundle.putString("zone",zone);
        OTPConfirmFragment fragment = new OTPConfirmFragment();
        fragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,fragment).commit();
    }

}
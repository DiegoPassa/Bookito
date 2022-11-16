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
import com.zerobudget.bookito.databinding.FragmentLoginBinding;

import java.util.List;


public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        binding = FragmentLoginBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_loginFragment_to_registerFragment);
            }
        });

        binding.login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = binding.phoneNumber.getText().toString().trim();
                if(phoneNumber.isEmpty()){
                    binding.phoneNumber.setError("Inserisci il tuo numero di telefono");
                    binding.phoneNumber.requestFocus();
                    return;
                }
                if(phoneNumber.length() != 10 || phoneNumber.charAt(0) != '3'){
                    binding.phoneNumber.setError("Il numero inserito non e valido");
                    binding.phoneNumber.requestFocus();
                    return;
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("users").whereEqualTo("telephone", phoneNumber)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                List<DocumentSnapshot> x = task.getResult().getDocuments();
                                if(!x.isEmpty()){
                                    Bundle bundle = new Bundle();
                                    bundle.putString("phone_number", phoneNumber);
                                    bundle.putBoolean("register",false);
                                    OTPConfirmFragment fragment = new OTPConfirmFragment();
                                    fragment.setArguments(bundle);
                                    requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,fragment).commit();
                                }else{
                                    binding.phoneNumber.setError("Il numero inserito non e registrato");
                                    binding.phoneNumber.requestFocus();
                                }

                            }
                        });
            }
        });

    }


}
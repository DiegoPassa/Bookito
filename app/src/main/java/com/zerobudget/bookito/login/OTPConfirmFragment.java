package com.zerobudget.bookito.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.zerobudget.bookito.MainActivity;
import com.zerobudget.bookito.databinding.FragmentOtpConfirmBinding;
import com.zerobudget.bookito.ui.users.UserModel;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;


public class OTPConfirmFragment extends Fragment {

    FragmentOtpConfirmBinding binding;
    FirebaseAuth mAuth;
    private String code;
    Bundle bundle;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {



        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            code = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        bundle = this.getArguments();
        mAuth = FirebaseAuth.getInstance();
        binding = FragmentOtpConfirmBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String phoneNumber = bundle.getString("phone_number");
        phoneNumber = "+39" + phoneNumber;
        mAuth.setLanguageCode("it");
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(requireActivity())                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userInput = binding.otpUser.getText().toString().trim();
                if(userInput.isEmpty()){
                    binding.otpUser.setError("Inserisci il codice OTP");
                    binding.otpUser.requestFocus();
                    return;
                }
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(code,userInput);
                signInWithPhoneCredential(credential);
            }
        });
    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential) {
        boolean isRegister = bundle.getBoolean("register");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //TODO aggiungere l'informazione utente all'interno del DATABASE
                        if(task.isSuccessful()){
                            Intent intent = new Intent(requireActivity(), MainActivity.class);
                            if(isRegister)
                                addUserToDatabase();
                            startActivity(intent);
                            requireActivity().finish();

                        }
                    }
                });
    }

    private void addUserToDatabase() {
        String first_name = bundle.getString("name");
        String last_name = bundle.getString("surname");
        String phone = bundle.getString("phone_number");
        String neighbourhood = bundle.getString("zone");
        UserModel user = new UserModel(first_name,last_name,phone,neighbourhood,new HashMap<String,Object>());
    }
}
package com.zerobudget.bookito.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.MainActivity;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentOtpConfirmBinding;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.internal.Util;


public class OTPConfirmFragment extends Fragment {

    FragmentOtpConfirmBinding binding;
    FirebaseAuth mAuth;
    private String code;
    private Bundle bundle;
    private boolean isRegister;
    FirebaseFirestore db;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {



        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            binding.otpConfirmButton.setEnabled(true);
            binding.codeSentProgressBar.setVisibility(View.GONE);
            binding.doneCheck.setVisibility(View.VISIBLE);
            code = s;
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            signInWithPhoneCredential(phoneAuthCredential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
            if(isRegister){
                Fragment fragment = new RegisterFragment();
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,fragment).commit();
            }
            else {
                Fragment fragment = new LoginFragment();
                requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,fragment).commit();
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        bundle = this.getArguments();
        mAuth = FirebaseAuth.getInstance();
        binding = FragmentOtpConfirmBinding.inflate(inflater,container,false);
        isRegister = bundle.getBoolean("register");
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
        binding.otpConfirmButton.setOnClickListener(new View.OnClickListener() {
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
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //TODO aggiungere l'informazione utente all'interno del DATABASE
                        if(task.isSuccessful()){
                            if(isRegister)
                                addUserToDatabase();
                            else {
                                Intent intent = new Intent(requireActivity(), MainActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                                Utils.setUserId(mAuth.getCurrentUser().getUid());
                            }
                        }
                    }
                });
    }

    private void addUserToDatabase() {
        String first_name = bundle.getString("name");
        String last_name = bundle.getString("surname");
        String phone = bundle.getString("phone_number");
        String neighborhood = bundle.getString("zone");
        HashMap<String,Object> user = new HashMap<>();
        HashMap<String, Object> karma = new HashMap<>();
        karma.put("points", 0l);
        karma.put("numbers", 0l);
        user.put("first_name",first_name);
        user.put("last_name",last_name);
        user.put("telephone",phone);
        user.put("karma", karma);
        user.put("neighborhood",neighborhood);
        ArrayList<HashMap<String,Object>> books = new ArrayList<>();
        user.put("books",books);
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid().toString())
                .set(user)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(requireActivity(), "Il suo account e stato corretamente registrato.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        startActivity(intent);
                        Utils.setUserId(mAuth.getCurrentUser().getUid());
                        requireActivity().finish();
                    }
                });
    }
}
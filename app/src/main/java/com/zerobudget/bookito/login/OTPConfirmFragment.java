package com.zerobudget.bookito.login;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.FirebaseException;
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


public class OTPConfirmFragment extends Fragment {

    FragmentOtpConfirmBinding binding;
    FirebaseAuth mAuth;
    private String code;
    private Bundle bundle;
    private boolean isRegister;
    FirebaseFirestore db;
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

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
            Log.e(TAG, e.toString());
            if (isRegister) {
                NavHostFragment.findNavController(OTPConfirmFragment.this).navigate(R.id.action_OTPConfirmFragment_to_registerFragment);
            } else {
                NavHostFragment.findNavController(OTPConfirmFragment.this).navigate(R.id.action_OTPConfirmFragment_to_loginFragment);
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
        binding = FragmentOtpConfirmBinding.inflate(inflater, container, false);
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

        binding.otpConfirmButton.setOnClickListener(view1 -> {
            String userInput = binding.otpUser.getText().toString().trim();
            if (userInput.isEmpty()) {
                binding.otpUser.setError("Inserisci il codice OTP");
                binding.otpUser.requestFocus();
                return;
            }
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(code, userInput);
            signInWithPhoneCredential(credential);
        });

    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    //TODO aggiungere l'informazione utente all'interno del DATABASE
                    if (task.isSuccessful()) {
                        if (isRegister)
                            addUserToDatabase();
                        else {
                            Intent intent = new Intent(requireActivity(), MainActivity.class);
                            startActivity(intent);
                            requireActivity().finish();
                            Utils.setUserId(mAuth.getCurrentUser().getUid());
                        }
                    }
                });
    }

    private void addUserToDatabase() {
        String firstName = bundle.getString("name");
        String lastName = bundle.getString("surname");
        String phone = bundle.getString("phone_number");
        //String neighborhood = bundle.getString("zone");
        String township = bundle.getString("township");
        String city = bundle.getString("city");

        HashMap<String, Object> user = new HashMap<>();
        HashMap<String, Object> karma = new HashMap<>();
        karma.put("points", 0L);
        karma.put("numbers", 0L);
        user.put("firstName", firstName);
        user.put("lastName", lastName);
        user.put("telephone", phone);
        user.put("karma", karma);
        user.put("township", township);
        user.put("city", city);

        ArrayList<HashMap<String, Object>> books = new ArrayList<>();
        user.put("books", books);
        user.put("hasPicture", false);
        db = FirebaseFirestore.getInstance();
        db.collection("users").document(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .set(user)
                .addOnCompleteListener(task -> {
                    Toast.makeText(requireActivity(), "Account registrato con successo!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    startActivity(intent);
                    Utils.setUserId(mAuth.getCurrentUser().getUid());
                    requireActivity().finish();
                });
    }

}
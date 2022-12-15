package com.zerobudget.bookito.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.register.setOnClickListener(view12 -> NavHostFragment.findNavController(LoginFragment.this)
                .navigate(R.id.action_loginFragment_to_registerFragment));


        binding.phoneNumber.addTextChangedListener(new TextWatcher() {
            private int previousLength;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
                previousLength = charSequence.length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int editLen = editable.length();
                boolean backSpace = previousLength > editLen;

                if (editable.toString().isEmpty()) {
                    binding.login.setEnabled(false);
                } else {
                    if (editLen > 0 && editLen < 12) {
                        String numWithSpace = editable + " ";
                        if (!backSpace && (editLen == 3 || editLen == 7)) {
                            binding.phoneNumber.setText(numWithSpace);
                            binding.phoneNumber.setSelection(editLen + 1);
                        }
                        binding.login.setEnabled(false);
                    } else {
                        binding.login.setEnabled(true);
                    }
                }

            }
        });

        binding.login.setOnClickListener(view1 -> {
            String phoneNumber = binding.phoneNumber.getText().toString().replaceAll("\\s", "");

            if (phoneNumber.isEmpty()) {
                binding.phoneNumber.setError("Inserisci il tuo numero di telefono");
                binding.phoneNumber.requestFocus();
                return;
            }
            if (phoneNumber.length() != 10 && phoneNumber.length() != 9) {
                binding.phoneNumber.setError("Il numero inserito non è valido");
                binding.phoneNumber.requestFocus();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").whereEqualTo("telephone", phoneNumber)
                    .get()
                    .addOnCompleteListener(task -> {
                        List<DocumentSnapshot> x = task.getResult().getDocuments();
                        if (!x.isEmpty()) {
                            Bundle bundle = new Bundle();
                            bundle.putString("phone_number", phoneNumber);
                            bundle.putBoolean("register", false);
                            NavHostFragment.findNavController(LoginFragment.this).navigate(R.id.action_loginFragment_to_OTPConfirmFragment, bundle);
                        } else {
                            binding.phoneNumber.setError("Il numero inserito non è registrato");
                            binding.phoneNumber.requestFocus();
                        }

                    });
        });

    }


}
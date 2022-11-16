package com.zerobudget.bookito.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentRegisterBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private String phoneNumber, name, zone, surname;
    private ArrayList<String> items;
    ArrayAdapter<String> adapterItems;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        items = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.quartieri)));
        adapterItems = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, items);
        binding.autoCompleteTextView.setAdapter(adapterItems);

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cancel.setOnClickListener(view12 -> NavHostFragment.findNavController(RegisterFragment.this)
                .navigate(R.id.action_registerFragment_to_loginFragment));

        binding.phoneNumberRegister.addTextChangedListener(new TextWatcher() {
            private int previousLength;
            private boolean backSpace;

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
                backSpace = previousLength > editLen;

                if (editable.toString().isEmpty()) {
                    binding.registerConfirm.setEnabled(false);
                } else {
                    if (editLen > 0 && editLen < 11) {
                        String numWithSpace = editable + " ";
                        if (!backSpace && (editLen == 3 || editLen == 7)) {
                            binding.phoneNumberRegister.setText(numWithSpace);
                            binding.phoneNumberRegister.setSelection(editLen + 1);
                        }
                        binding.registerConfirm.setEnabled(false);
                    } else
                        binding.registerConfirm.setEnabled(true);
                }
            }
        });

        binding.registerConfirm.setOnClickListener(view1 -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            if (validateInput())
                db.collection("users").whereEqualTo("telephone", phoneNumber)
                        .get()
                        .addOnCompleteListener(task -> {
                            List<DocumentSnapshot> x = task.getResult().getDocuments();
                            if (x.isEmpty()) {
                                register();
                            } else {
                                binding.phoneNumberRegister.setError("Il numero inserito e gia registrato");
                                binding.phoneNumberRegister.requestFocus();
                            }

                        });
        });
    }

    private boolean validateInput() {
        phoneNumber = binding.phoneNumberRegister.getText().toString().replaceAll("\\s", "");
        name = binding.name.getText().toString().trim();
        zone = binding.autoCompleteTextView.getText().toString();
        surname = binding.surname.getText().toString().trim();

        if (name.isEmpty()) {
            binding.name.setError("Il campo nome deve essere compilato");
            binding.name.requestFocus();
            return false;
        }
        if (surname.isEmpty()) {
            binding.surname.setError("Il campo cognome deve essere compilato");
            binding.surname.requestFocus();
            return false;
        }
        if (phoneNumber.isEmpty()) {
            binding.phoneNumberRegister.setError("Deve inserire il suo numero di telefono");
            binding.phoneNumberRegister.requestFocus();
            return false;
        }

        if (phoneNumber.length() != 10 || phoneNumber.charAt(0) != '3') {
            binding.phoneNumberRegister.setError("Il numero inserito non e valido");
            binding.phoneNumberRegister.requestFocus();
            return false;
        }

        //Possible change of input insertion mode
        if (!items.contains(zone)) {
            binding.neighborhood.setError("Deve specificare il quartiere dove abbita");
            binding.neighborhood.requestFocus();
            return false;
        }
        return true;
    }

    public void register() {
        Bundle bundle = new Bundle();
        bundle.putString("phone_number", phoneNumber);
        bundle.putBoolean("register", true);
        bundle.putString("name", name);
        bundle.putString("surname", surname);
        bundle.putString("zone", zone);
        NavHostFragment.findNavController(RegisterFragment.this).navigate(R.id.action_registerFragment_to_OTPConfirmFragment,bundle);
    }

}
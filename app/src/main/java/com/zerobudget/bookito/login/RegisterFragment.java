package com.zerobudget.bookito.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentRegisterBinding;
import com.zerobudget.bookito.models.neighborhood.NeighborhoodModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.List;


public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private String phoneNumber, name, surname, township, city;
    private Boolean age;
    //private ArrayList<String> items;
    //ArrayAdapter<String> adapterItems;
    private final ArrayList<String> townshipsArray = new ArrayList<>();
    private ArrayList<String> citiesArray = new ArrayList<>();


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
        //items = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.quartieri)));
        //adapterItems = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, items);
        //binding.autoCompleteTextView.setAdapter(adapterItems);
        townshipsArray.clear();
        for (NeighborhoodModel s : Utils.getNeighborhoods()) {
            townshipsArray.add(s.getComune());
        }
        binding.newTownship.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, townshipsArray));
        citiesArray.clear();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cancel.setOnClickListener(view12 -> NavHostFragment.findNavController(RegisterFragment.this)
                .navigate(R.id.action_registerFragment_to_loginFragment));

        binding.newTownship.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                citiesArray = Utils.neighborhoodsMap.get(binding.newTownship.getText().toString());
                if (citiesArray != null) {
                    binding.newCity.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, citiesArray));
                }
            }
        });

        binding.checkBoxAge.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                binding.checkBoxAge.setError(null);
            }
        });

        binding.phoneNumberRegister.addTextChangedListener(new TextWatcher() {
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
                    binding.registerConfirm.setEnabled(false);
                } else {
                    if (editLen > 0 && editLen < 12) {
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

            if (validateInput()) {
                AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(getContext());
                View viewPopup = View.inflate(getContext(), R.layout.popup_gdpr, null);

                dialogBuilder.setView(viewPopup);
                AlertDialog dialog = dialogBuilder.create();

                ProgressBar progressBar = viewPopup.findViewById(R.id.progressBar);
                ConstraintLayout constr = viewPopup.findViewById(R.id.constr);
                constr.setVisibility(View.GONE);

                WebView web = viewPopup.findViewById(R.id.web);
                //sito web contenente l'informativa sulla privacy
                web.loadUrl("https://sites.google.com/view/bookito/home-page");
                web.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        progressBar.setVisibility(View.GONE);
                        constr.setVisibility(View.VISIBLE);
                    }
                });

                CheckBox checkBoxGdpr = viewPopup.findViewById(R.id.checkBox_gdpr);
                Button btnAccept = viewPopup.findViewById(R.id.btn_accept);
                btnAccept.setEnabled(false);

                Button btnRefuse = viewPopup.findViewById(R.id.btn_refuse);

                dialog.show();

                checkBoxGdpr.setOnCheckedChangeListener((compoundButton, b) -> {
                    //può registrarsi solo se ha seezionato la checkbox
                    btnAccept.setEnabled(compoundButton.isChecked());
                });

                btnRefuse.setOnClickListener(view2 -> {
                    dialog.dismiss();
                    Toast.makeText(getContext(), "Non puoi accedere a tutte le funzionalità di Bookito\nse non accetti la policy sulla privacy!", Toast.LENGTH_LONG).show();
                });

                btnAccept.setOnClickListener(view2 -> {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users").whereEqualTo("telephone", phoneNumber)
                            .get()
                            .addOnCompleteListener(task -> {
                                List<DocumentSnapshot> x = task.getResult().getDocuments();
                                if (x.isEmpty()) {
                                    register();
                                } else {
                                    binding.phoneNumberRegister.setError("Il numero inserito è gia registrato");
                                    binding.phoneNumberRegister.requestFocus();
                                }
                            });
                    dialog.dismiss();
                });
            }
        });
    }

    /**
     * controlla che gli input siano corretti
     */
    private boolean validateInput() {
        boolean flag = true;
        phoneNumber = binding.phoneNumberRegister.getText().toString().replaceAll("\\s", "");
        name = binding.name.getText().toString().trim();
        township = binding.newTownship.getText().toString().trim();
        city = binding.newCity.getText().toString().trim();
        surname = binding.surname.getText().toString().trim();
        age = binding.checkBoxAge.isChecked();

        if (name.isEmpty()) {
            binding.name.setError("Il campo nome deve essere compilato");
            // binding.name.requestFocus();
            flag = false;
        }
        if (surname.isEmpty()) {
            binding.surname.setError("Il campo cognome deve essere compilato");
            // binding.surname.requestFocus();
            flag = false;
        }
        if (phoneNumber.isEmpty()) {
            binding.phoneNumberRegister.setError("Devi inserire il tuo numero di telefono");
            // binding.phoneNumberRegister.requestFocus();
            flag = false;
        }

        if (phoneNumber.length() != 10) {
            binding.phoneNumberRegister.setError("Il numero inserito non è valido");
            // binding.phoneNumberRegister.requestFocus();
            flag = false;
        }

        if (!age) {
            binding.checkBoxAge.setError("Devi dichiarare di avere più di 13 anni");
            //binding.checkBoxAge.requestFocus();
            flag = false;
        }

        if (!townshipsArray.contains(township)) {
            binding.newTownship.setError("Seleziona un comune di residenza valido!");
            flag = false;
        }
        if (citiesArray == null || !citiesArray.contains(city)) {
            binding.newCity.setError("Seleziona la frazione in cui abiti!");
            flag = false;
        }
        return flag;
    }

    public void register() {
        Bundle bundle = new Bundle();
        bundle.putString("phone_number", phoneNumber);
        bundle.putBoolean("register", true);
        bundle.putString("name", name);
        bundle.putString("surname", surname);
        bundle.putString("township", township);
        bundle.putString("city", city);
        NavHostFragment.findNavController(RegisterFragment.this).navigate(R.id.action_registerFragment_to_OTPConfirmFragment, bundle);
    }

}
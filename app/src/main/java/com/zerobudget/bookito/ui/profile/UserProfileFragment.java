package com.zerobudget.bookito.ui.profile;

import static android.content.ContentValues.TAG;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentUserProfileBinding;
import com.zerobudget.bookito.models.neighborhood.NeighborhoodModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    private final ArrayList<String> townshipsArray = new ArrayList<>();
    private ArrayList<String> citiesArray = new ArrayList<>();

    private UserModel user;

    ActivityResultLauncher<String> activityResultGalleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    result -> {
                        if (result != null) {
                            addPicOnFirebase(result);
                        }
                    });

    private void openImagePicker() {
        activityResultGalleryLauncher.launch("image/*");
    }

    public UserProfileFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        user = Utils.CURRENT_USER;

        //Notifications.sendPushNotification("profilo di " + user.getFirstName() + " " + user.getLastName() + "\ntoken: " + user.getNotificationToken(), user.getTelephone(), user.getNotificationToken());

        binding.usrFirstName.setText(user.getFirstName());
        binding.usrLastName.setText(user.getLastName());
        binding.usrTelephone.setText(user.getTelephone());

        binding.usrTownship.setText(user.getTownship());
        binding.usrCity.setText(user.getCity());
        // binding.usrNeighborhood.setText(user.getNeighborhood());

        showPic();

        binding.imgContainer.setOnClickListener(view -> {
            showImagePicDialog();
        });

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
                binding.newCity.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, citiesArray));
            }
        });

        binding.floatingActionButton.setOnClickListener(view -> {
            binding.newTownship.setHint(binding.usrTownship.getText());
            binding.newCity.setHint(binding.usrCity.getText());
            changeVisibility();
        });

        binding.btnConfirmEdit.setOnClickListener(view -> {
            String new_township = binding.newTownship.getText().toString();
            String new_city = binding.newCity.getText().toString();
            if (!townshipsArray.contains(new_township)) {
                binding.newTownship.setError("Seleziona un comune valido!");
                binding.newTownship.requestFocus();
                // binding.newTownship.setDefaultHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.md_theme_light_error)));
            } else if (!citiesArray.contains(new_city)) {
                binding.newCity.setError("Seleziona una frazione valida!");
                binding.newCity.requestFocus();
            } else {
                db.collection("users").document(Utils.USER_ID).update("township", new_township, "city", new_city).addOnSuccessListener(unused -> {
                    // user.setNeighborhood(new_neighborhood);
                    //aggiorna la pagina
                    Toast.makeText(getContext().getApplicationContext(), "Fatto! Ora sei una persona nuova!", Toast.LENGTH_LONG).show();
                    // Navigation.findNavController(view).navigate(R.id.action_userProfileFragment_self);
                });
                Utils.CURRENT_USER.setTownship(new_township);
                Utils.CURRENT_USER.setCity(new_city);
                binding.usrTownship.setText(new_township);
                binding.usrCity.setText(new_city);
                changeVisibility();
            }
        });

        binding.btnAnnulla.setOnClickListener(view -> {
            changeVisibility();
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.setGroupVisible(R.id.default_group, false);
        menu.setGroupVisible(R.id.profile_group, true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_profile:
                Toast.makeText(getContext(), "Funzionalità da implementare", Toast.LENGTH_LONG).show();
                return true;

            default:
                return false;

        }
    }

    /**
     * visualizza l'immagine di profilo
     * se esso l'ha impostata viene prelevata dal database, altrimenti è generata una di default
     */
    private void showPic() {
        if (user.isHasPicture()) {
            Picasso.get().load(Utils.URI_PIC).into(binding.profilePic);
            binding.profilePic.setVisibility(View.VISIBLE);
            binding.userGravatar.setVisibility(View.GONE);
        } else {
            binding.userGravatar.setHash(user.getTelephone().hashCode());
            binding.userGravatar.setVisibility(View.VISIBLE);
            binding.profilePic.setVisibility(View.GONE);
        }
    }

    /**
     * visualizza il popup con le opzioni per scattare foro, selezionarla dalla galleria o eliminarla
     */
    private void showImagePicDialog() {

        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(this.getContext());
        View view = View.inflate(this.getContext(), R.layout.popup_edit_profilepic, null);

        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();

        TextView takePhoto = view.findViewById(R.id.take_pic);
        TextView chooseFromGallery = view.findViewById(R.id.choose_from_gallery);
        TextView removePhoto = view.findViewById(R.id.remove);

        takePhoto.setOnClickListener(view1 -> {
            //TODO: pick from camera
            Toast.makeText(getContext().getApplicationContext(), "Funzionalità da implementare", Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        chooseFromGallery.setOnClickListener(view1 -> {
            openImagePicker();//prende l'immagine dalla gallera
            dialog.dismiss();
        });

        removePhoto.setOnClickListener(view1 -> {
            if (!binding.userGravatar.isShown()) {
                deletePicOnFirebase();
                Utils.setUriPic("");
                Toast.makeText(getContext().getApplicationContext(), "Immagine eliminata correttamente!", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        dialogBuilder.setView(view);
        dialog.show();
    }


    /**
     * aggiunde l'immagine nel database
     *
     * @param uri: link da inserire all'interno di firebase storage
     */
    private void addPicOnFirebase(Uri uri) {
        //salvata in profile_pics/<id dell'utente>
        StorageReference riversRef = storageRef.child("profile_pics/" + Utils.USER_ID);
        UploadTask uploadTask = riversRef.putFile(uri);
        Utils.setUriPic(uri.toString());

        user.setHasPicture(true);

        uploadTask.addOnFailureListener(exception -> {
            Log.e(TAG, exception.getMessage());
        }).addOnSuccessListener(taskSnapshot -> {
            //Toast.makeText(getContext().getApplicationContext(), "Fatto! Ora sei una persona nuova", Toast.LENGTH_LONG);
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult().getUploadSessionUri(); //this is the download url that you need to pass to your database
                db.collection("users").document(Utils.USER_ID).update("hasPicture", true);
                showPic();
                Toast.makeText(getContext().getApplicationContext(), "Fatto! Ora sei una persona nuova!", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * elimina la foto profilo dal database
     */
    private void deletePicOnFirebase() {
        StorageReference desertRef = storageRef.child("profile_pics/" + Utils.USER_ID);

        user.setHasPicture(false);
        Utils.setUriPic("");

        desertRef.delete().addOnSuccessListener(aVoid -> {
            db.collection("users").document(Utils.USER_ID).update("hasPicture", false);
        }).addOnFailureListener(exception -> {
            Log.e(TAG, exception.getMessage());
        });

        showPic();
    }


    /**
     * cambia gli elementi visualizzati quando si effettuano modifiche al profilo
     * (in seguito alla pressione del float button sul fondo dello schermo)
     */
    private void changeVisibility() {
        if (binding.floatingActionButton.isShown()) {
            binding.editView.setVisibility(View.VISIBLE);
            binding.infoView.setVisibility(View.GONE);
            binding.floatingActionButton.setVisibility(View.GONE);
        } else {
            binding.editView.setVisibility(View.GONE);
            binding.infoView.setVisibility(View.VISIBLE);
            binding.floatingActionButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        townshipsArray.clear();
        for (NeighborhoodModel s : Utils.getNeighborhoods()) {
            townshipsArray.add(s.getComune());
        }
        binding.newTownship.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, townshipsArray));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
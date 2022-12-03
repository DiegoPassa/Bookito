package com.zerobudget.bookito.ui.profile;

import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
import androidx.navigation.Navigation;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Notifications;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentUserProfileBinding;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;

    private FirebaseFirestore db;
    private StorageReference storageRef;

    private ArrayList<String> items;
    ArrayAdapter<String> adapterItems;

    private UserModel user;

    ActivityResultLauncher<String> activityResultGalleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    result -> {
                        if (result != null) {
                            Log.d("URI", result.toString());
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

        Log.d("Sent to: ", user.getNotificationToken());
        Notifications.sendPushNotification("profilo di " + user.getFirstName() + " " + user.getLastName() + "\ntoken: " + user.getNotificationToken(), user.getTelephone(), user.getNotificationToken());


        binding.usrFirstName.setText(user.getFirstName());
        binding.usrLastName.setText(user.getLastName());
        binding.usrTelephone.setText(user.getTelephone());
        binding.usrNeighborhood.setText(user.getNeighborhood());

        showPic();

        Log.d("SONO_USER_PROFILE", user.getFirstName());

        binding.imgContainer.setOnClickListener(view -> {
            showImagePicDialog();
        });

        binding.floatingActionButton.setOnClickListener(view -> {
            binding.autoCompleteTextView.setHint(binding.usrNeighborhood.getText());
            changeVisibility();
        });

        binding.btnConfirmEdit.setOnClickListener(view -> {
            String new_neighborhood = binding.autoCompleteTextView.getText().toString();

            if (!items.contains(new_neighborhood)) {
                binding.editNeighborhood.setError("Seleziona un nuovo quartiere!");
                binding.editNeighborhood.setDefaultHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.md_theme_light_error)));
            } else {
                db.collection("users").document(Utils.USER_ID).update("neighborhood", new_neighborhood).addOnSuccessListener(unused -> {
                    user.setNeighborhood(new_neighborhood);
                    //aggiorna la pagina
                    Toast.makeText(getContext().getApplicationContext(), "Fatto! Ora sei una persona nuova!", Toast.LENGTH_LONG).show();
                    Navigation.findNavController(view).navigate(R.id.action_userProfileFragment_self);
                });
                changeVisibility();
            }
        });

        binding.btnAnnulla.setOnClickListener(view -> {
            changeVisibility();
        });

        return root;
    }

    private void showPic() {
        if (user.isHasPicture()) {
            binding.profilePic.setVisibility(View.VISIBLE);
            binding.userGravatar.setVisibility(View.GONE);
            if (Utils.URI_PIC.equals("")) {
                StorageReference load = storageRef.child("profile_pics/" + Utils.USER_ID);

                load.getDownloadUrl().addOnSuccessListener(uri -> {
                    Utils.setUriPic(uri.toString());
                    Picasso.get().load(uri.toString()).into(binding.profilePic);
                }).addOnFailureListener(exception -> {
                    String errorMessage = exception.getMessage();
                    Log.d("ERR", errorMessage);
                });
            } else {
                Picasso.get().load(Utils.URI_PIC).into(binding.profilePic);
            }
        } else {
            binding.userGravatar.setHash(user.getTelephone().hashCode());
            binding.userGravatar.setVisibility(View.VISIBLE);
            binding.profilePic.setVisibility(View.GONE);
        }
    }


    private void showImagePicDialog() {
        //String[] options = {"Scatta foto", "Seleziona da galleria", "Elimina foto"};

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
        });

        removePhoto.setOnClickListener(view1 -> {
            if (!binding.userGravatar.isShown()) {
                deletePicOnFirebase();
                Utils.setUriPic("");
                Toast.makeText(getContext().getApplicationContext(), "Immagine eliminata correttamente!", Toast.LENGTH_LONG).show();
                Navigation.findNavController(getView()).navigate(R.id.action_userProfileFragment_self);
            }
        });


        dialogBuilder.setView(view);
        dialog.show();

       /* AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Cosa vuoi fare?");
        builder.setItems(options, (dialogInterface, i) -> {
            if (i == 0) {

            } else if (i == 1) {
                openImagePicker();//prende l'immagine dalla gallera
            } else if (i == 2 && !binding.userGravatar.isShown()) {
                deletePicOnFirebase();
                Utils.setUriPic("");
                Toast.makeText(getContext().getApplicationContext(), "Immagine eliminata correttamente!", Toast.LENGTH_LONG).show();
                Navigation.findNavController(getView()).navigate(R.id.action_userProfileFragment_self);
            }
        });
        builder.create().show();*/
    }


    private void addPicOnFirebase(Uri uri) {
        StorageReference riversRef = storageRef.child("profile_pics/" + Utils.USER_ID);
        UploadTask uploadTask = riversRef.putFile(uri);

        user.setHasPicture(true);

        uploadTask.addOnFailureListener(exception -> {
            int errorCode = ((StorageException) exception).getErrorCode();
            String errorMessage = exception.getMessage();
            Log.d("ERR", errorMessage);
        }).addOnSuccessListener(taskSnapshot -> {
            //Toast.makeText(getContext().getApplicationContext(), "Fatto! Ora sei una persona nuova", Toast.LENGTH_LONG);
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult().getUploadSessionUri(); //this is the download url that you need to pass to your database
                db.collection("users").document(Utils.USER_ID).update("hasPicture", true).addOnSuccessListener(unused -> {
                    showPic();
                });
                Log.d("URIIDB", downloadUri.toString());
                Toast.makeText(getContext().getApplicationContext(), "Fatto! Ora sei una persona nuova!", Toast.LENGTH_LONG).show();
                // Navigation.findNavController(getView()).navigate(R.id.action_userProfileFragment_self);
            } else {
                //
            }
        });
    }

    private void deletePicOnFirebase() {
        StorageReference desertRef = storageRef.child("profile_pics/" + Utils.USER_ID);

        user.setHasPicture(false);

        desertRef.delete().addOnSuccessListener(aVoid -> {
            db.collection("users").document(Utils.USER_ID).update("hasPicture", false);
        }).addOnFailureListener(exception -> {
            int errorCode = ((StorageException) exception).getErrorCode();
            String errorMessage = exception.getMessage();
            Log.d("ERR_DEL", errorMessage);
        });

        showPic();
    }


    private void changeVisibility() {
        if (binding.floatingActionButton.isShown()) {
            binding.usrNeighborhood.setVisibility(View.GONE);
            binding.editNeighborhood.setVisibility(View.VISIBLE);
            binding.btnConfirmEdit.setVisibility(View.VISIBLE);
            binding.btnAnnulla.setVisibility(View.VISIBLE);
            binding.floatingActionButton.setVisibility(View.GONE);
        } else {
            binding.floatingActionButton.setVisibility(View.VISIBLE);
            binding.usrNeighborhood.setVisibility(View.VISIBLE);
            binding.editNeighborhood.setVisibility(View.GONE);
            binding.btnConfirmEdit.setVisibility(View.GONE);
            binding.btnAnnulla.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        items = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.quartieri)));
        adapterItems = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, items);
        binding.autoCompleteTextView.setAdapter(adapterItems);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
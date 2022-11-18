package com.zerobudget.bookito;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.zerobudget.bookito.databinding.ActivityMainBinding;
import com.zerobudget.bookito.login.LoginActivity;
import com.zerobudget.bookito.models.users.UserLibrary;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storageRef;

    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("STO_CREANDO", "AHHAHAHAHAHAHAHAH STO CREANDOOOOO");
        super.onCreate(savedInstanceState);
//
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
            return;
        }
        // Log.d("USER_ID", currentUser.getUid());
        Utils.setUserId(currentUser.getUid());

        getUriPic();

        initFirebaseMessaging();

        getQueryCurrentUser();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.topAppBar);

        binding.topAppBar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.request_page_nav, R.id.navigation_library, R.id.navigation_search) //changed navigation_requests to request_page_nav
                .build();
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {
            if (navDestination.getId() == R.id.userProfileFragment || navDestination.getId() == R.id.notificationsFragment) {
                navView.setVisibility(View.GONE);
            } else {
                navView.setVisibility(View.VISIBLE);
            }
        });

    }

    private void initFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()){
                Log.d("CAZZO", "CAZZOCAZZOCAZZOCAzZo");
                return;
            }

            // Get new FCM registration token
            String token = task.getResult();

            System.out.println(token);

        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                // TODO: Logout utente
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Disconnessione...", Toast.LENGTH_SHORT).show();
                finish();

            default:
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
        }
    }

    protected void getQueryCurrentUser() {
        // FirebaseUser currentUser = mAuth.getCurrentUser();
        //TODO aspettiamo la registrazione ed il login
        //String id = currentUser.getUid();
        db.collection("users").document(Utils.USER_ID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult() != null) {
                    UserModel u = UserModel.getUserFromDocument(task.getResult());
                    UserLibrary nowUser = new UserLibrary(u);

                    nowUser.setLibrary(UserLibrary.loadLibrary(task.getResult()));
                    UserModel.loadUser(nowUser);

                    Log.d("USER ORA AHAH", "" + UserModel.getUserFromDocument(task.getResult()).serialize());
                }
            }
        });
    }

    private void getUriPic() {
        StorageReference load = storageRef.child("profile_pics/" + Utils.USER_ID);

        load.getDownloadUrl().addOnSuccessListener(uri -> {
            Utils.setUriPic(uri.toString());
            Log.d("PIC", Utils.URI_PIC);
        }).addOnFailureListener(exception -> {
            int code = ((StorageException) exception).getErrorCode();
            if (code == StorageException.ERROR_OBJECT_NOT_FOUND)
                Log.d("ERR", "L'immagine non esiste");

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

}
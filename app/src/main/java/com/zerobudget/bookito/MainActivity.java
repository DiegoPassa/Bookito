package com.zerobudget.bookito;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        Utils.setUserId(currentUser.getUid());

        initFirebaseMessaging();
    }

    private void initFirebaseMessaging() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }
            // Get new FCM registration token
            String token = task.getResult();

            db.collection("users").document(Utils.USER_ID).update("notificationToken", token).addOnSuccessListener(task1 -> {
                getQueryCurrentUser();
            });
            Log.d("TOKEN GENERATO!!", "initFirebaseMessaging: " + token);

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
            case R.id.info:
                AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(MainActivity.this);
                View viewPopup = View.inflate(MainActivity.this, R.layout.fragment_gdpr, null);

                dialogBuilder.setView(viewPopup);
                AlertDialog dialog = dialogBuilder.create();

                ProgressBar progressBar = viewPopup.findViewById(R.id.progressBar);

                ConstraintLayout constr = viewPopup.findViewById(R.id.constr);
                constr.setVisibility(View.GONE);
                //WebView
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

                viewPopup.findViewById(R.id.btn_refuse).setVisibility(View.GONE);
                viewPopup.findViewById(R.id.checkBox_gdpr).setVisibility(View.GONE);
                Button btn = viewPopup.findViewById(R.id.btn_accept);
                btn.setEnabled(true);
                btn.setText("OK, torna indietro!");

                btn.setOnClickListener(view -> {
                    Toast.makeText(MainActivity.this, "Grazie per aver letto la nostra\ninformativa sulla privacy!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                });

                dialog.show();
                break;
            case R.id.logout:
                // TODO: Logout utente
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "Disconnessione...", Toast.LENGTH_SHORT).show();
                removeToken();
                finish();
                break;
            default:
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);


    }

    protected void removeToken() {
        db.collection("users").document(Utils.USER_ID)
                .update("notificationToken", "");
    }


    protected void getQueryCurrentUser() {
        // FirebaseUser currentUser = mAuth.getCurrentUser();
        //TODO aspettiamo la registrazione ed il login
        //String id = currentUser.getUid();

        // get user
        db.collection("users").document(Utils.USER_ID).get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                // initialize user
                Utils.CURRENT_USER = new UserLibrary(task.getResult().toObject(UserModel.class));
                Log.d("UTENTE CREATO!!", "getQueryCurrentUser: " + Utils.CURRENT_USER);

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
                    if (navDestination.getId() == R.id.userProfileFragment || navDestination.getId() == R.id.notificationsFragment || navDestination.getId() == R.id.chat_fragment) {
                        navView.setVisibility(View.GONE);
                    } else {
                        navView.setVisibility(View.VISIBLE);
                    }
                });

                getUriPic();
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
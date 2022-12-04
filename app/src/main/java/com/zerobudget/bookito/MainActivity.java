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
import com.google.android.material.badge.BadgeDrawable;
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
    private StorageReference storageRef;

    private AppBarConfiguration appBarConfiguration;
    private BadgeDrawable badge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("STO_CREANDO", "AHHAHAHAHAHAHAHAH STO CREANDOOOOO");

        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
            return;
        }

        Utils.setUserId(currentUser.getUid());

        updateNotificationToken();
    }

    private void updateNotificationToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get new FCM registration token
                String token = task.getResult();
                db.collection("users").document(Utils.USER_ID).update("notificationToken", token).addOnSuccessListener(task1 -> {
                    getCurrentUserFromDB();
                });
                Log.d("TOKEN GENERATO!!", "TOKEN: " + token);
            } else {
                Log.e("TOKEN ERROR", "updateNotificationToken: ", task.getException());
            }
        });
    }

    /**
     * preleva i dati dell'utente corrente dal database*/
    private void getCurrentUserFromDB() {
        // get user
        db.collection("users").document(Utils.USER_ID).get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                // initialize user
                Utils.CURRENT_USER = new UserLibrary(task.getResult().toObject(UserModel.class));
                Log.d("UTENTE CREATO!!", "getQueryCurrentUser: " + Utils.CURRENT_USER);
                if (Utils.CURRENT_USER.isHasPicture()) {
                    getUriPic();
                }
                mainActivitySetup();
            }
        });
    }

    private void mainActivitySetup() {
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

        int menuItemId = navView.getMenu().getItem(0).getItemId();
        badge = navView.getOrCreateBadge(menuItemId);

        navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {
            if (navDestination.getId() == R.id.userProfileFragment || navDestination.getId() == R.id.notificationsFragment || navDestination.getId() == R.id.chat_fragment) {
                navView.setVisibility(View.GONE);
            } else {
                navView.setVisibility(View.VISIBLE);
            }
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
                View viewPopup = View.inflate(MainActivity.this, R.layout.popup_gdpr, null);

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
                removeTokenAndLogout();
                break;
            default:
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
                return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.onNavDestinationSelected(item, navController) || super.onOptionsItemSelected(item);


    }

    protected void removeTokenAndLogout() {
        db.collection("users").document(Utils.USER_ID)
                .update("notificationToken", "").addOnSuccessListener(unused -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    Toast.makeText(getApplicationContext(), "Disconnessione...", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * carica l'immagine di profilo dell'utente*/
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

    /**
     * preleva le richieste ricevute dal database e inserisce il numero nel badge della bottom bar*/
    private void setRequestBadgeNumber(){
        Log.d("BADGEE", "aaaaaaaaaaaaaaaaaaaaaaaaa");
        db.collection("requests")
                .whereEqualTo("status", "undefined")
                .whereEqualTo("receiver", Utils.USER_ID)
                .get().addOnCompleteListener(task -> {
                    int numReq = task.getResult().size();
                    badge.setNumber(numReq);
                    badge.setVisible(numReq > 0);
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        setRequestBadgeNumber();
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


}
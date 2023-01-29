package com.zerobudget.bookito;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
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
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.zerobudget.bookito.databinding.ActivityMainBinding;
import com.zerobudget.bookito.login.LoginActivity;
import com.zerobudget.bookito.models.neighborhood.NeighborhoodModel;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.users.UserLibrary;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private StorageReference storageRef;
    private AppBarConfiguration appBarConfiguration;
    private BadgeDrawable badge;
    private ListenerRegistration fireStoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        if(!Utils.isOnline())
            Toast.makeText(MainActivity.this, "Sembra che tu non sia connesso ad internet, connettiti e riprova!", Toast.LENGTH_SHORT).show();
*/

        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Utils.setNeighborhoods(objectMapper.readValue(getAssets().open("neighborhoodJSON.json"), new TypeReference<List<NeighborhoodModel>>() {
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.neighborhoodsToMap();

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
            } else {
                Log.e("TOKEN ERROR", "updateNotificationToken: ", task.getException());
            }
        });
    }

    /**
     * preleva i dati dell'utente corrente dal database
     */
    private void getCurrentUserFromDB() {
        // get user
        db.collection("users").document(Utils.USER_ID).get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                // initialize user
                Utils.CURRENT_USER = new UserLibrary(task.getResult().toObject(UserModel.class));
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

        BottomNavigationView navView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.request_page_nav, R.id.navigation_library, R.id.navigation_search) //changed navigation_requests to request_page_nav
                .build();
        MaterialToolbar toolbar = binding.topAppBar;

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if (navHostFragment != null) {
            // NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            NavController navController = navHostFragment.getNavController();
            NavInflater navInflater = navController.getNavInflater();
            NavGraph graph = navInflater.inflate(R.navigation.mobile_navigation);

            graph.setStartDestination(R.id.to_navigation_library);
            navController.setGraph(graph);

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(binding.navView, navController);

            int menuItemId = navView.getMenu().getItem(0).getItemId();
            badge = navView.getOrCreateBadge(menuItemId);

            navController.addOnDestinationChangedListener((navController1, navDestination, bundle) -> {

                int destination = navDestination.getId();

                if (destination == R.id.userProfileFragment ||
                        destination == R.id.notificationsFragment ||
                        destination == R.id.chat_fragment ||
                        destination == R.id.bookTradeFragment ||
                        destination == R.id.searchByNameFragment) {

                    navView.setVisibility(View.GONE);
                    toolbar.getMenu().setGroupVisible(R.id.default_group, false);

                    if (destination == R.id.chat_fragment) {
                        toolbar.getMenu().setGroupVisible(R.id.chat_group, true);
                    }

                    if (destination == R.id.userProfileFragment) {
                        toolbar.getMenu().setGroupVisible(R.id.profile_group, true);
                    }

                    if (destination == R.id.searchByNameFragment) {
                        toolbar.getMenu().setGroupVisible(R.id.search_group, true);
                    }
                } else {
                    navView.setVisibility(View.VISIBLE);
                }
            });

            navView.setOnItemSelectedListener(item -> {
                if (graph.getStartDestinationId() != item.getItemId()) {
                    graph.setStartDestination(item.getItemId());
                    navController.navigate(item.getItemId());
                }
                return true;
            });
        }

        getRequestsRealTime();
        updateBadge();
    }

    /**
     * preleva in realtime le richieste ricevute dall'utente corrente
     */
    protected void getRequestsRealTime() {
        fireStoreListener = db.collection("requests")
                .whereEqualTo("receiver", Utils.USER_ID)
                .whereEqualTo("status", "undefined")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "getRequestsRealTime: ", error);
                        return;
                    }
                    if (value != null) {
                        for (DocumentChange doc : value.getDocumentChanges()) {
                            switch (doc.getType()) {
                                case ADDED:
                                    RequestModel addedRequestModel = RequestModel.getRequestModel(doc.getDocument().toObject(RequestModel.class).getType(), doc.getDocument());
                                    getUserByRequest(addedRequestModel, doc.getNewIndex());
                                    break;
                                case REMOVED:
                                    Utils.incomingRequests.remove(doc.getOldIndex());
                                    updateBadge();
                                    break;
                            }
                        }
                    }
                });

    }

    protected void updateBadge() {
        if (badge != null) {
            badge.setNumber(Utils.incomingRequests.size());
            badge.setVisible(badge.getNumber() > 0);
        }
    }

    protected void getUserByRequest(RequestModel r, int position) {
        db.collection("users").document(r.getSender())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UserModel u = task.getResult().toObject(UserModel.class);
                        r.setOtherUser(u);
                        Utils.incomingRequests.add(position, r);
                        updateBadge();
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
                String urlPagePolicy = "https://sites.google.com/view/bookito/home-page";
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
                web.loadUrl(urlPagePolicy);
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

            case R.id.about_us:
                String urlPageAboutUs = "https://sites.google.com/view/bookito-about-us/home-page";
                AlertDialog.Builder dialogBuilderAboutUs = new MaterialAlertDialogBuilder(MainActivity.this);
                View viewPopupAbUs = View.inflate(MainActivity.this, R.layout.popup_about_us, null);

                dialogBuilderAboutUs.setView(viewPopupAbUs);
                AlertDialog dialogAbUs = dialogBuilderAboutUs.create();

                ProgressBar progressBarAbUs = viewPopupAbUs.findViewById(R.id.progressBar);
                Button btnOk = viewPopupAbUs.findViewById(R.id.btn_ok);

                ConstraintLayout constr1 = viewPopupAbUs.findViewById(R.id.constr);
                constr1.setVisibility(View.GONE);
                //WebView
                WebView webAbUs = viewPopupAbUs.findViewById(R.id.web);
                //sito web contenente l'informativa sulla privacy
                webAbUs.loadUrl(urlPageAboutUs);
                webAbUs.setWebViewClient(new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        progressBarAbUs.setVisibility(View.GONE);
                        constr1.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        super.onPageStarted(view, url, favicon);
                        if (url.equals(urlPageAboutUs)) {
                            btnOk.setText("Ho capito, torna indietro!");
                            btnOk.setOnClickListener(view1 -> {
                                Toast.makeText(MainActivity.this, "Grazie per aver letto chi siamo!", Toast.LENGTH_LONG).show();
                                dialogAbUs.dismiss();
                            });
                        }
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                        String temp = request.getUrl().toString();
                        if (!temp.isEmpty() && !temp.equals(urlPageAboutUs)) {
                            btnOk.setText("Torna alla home page");
                            btnOk.setOnClickListener(view1 -> {
                                webAbUs.loadUrl(urlPageAboutUs);
                            });
                        }
                        return false;
                    }
                });

                btnOk.setOnClickListener(view -> {
                    Toast.makeText(MainActivity.this, "Grazie per aver letto chi siamo!", Toast.LENGTH_LONG).show();
                    dialogAbUs.dismiss();
                });

                dialogAbUs.show();
                break;
            case R.id.logout:
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
     * carica l'immagine di profilo dell'utente
     */
    private void getUriPic() {
        StorageReference load = storageRef.child("profile_pics/" + Utils.USER_ID);
        load.getDownloadUrl().addOnSuccessListener(uri -> {
            Utils.setUriPic(uri.toString());
        }).addOnFailureListener(exception -> {
            Log.e(TAG, "getUriPic: ", exception);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // setRequestBadgeNumber();
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fireStoreListener != null) fireStoreListener.remove();
        Utils.incomingRequests.clear();
    }
}
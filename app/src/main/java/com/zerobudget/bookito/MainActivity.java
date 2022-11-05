package com.zerobudget.bookito;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.databinding.ActivityMainBinding;
import com.zerobudget.bookito.ui.library.BookModel;
import com.zerobudget.bookito.ui.users.UserModel;

import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        getQueryCurrentUser();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_requests, R.id.to_navigation_library, R.id.navigation_search)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    protected void getQueryCurrentUser() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //TODO aspettiamo la registrazione ed il login
        //String id = currentUser.getUid();
        String id = "lcEOKGRTqiyx6UgExmgD";

        db.collection("users").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                UserModel u = new UserModel();
                DocumentSnapshot result = task.getResult();
                u.setFirst_name((String) result.get("first_name"));
                u.setLast_name((String) result.get("last_name"));
                u.setTelephone((String) result.get("telephone"));
                u.setNeighborhood((String) result.get("neighborhood"));
                u.setKarma(getKarma(result));
                u.setLibrary(getLibrary(result));
                UserModel.loadUser(u);
            }
        });
    }

    protected HashMap<String, Object> getKarma(DocumentSnapshot doc) {
        return (HashMap<String, Object>) doc.get("karma");
    }

    protected ArrayList<BookModel> getLibrary(DocumentSnapshot doc) {
        ArrayList<BookModel> library = new ArrayList<>();
        ArrayList<Object> results = (ArrayList<Object>) doc.get("books");

        for (Object book : results) {
            HashMap<Object, Object> map = (HashMap<Object, Object>)  book;
            BookModel newBook = new BookModel();
            newBook.setIsbn((String) map.get("isbn"));
            newBook.setThumbnail((String) map.get("thumbnail"));
            newBook.setTitle((String) map.get("title"));
            newBook.setAuthor((String)map.get("author"));
            library.add(newBook);
        }
        return library;
    }


}
package com.zerobudget.bookito.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.R;

import java.util.List;
import java.util.concurrent.Semaphore;

public class LoginActivity extends AppCompatActivity {
    //TODO aggiungere le stringhe hardcoded dal XML al file strings.xml
    //TODO grammar check per gli errori.
    //TODO Update UI + bugfixing

    public static class PhoneChecker{
        private String phone;
        private boolean isRegistered = false;
        FirebaseFirestore db;
        Semaphore sem;
        PhoneChecker(){
            this.phone = null;
        }
        PhoneChecker(String phone) {
            this.phone = phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        boolean isRegistered(){
            db = FirebaseFirestore.getInstance();
            db.collection("users").whereEqualTo("telephone", phone)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> x = task.getResult().getDocuments();
                            if(!x.isEmpty()){
                                confirmRegistered();
                            }

                        }
                    });
            return this.isRegistered;
        }
        void confirmRegistered(){
            this.isRegistered = true;
        }

        public boolean checkPhoneIntegrity(){
            if(phone != null){
                return phone.charAt(0) == '3' && phone.length() == 10;
            }
            return false;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}
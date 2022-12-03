package com.zerobudget.bookito.login;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zerobudget.bookito.R;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    public void onBackPressed() {
    }
}
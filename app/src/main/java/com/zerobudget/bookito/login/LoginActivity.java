package com.zerobudget.bookito.login;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.neighborhood.NeighborhoodModel;
import com.zerobudget.bookito.utils.Utils;

import java.io.IOException;
import java.util.List;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Utils.setNeighborhoods(objectMapper.readValue(getAssets().open("neighborhoodJSON.json"), new TypeReference<List<NeighborhoodModel>>() {
            }));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Utils.neighborhoodsToMap();
        Log.d("NeighborhoodMAP", Utils.neighborhoodsMap.toString());
        Log.d("NeighborhoodJSON", Utils.neighborhoods.toString());
    }

    @Override
    public void onBackPressed() {
    }
}
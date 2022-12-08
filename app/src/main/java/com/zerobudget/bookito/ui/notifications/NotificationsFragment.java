package com.zerobudget.bookito.ui.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentNotificationsBinding;
import com.zerobudget.bookito.models.Notification.NotificationModel;
import com.zerobudget.bookito.utils.Utils;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;


public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private DatabaseReference ref;
    private ArrayList<NotificationModel> notifications = new ArrayList<>();


    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);

        View root = binding.getRoot();

        setUpFragmentData();

        return root;
    }

    protected void setUpFragmentData() {
        ref = FirebaseDatabase.getInstance().getReference("/notification/"+ Utils.USER_ID);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notifications.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NotificationModel not = dataSnapshot.getValue(NotificationModel.class);
                    not.setNotificationId(dataSnapshot.getKey());
                    notifications.add(not);
                }
                Log.d("ARRAY", ""+notifications);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
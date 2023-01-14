package com.zerobudget.bookito.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zerobudget.bookito.databinding.FragmentNotificationsBinding;
import com.zerobudget.bookito.models.notification.NotificationModel;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;


public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private DatabaseReference ref;
    private ArrayList<NotificationModel> notifications = new ArrayList<>();

    private Notification_RecycleViewAdapter adapter;
    private RecyclerView recyclerView;


    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        recyclerView = binding.notificationRecycleView;

        setUpScrollDelete();
        setUpRecycleView();
        setUpFragmentData();

        return root;
    }

    protected void setUpScrollDelete() {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                int position = viewHolder.getAdapterPosition();
//                notifications.remove(position);
//                adapter.notifyDataSetChanged();
                deleteNotificationFromDatabase(viewHolder.getAdapterPosition());
            }

            private void deleteNotificationFromDatabase(int position) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("/notification/" + Utils.USER_ID + '/' + notifications.get(position).getNotificationId());
                databaseReference.removeValue();
            }

        };

        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);
    }

    protected void setUpFragmentData() {
        ref = FirebaseDatabase.getInstance().getReference("/notification/" + Utils.USER_ID);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notifications.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NotificationModel not = dataSnapshot.child("data_notify").getValue(NotificationModel.class);
                    not.setNotificationId(dataSnapshot.getKey());
                    not.setUserModel(dataSnapshot.child("actioner").getValue(UserModel.class));
                    not.setRequest(dataSnapshot.child("request").getValue(RequestModel.class));
                    notifications.add(not);
                }

                Collections.sort(notifications); //ordina le notifiche in ordine di arrivo (la più recente in cima)
                adapter.notifyDataSetChanged();

                if (notifications.size() > 0)
                    binding.emptyNotifications.setVisibility(View.GONE);
                else
                    binding.emptyNotifications.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void setUpRecycleView() {
        adapter = new Notification_RecycleViewAdapter(getContext(), notifications);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}
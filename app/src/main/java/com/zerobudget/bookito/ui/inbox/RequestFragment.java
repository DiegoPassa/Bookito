package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.utils.Utils;

import java.util.Objects;

public class RequestFragment extends Fragment {

    private DatabaseReference realTimedb;
    private ViewPager2 viewPager;
    private TabLayout tabs;

    RequestPageAdapter adapter;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_request_page, container, false);

        realTimedb = FirebaseDatabase.getInstance().getReference("/chatapp/");

        // Setting ViewPager for each Tabs
        viewPager = view.findViewById(R.id.viewPager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        tabs = view.findViewById(R.id.tabLayout);

        tabs.getTabAt(Wrapper.position).select();
        viewPager.setCurrentItem(Wrapper.position);

        setUpBadging();

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //il badge è rimosso se si è nel tab corrente
                tab.removeBadge();
                viewPager.setCurrentItem(tab.getPosition());
                Wrapper.setPosition(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //se non è selezionato l'ultimo tab mostra il badge
                if (tab.getPosition() == 2)
                    setUpBadging();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //il badge è rimosso se si riseleziona il tab
                tab.removeBadge();
            }
        });


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Objects.requireNonNull(tabs.getTabAt(position)).select();
            }
        });


        return view;

    }

    private void setupViewPager(ViewPager2 viewPager) {
        adapter = new RequestPageAdapter(getChildFragmentManager(), getLifecycle());

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
//        viewPager.setUserInputEnabled(false);

    }

    public static class Wrapper {
        public static Integer position = 0;

        public static void setPosition(Integer i) {
            position = i;
        }
    }

    /**
     * in real time vede se esistono nuovi messaggi nelle chat e ne visualizza il numero nel tab realtivo
     */
    protected void setUpBadging() {

        realTimedb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                int tot = 0;
                //TODO dare permessi migliori su firebase
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (!ds.getKey().equals("user1") && !ds.getKey().equals("user2")) {
                            //se lo status del messaggio dell'altro utente è segnato come sent,
                            //viene contato come nuovo messaggio
                            if (ds.hasChild("status"))
                                if (ds.child("receiver").getValue(String.class).equals(Utils.USER_ID)
                                        && ds.child("status").getValue(String.class).equals("sent"))
                                    tot++;
                        }
                    }
                }
                if(tot > 0)
                    tabs.getTabAt(2).getOrCreateBadge().setNumber(tot);
                else
                    tabs.getTabAt(2).removeBadge();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                Log.e("DB ERROR", error.getMessage());
            }
        });
    }


}

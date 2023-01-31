package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class RequestFragment extends Fragment {

    protected FirebaseFirestore db;
    private ArrayList<String> arrRequestsID = new ArrayList<>();
    private DatabaseReference realTimedb;
    private ViewPager2 viewPager;
    private TabLayout tabs;
    private int tot = 0;

    private RequestPageAdapter adapter;
    private ValueEventListener event[];

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        for (int i = 0; i < arrRequestsID.size(); i++) {
            realTimedb = FirebaseDatabase.getInstance().getReference("/chatapp/" + arrRequestsID.get(i));
            realTimedb.removeEventListener(event[i]);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_request_page, container, false);

        this.db = FirebaseFirestore.getInstance();

        // Setting ViewPager for each Tabs
        viewPager = view.findViewById(R.id.viewPager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        tabs = view.findViewById(R.id.tabLayout);

        tabs.getTabAt(Wrapper.position).select();
        viewPager.setCurrentItem(Wrapper.position, false);
        setUpBadgeNotRead();


        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //il badge è rimosso se si è nel tab corrente
//                tab.removeBadge();
                viewPager.setCurrentItem(tab.getPosition());
                Wrapper.setPosition(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //se non è selezionato l'ultimo tab mostra il badge
//                if (tab.getPosition() == 2) {
//                    setUpBadgeNotRead();
//                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //il badge è rimosso se si riseleziona il tab
//                tab.removeBadge();
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

    private ValueEventListener createEvent(int[] counts, int index) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                counts[index] = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!dataSnapshot.getKey().equals("user1") && !dataSnapshot.getKey().equals("user2")) {
                        //se lo status del messaggio dell'altro utente è segnato come sent,
                        //viene contato come nuovo messaggio
                        if (dataSnapshot.hasChild("status"))
                            if (dataSnapshot.child("receiver").getValue(String.class).equals(Utils.USER_ID)
                                    && dataSnapshot.child("status").getValue(String.class).equals("sent"))
                                counts[index]++;
                    }
                }
                tot = Arrays.stream(counts).sum();
                if (tot > 0)
                    tabs.getTabAt(2).getOrCreateBadge().setNumber(tot);
                else
                    tabs.getTabAt(2).removeBadge();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                Log.e("DB ERROR", error.getMessage());
            }
        };
    }

    /**
     * in real time vede se esistono nuovi messaggi nelle chat e ne visualizza il numero nel tab realtivo
     */
    protected void getNumberMsgNotRead() {
        int[] counts = new int[arrRequestsID.size()];

        for (int i = 0; i < arrRequestsID.size(); i++) {
            final int index = i;
            realTimedb = FirebaseDatabase.getInstance().getReference("/chatapp/" + arrRequestsID.get(i));
            event[i] = createEvent(counts, index);
            realTimedb.addValueEventListener(event[i]);
        }
    }



    private void setUpBadgeNotRead() {
        arrRequestsID.clear();
        Task<QuerySnapshot> requestSent = db.collection("requests")
                .whereEqualTo("status", "accepted")
                .whereEqualTo("sender", Utils.USER_ID).get();

        Task<QuerySnapshot> requestReceived = db.collection("requests")
                .whereEqualTo("status", "accepted")
                .whereEqualTo("receiver", Utils.USER_ID).get();

        Tasks.whenAllSuccess(requestSent, requestReceived).addOnSuccessListener(list -> {
            QuerySnapshot queryRequestSent = (QuerySnapshot) list.get(0);
            QuerySnapshot queryRequestReceived = (QuerySnapshot) list.get(1);

            for (QueryDocumentSnapshot doc : queryRequestSent)
                arrRequestsID.add((String) doc.getId());

            for (QueryDocumentSnapshot doc : queryRequestReceived)
                arrRequestsID.add((String) doc.getId());

            event = new ValueEventListener[arrRequestsID.size()];
            getNumberMsgNotRead();
        });
    }
}

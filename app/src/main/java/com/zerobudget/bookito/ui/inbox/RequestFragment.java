package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;
import com.zerobudget.bookito.databinding.FragmentRequestPageBinding;

public class RequestFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_request_page,container, false);
        // Setting ViewPager for each Tabs
        ViewPager2 viewPager = (ViewPager2) view.findViewById(R.id.viewPager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        TabLayout tabs = (TabLayout) view.findViewById(R.id.tabLayout);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 2) {
                    Log.d("TEST", "AAAA");
                }else
                    viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabs.getTabAt(position).select();
            }
        });
        return view;

    }

    private void setupViewPager(ViewPager2 viewPager) {
        RequestPageAdapter adapter = new RequestPageAdapter(getActivity());

        viewPager.setAdapter(adapter);
    }

}

package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.zerobudget.bookito.R;

import java.util.Objects;

public class RequestFragment extends Fragment {

    ViewPager2 viewPager;
    TabLayout tabs;

    RequestPageAdapter adapter;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_request_page, container, false);


        // Setting ViewPager for each Tabs
        viewPager = view.findViewById(R.id.viewPager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        tabs = view.findViewById(R.id.tabLayout);

        tabs.getTabAt(Wrapper.position).select();
        viewPager.setCurrentItem(Wrapper.position);


        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                Wrapper.setPosition(tab.getPosition());
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


}

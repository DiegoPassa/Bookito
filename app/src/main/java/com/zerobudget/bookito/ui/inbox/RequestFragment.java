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

import java.util.Objects;

public class RequestFragment extends Fragment {

    ViewPager2 viewPager;
    TabLayout tabs;

    RequestPageAdapter adapter;
    int position= 0;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        position = tabs.getSelectedTabPosition();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_request_page,container, false);


        // Setting ViewPager for each Tabs
        viewPager = view.findViewById(R.id.viewPager);
        setupViewPager(viewPager);
        // Set Tabs inside Toolbar
        tabs = view.findViewById(R.id.tabLayout);
        tabs.getTabAt(Wrapper.position).select();
        viewPager.setCurrentItem(position);


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

}

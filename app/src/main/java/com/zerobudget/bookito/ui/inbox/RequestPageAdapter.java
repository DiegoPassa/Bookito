package com.zerobudget.bookito.ui.inbox;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zerobudget.bookito.ui.library.LibraryFragment;

import java.util.ArrayList;
import java.util.List;

public class RequestPageAdapter extends FragmentStateAdapter {


    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public RequestPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Log.d("CREATE_FRAGMENT", "ENTRO");
        switch (position) {
            case 0: return new InboxFragment();
            case 1: return new RequestSentFragment();
            //case2: retutn new CompletedRequestFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2; //todo mettere 3 appena facciamo il terzo fragment
    }
}

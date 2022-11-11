package com.zerobudget.bookito.ui.inbox;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RequestPageAdapter extends FragmentStateAdapter {
    public RequestPageAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0: return new InboxFragment();
            case 1: return new RequestSentFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

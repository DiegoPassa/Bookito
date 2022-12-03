package com.zerobudget.bookito.ui.inbox;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RequestPageAdapter extends FragmentStateAdapter {
    public RequestPageAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    public RequestPageAdapter(FragmentActivity fa) {
        super(fa);
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 0:
                return new RequestsReceivedFragment();
            case 1:
                return new RequestsSentFragment();
            case 2:
                return new RequestsAcceptedFragment();
            //case2: retutn new CompletedRequestFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

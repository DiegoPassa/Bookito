package com.zerobudget.bookito.ui.inbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentInboxBinding;

public class InboxFragment extends Fragment {

    private FragmentInboxBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        InboxViewModel inboxViewModel = new ViewModelProvider(this).get(InboxViewModel.class);

        binding = FragmentInboxBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.testFragment);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
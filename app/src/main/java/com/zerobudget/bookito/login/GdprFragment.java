package com.zerobudget.bookito.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zerobudget.bookito.databinding.FragmentGdprBinding;

public class GdprFragment extends Fragment {

    private FragmentGdprBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentGdprBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.web.loadUrl("https://sites.google.com/view/bookito/home-page");
        binding.web.setWebViewClient(new WebViewClient());

        return root;
    }
}

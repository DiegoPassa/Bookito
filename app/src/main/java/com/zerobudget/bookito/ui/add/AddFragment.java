package com.zerobudget.bookito.ui.add;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentAddBinding;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class AddFragment extends Fragment {

    private FragmentAddBinding binding;

    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
       if (result.getContents() != null){
           AlertDialog.Builder builder = new AlertDialog.Builder(AddFragment.this.getContext());
           builder.setTitle("Result");
           builder.setMessage(result.getContents());
           builder.setPositiveButton("OK", (dialogInterface, i) -> {
               dialogInterface.dismiss();
           }).show();
       }
    });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        AddViewModel addViewModel =
                new ViewModelProvider(this).get(AddViewModel.class);

        binding = FragmentAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView textView = binding.textNotifications;
        addViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        binding.textView2.setText(Integer.toString(addViewModel.getScore()));
/*
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(this.getContext(), R.anim.my_animation);
        textView.startAnimation(hyperspaceJumpAnimation);
*/

        binding.addOneBtm.setOnClickListener(v -> {
            addViewModel.plusScore();
            binding.textView2.setText(Integer.toString(addViewModel.getScore()));
        });

        binding.subOneBtn.setOnClickListener(view -> {
            addViewModel.subScore();
            binding.textView2.setText(Integer.toString(addViewModel.getScore()));
        });

        binding.scanBtn.setOnClickListener(view -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Ao");
            options.setOrientationLocked(true);
            options.setCaptureActivity(CaptureAct.class);

            barLauncher.launch(options);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    static public class CaptureAct extends CaptureActivity {

    }
}
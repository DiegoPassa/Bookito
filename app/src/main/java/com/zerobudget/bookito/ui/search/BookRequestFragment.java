package com.zerobudget.bookito.ui.search;

import android.app.AlertDialog;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentRequestBookBinding;
import com.zerobudget.bookito.utils.Utils;

public class BookRequestFragment extends Fragment {

    private FragmentRequestBookBinding binding;
    private SearchResultsModel usrBookSelected;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        binding = FragmentRequestBookBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        String str = args.getString("USR_BK");
        usrBookSelected= Utils.getGsonParser().fromJson(str, SearchResultsModel.class);

        binding.bookTitle.setText(usrBookSelected.getBook().getTitle());
        binding.bookAuthor.setText(usrBookSelected.getBook().getAuthor());
        binding.bookDescription.setText(usrBookSelected.getBook().getDescription());
        binding.bookDescription.setMovementMethod(new ScrollingMovementMethod());
        String owner = usrBookSelected.getUser().getFirst_name()+" "+usrBookSelected.getUser().getLast_name();
        binding.bookOwner.setText(owner);
        binding.bookType.setText(usrBookSelected.getBook().getType());
        Picasso.get().load(usrBookSelected.getBook().getThumbnail()).into(binding.bookThumbnail);


        switch (usrBookSelected.getBook().getType()) {
            case "Scambio":
                binding.bookmarkOutline.setColorFilter(getContext().getColor(R.color.bookmark_outline_scambio), PorterDuff.Mode.SRC_ATOP);
                binding.bookmark.setColorFilter(getContext().getColor(R.color.bookmark_scambio), PorterDuff.Mode.SRC_ATOP);
                break;
            case "Prestito":
                binding.bookmarkOutline.setColorFilter(getContext().getColor(R.color.bookmark_outline_prestito), PorterDuff.Mode.SRC_ATOP);
                binding.bookmark.setColorFilter(getContext().getColor(R.color.bookmark_prestito), PorterDuff.Mode.SRC_ATOP);
                break;
            case "Regalo":
                binding.bookmarkOutline.setColorFilter(getContext().getColor(R.color.bookmark_outine_regalo), PorterDuff.Mode.SRC_ATOP);
                binding.bookmark.setColorFilter(getContext().getColor(R.color.bookmark_regalo), PorterDuff.Mode.SRC_ATOP);
                break;
            default:
                Picasso.get().load(R.drawable.bookmark_template).into(binding.bookmark);
                break;
        }

        binding.btnRequest.setOnClickListener(view -> {
            //TODO: implementare le query di richiesta
            /*AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle("Richiesta effettuata");
            builder.setMessage("La richiesta è andata a buon fine");
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                Navigation.findNavController(view).navigate(R.id.to_navigation_library);
            }).show();*/
        });

        //}

        return root;
    }
}

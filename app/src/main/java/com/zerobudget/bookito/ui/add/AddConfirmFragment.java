package com.zerobudget.bookito.ui.add;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentConfirmAddBinding;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.Utils;


public class AddConfirmFragment extends Fragment {

    private FragmentConfirmAddBinding binding;
    protected BookModel newBook;

    String[] items;

    AutoCompleteTextView autoCompleteTxt;
    ArrayAdapter<String> adapterItems;

    @Override
    public void onResume() {
        super.onResume();
        items = getResources().getStringArray(R.array.azioni_libro);
        adapterItems = new ArrayAdapter<>(requireContext(), R.layout.dropdown_item, items);
        binding.autoCompleteTextView.setAdapter(adapterItems);

    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentConfirmAddBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Bundle args = getArguments();
        String str = args.getString("BK");
        newBook = Utils.getGsonParser().fromJson(str, BookModel.class);

        binding.bookTitle.setText(newBook.getTitle());
        binding.bookAuthor.setText(newBook.getAuthor());
        binding.bookDescription.setText(newBook.getDescription());
        binding.bookDescription.setMovementMethod(new ScrollingMovementMethod());
        Picasso.get().load(newBook.getThumbnail()).into(binding.bookThumbnail);

        Log.d("BKCONF", newBook.getTitle());


        binding.btnConfirm.setOnClickListener(view -> {
            String action = binding.autoCompleteTextView.getText().toString();

            if (!action.equals("Regalo") && !action.equals("Scambio") && !action.equals("Prestito")) {
                binding.InputText.setError("Devi selezionare un'azione!");
                binding.InputText.setDefaultHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.md_theme_light_error)));
            } else {
                newBook.setType(action);
                addBook();//inserimento libro nel database

                Toast.makeText(getContext(), "Libro " + newBook.getTitle() + " inserito correttamente!", Toast.LENGTH_LONG).show();

                Navigation.findNavController(view).navigate(R.id.action_addConfirmFragment_to_navigation_library);

/*                AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this.getContext());
                builder.setTitle("Conferma");
                builder.setMessage("Libro " + newBook.getTitle() + " è stato inserito correttamente");
                builder.setPositiveButton("OK", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    Navigation.findNavController(view).navigate(R.id.action_addConfirmFragment_to_navigation_library);
                }).show();*/
            }
        });

        binding.btnCancel.setOnClickListener(view -> {
            Navigation.findNavController(view).navigate(R.id.to_navigation_library);

            Toast.makeText(getContext(), "Inserimento annullato", Toast.LENGTH_LONG).show();

/*            AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this.getContext());
            builder.setTitle("Attenzione");
            builder.setMessage("Inserimento annullato");
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                dialogInterface.dismiss();
                Navigation.findNavController(view).navigate(R.id.to_navigation_library);
            }).show();*/
        });

        return root;
    }


    /**
     * inserisce il nuovo libro nel database, nel documento dell'utente corrente
     */
    private void addBook() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        //TODO: in attesa dell'autenticazione dell'utente qusto resta commentato
        //if (currentUser != null) {
        //   String id = currentUser.getUid();
        db.collection("users").document(Utils.USER_ID)
                .update("books", FieldValue.arrayUnion(newBook.serialize())).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (UserModel.getCurrentUser() != null)
                            UserModel.getCurrentUser().appendBook(newBook);
                    }
                });
        // }
    }
}

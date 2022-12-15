package com.zerobudget.bookito.ui.add;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentConfirmAddBinding;
import com.zerobudget.bookito.models.book.BookModel;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;


public class AddConfirmFragment extends Fragment {

    private FragmentConfirmAddBinding binding;
    protected BookModel newBook;

    String[] items;

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

        binding.btnConfirm.setOnClickListener(view -> {
            String action = binding.autoCompleteTextView.getText().toString();

            if (!action.equals("Regalo") && !action.equals("Scambio") && !action.equals("Prestito")) {
                binding.InputText.setError("Devi selezionare un'azione!");
                binding.InputText.setDefaultHintTextColor(ColorStateList.valueOf(getResources().getColor(R.color.md_theme_light_error)));
            } else {
                newBook.setType(action);
                addBook(getContext());//inserimento libro nel database
            }
        });

        binding.btnCancel.setOnClickListener(view -> {
            Navigation.findNavController(view).navigate(R.id.to_navigation_library);

            Toast.makeText(getContext(), "Inserimento annullato", Toast.LENGTH_LONG).show();
        });

        return root;
    }

    /**
     * inserisce il nuovo libro nel database, nel documento dell'utente corrente
     * controlla che non esista già un libro con lo stesso isbn
     *
     * @param context: contesto nel quale si stanno svolgendo le operazioni
     */
    private void addBook(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(Utils.USER_ID).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                boolean alreadyExists = false;
                Object books = task.getResult().get("books");
                for (Object o : (ArrayList<Object>) books) {
                    HashMap<String, Object> map = (HashMap<String, Object>) o;
                    if (map.get("isbn").equals(newBook.getIsbn())) {
                        alreadyExists = true;
                    }
                }

                //controlla che il libro non sia già presente
                if (!alreadyExists) {
                    db.collection("users").document(Utils.USER_ID)
                            .update("books", FieldValue.arrayUnion(newBook.serialize()));

                    Toast.makeText(context, "Libro " + newBook.getTitle() + " inserito correttamente!", Toast.LENGTH_LONG).show();
                    Utils.CURRENT_USER.getBooks().add(newBook);

                } else {
                    Toast.makeText(context, "Il libro è già presente nella libreria", Toast.LENGTH_LONG).show();

                }
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_addConfirmFragment_to_navigation_library);
            }
        });
    }
}

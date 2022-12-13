package com.zerobudget.bookito.ui.chat;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.databinding.FragmentChatBinding;
import com.zerobudget.bookito.models.chat.MessageModel;
import com.zerobudget.bookito.models.chat.MessageModelTrade;
import com.zerobudget.bookito.models.chat.MessageModelWithImage;
import com.zerobudget.bookito.models.requests.RequestModel;
import com.zerobudget.bookito.models.requests.RequestShareModel;
import com.zerobudget.bookito.models.requests.RequestTradeModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.UserFlag;
import com.zerobudget.bookito.utils.Utils;
import com.zerobudget.bookito.utils.popups.PopupInbox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ChatFragment extends Fragment {
    private FragmentChatBinding binding;
    private UserModel otherUser;
    private DatabaseReference realTimedb;
    private String requestID;
    private RequestModel request;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    private Chat_RecycleViewAdapter adapter;

    protected FirebaseFirestore db;

    private View root;
    int position;

    private final ArrayList<MessageModel> messages = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        root = binding.getRoot();


        this.db = FirebaseFirestore.getInstance();

        progressBar = binding.progressBar;

        progressBar.setVisibility(View.VISIBLE);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Bundle args = getArguments();

        String otherId = args.getString("otherChatUser");
        otherUser = Utils.getGsonParser().fromJson(otherId, UserModel.class);

        String requestModelStr = args.getString("requestModel");
        String requestClassName = args.getString("requestClassName");
        switch (requestClassName) {
            case "RequestShareModel":
                request = new RequestShareModel();
                request = Utils.getGsonParser().fromJson(requestModelStr, RequestShareModel.class);
                break;
            case "RequestTradeModel":
                request = new RequestTradeModel();
                request = Utils.getGsonParser().fromJson(requestModelStr, RequestTradeModel.class);
                break;
            default:
                request = Utils.getGsonParser().fromJson(requestModelStr, RequestModel.class);
                break;
        }

        position = args.getInt("position");

        requestID = args.getString("requestID");
        realTimedb = FirebaseDatabase.getInstance().getReference("/chatapp/" + requestID);

        String otherUserId = args.getString("otherUserId");

        recyclerView = binding.ChatRecycleView;

        adapter = new Chat_RecycleViewAdapter(this.getContext(), messages, otherUser, otherUserId, args.getParcelable("otherUserPic"));

        recyclerView.setAdapter(adapter);
        LinearLayoutManager l = new LinearLayoutManager(this.getContext());
        l.setStackFromEnd(true);
        recyclerView.setLayoutManager(l);

        setUpChatRoom();

        binding.sendMessage.setOnClickListener(view -> {
            String message = binding.inputMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                //inserisce il messaggio nel realtime database

                Date now = Timestamp.now().toDate();

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentTime = sdf.format(now);
                SimpleDateFormat sdf1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String currentDate = sdf1.format(now);

                realTimedb.push().setValue(new MessageModel(Utils.USER_ID, args.getString("otherUserId"), message, "sent", currentTime, currentDate));
                binding.inputMessage.setText("");
            }
        });

        return root;
    }


    /**
     * LO SO CHE SONO DEPRECATI MA FUNZIONANO
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialToolbar toolbar = (MaterialToolbar) getActivity().findViewById(R.id.topAppBar);
        toolbar.setTitle(otherUser.getFirstName() + " " + otherUser.getLastName());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.setGroupVisible(R.id.default_group, false);
        menu.setGroupVisible(R.id.chat_group, true);

        int id_cancel_request_item = menu.findItem(R.id.cancel_request_item).getItemId();
        int id_confirmGiven_request_item = menu.findItem(R.id.confirm_book_given_item).getItemId();

        int id_close_request = menu.findItem(R.id.close_request_item).getItemId();

        if (request instanceof RequestShareModel) {
            if (request.getStatus().equals("ongoing")) {
                menu.removeItem(id_cancel_request_item);
                menu.removeItem(id_confirmGiven_request_item);
            } else
                menu.removeItem(id_close_request);
        } else
            menu.removeItem(id_confirmGiven_request_item);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.info_chat:
                UserModel senderModel = request.getOtherUser();
                if (senderModel != null) {
                    HashMap<String, Object> karma = senderModel.getKarma(); //HashMap<String, Long>
                    Number points = (Number) karma.get("points");
                    Number feedback_numbers = (Number) karma.get("numbers");
                    Flag flag = UserFlag.getFlagFromUser(points, feedback_numbers);
                    createNewContactDialog(flag);

                }
                return true;

            case R.id.confirm_book_given_item:
                confirmBookGivenDialog();
                return true;

            case R.id.close_request_item:
                Toast.makeText(getContext(), "TODO", Toast.LENGTH_LONG).show();
                return true;

            case R.id.cancel_request_item:
                //annullata mentre è ancora in corso


                return true;
            default:
                return false;

        }
    }

    private void confirmBookGivenDialog() {
        Log.d("aaaa", "hei");
        if (!Utils.USER_ID.equals(request.getReceiver()))
            return; //solo chi riceve il libro può confermare di averlo ricevuto
            /*
            da rivedere in ogni caso questo sistema, si potrebbe fare che serva una doppia conferma da parte di entrambi gli utenti
             */
        if (request.getStatus().equals("ongoing")) return;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("Conferma");
        builder.setMessage(Html.fromHtml("Sei sicuro di voler confermare l'inizio del prestito di <br><b>" + request.getTitle() + "</b>", Html.FROM_HTML_MODE_LEGACY));
        builder.setPositiveButton("SI", (dialogInterface, i) -> {
            db.collection("requests").document(request.getRequestId()).update("status", "ongoing").addOnSuccessListener(task -> {
                request.setStatus("ongoing");
            });

            Bundle args = new Bundle();
            args.putInt("position", position);

            //devo usate la navigation per poter passare l'indice della posizione dell'elemento della recycle view
            //per poter notificare l'adapter del cambiamento!
            //TODO rimuovere lo slider fastidioso che fa
            Navigation.findNavController(getView()).navigate(R.id.action_chat_fragment_to_request_page_nav, args);
            dialogInterface.dismiss();
        });
        builder.setNegativeButton("NO", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        builder.show();
    }


    /**
     * visualizza le informazioni relative alla richiesta selezionata
     */
    public void createNewContactDialog(Flag flag) {
        View view = View.inflate(getContext(), R.layout.popup, null);
        //crea il popup tramite la classe PopupInbox hce fornisce i metodi per settarne i valori
        PopupInbox dialogBuilder = new PopupInbox(getContext(), view);
        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();

        dialogBuilder.setUpInformation(request);
        dialogBuilder.setReputationMessage(request, flag);

        if (request instanceof RequestShareModel)
            dialogBuilder.setUpDate((RequestShareModel) request);

        dialogBuilder.setTextConfirmButton("OK, torna indietro");
        dialogBuilder.getConfirmButton().setOnClickListener(view1 -> dialog.dismiss());

        dialogBuilder.getRefuseButton().setVisibility(View.GONE);

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    /**
     * in realtime prende i messaggi dal database e ne permette la visualizzazione sulla chat
     */
    protected void setUpChatRoom() {
        realTimedb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (!dataSnapshot.getKey().equals("user1") && !dataSnapshot.getKey().equals("user2")) {

                        MessageModel msg;
                        if (dataSnapshot.hasChild("isbnBookTrade")) {
                            msg = new MessageModelTrade();
                            ((MessageModelTrade) msg).setIsbnBookTrade(dataSnapshot.child("isbnBookTrade").getValue(String.class));
                            ((MessageModelTrade) msg).setThumbnailBookTrade(dataSnapshot.child("thumbnailBookTrade").getValue(String.class));
                        } else if (dataSnapshot.hasChild("thumbnailBookRequested")) {
                            msg = new MessageModelWithImage();
                            ((MessageModelWithImage) msg).setThumbnailBookRequested(dataSnapshot.child("thumbnailBookRequested").getValue(String.class));
                        } else {
                            msg = new MessageModel();
                        }

                        //se lo status del messaggio dell'altro utente è segnato come sent,
                        //appena l'utente corrente apre la chat esso passa a read
                        if (dataSnapshot.hasChild("status"))
                            if (dataSnapshot.child("receiver").getValue(String.class).equals(Utils.USER_ID)
                                    && dataSnapshot.child("status").getValue(String.class).equals("sent")) {
                                realTimedb.child(dataSnapshot.getKey()).child("status").setValue("read");
                            }

                        msg.setStatus(dataSnapshot.child("status").getValue(String.class));
                        msg.setMessage(dataSnapshot.child("message").getValue(String.class));
                        msg.setSender(dataSnapshot.child("sender").getValue(String.class));
                        msg.setReceiver(dataSnapshot.child("receiver").getValue(String.class));
                        //il timestamp dava problemi brutti perché non ha un costruttore senza argomenti
                        msg.setMessageTime(dataSnapshot.child("messageTime").getValue(String.class));
                        msg.setMessageDate(dataSnapshot.child("messageDate").getValue(String.class));

                        //messages.add(dataSnapshot.getValue(MessageModel.class));
                        messages.add(msg);
                    }
                }
                //checkStatusPreviousMessages();

                adapter.notifyDataSetChanged();

                recyclerView.scrollToPosition(messages.size() - 1);
                recyclerView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("ENTRO", "SONO ENTRATO IN CANCELLAZIONE");
            }
        });
    }
}

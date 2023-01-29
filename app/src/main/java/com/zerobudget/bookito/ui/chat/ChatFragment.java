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
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
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
import com.zerobudget.bookito.utils.popups.PopupInboxTradeAcc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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

    private ValueEventListener eventListener = createEvent();

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
        recyclerView.setLayoutManager(l);

        setUpChatRoom();

        binding.sendMessage.setOnClickListener(view -> {
            String message = binding.inputMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                //inserisce il messaggio nel realtime database
                realTimedb.push().setValue(new MessageModel(Utils.USER_ID, args.getString("otherUserId"), message, "sent", Timestamp.now().getSeconds()));
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
        MaterialToolbar toolbar = requireActivity().findViewById(R.id.topAppBar);
        // TODO: se si cambia tema toolbar diventa null e crasha
        if (toolbar != null)
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
                //libro prestato a chi l'ha richiesto
                confirmBookGivenDialog();
                return true;

            case R.id.close_request_item:
                //richiesta conclusa
                closeRequestDialog();
                return true;

            case R.id.cancel_request_item:
                //annullata mentre è ancora in corso
                cancelRequestDialog();
                return true;
            default:
                return false;

        }
    }

    private void confirmBookGivenDialog() {
        if (Utils.USER_ID.equals(request.getSender())) {
            Toast.makeText(getContext(), "Solo il mittente può confermare", Toast.LENGTH_LONG).show();
            return;
        }

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
            args.putString("type", "changed");

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
     * annulla la richiesta
     * segna la richiesta in corso come cancelled*/
    private void cancelRequestDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle("Conferma");
        builder.setMessage(Html.fromHtml("Sei sicuro di voler annullare la richiesta di <br><b>" + request.getTitle() + "</b>?", Html.FROM_HTML_MODE_LEGACY));
        builder.setPositiveButton("SI", (dialogInterface, i) -> {
            //TODO sistemare i permessi su firebase
            //se è uno scambio  devono tornare disponibili entrambi i libri
            if (request instanceof RequestTradeModel) {
                if (request.getReceiver().equals(Utils.USER_ID)) {
                    Utils.changeBookStatus(db, Utils.USER_ID, request.getRequestedBook(), true);
                    Utils.changeBookStatus(db, request.getSender(), ((RequestTradeModel) request).getRequestTradeBook(), true);
                } else {
                    Utils.changeBookStatus(db, Utils.USER_ID, (((RequestTradeModel) request).getRequestTradeBook()), true);
                    Utils.changeBookStatus(db, request.getReceiver(), request.getRequestedBook(), true);
                }
            } else {
                Utils.changeBookStatus(db, Utils.USER_ID, request.getRequestedBook(), true);
            }

            db.collection("requests").document(request.getRequestId()).update("status", "cancelled");

            Toast.makeText(getContext(), "Richiesta annullata!", Toast.LENGTH_LONG).show();

            Bundle args = new Bundle();
            args.putInt("position", position);
            args.putString("type", "removed");

            Navigation.findNavController(getView()).navigate(R.id.action_chat_fragment_to_request_page_nav, args);

            dialogInterface.dismiss();
        }).setNegativeButton("NO", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        }).show();
    }

    private void closeRequestDialog(){
        if (request instanceof RequestShareModel)
            if (Utils.USER_ID.equals(request.getSender())) {
                Toast.makeText(getContext(), "Solo il mittente può confermare che la richiesta è effettivamente conclusa!", Toast.LENGTH_LONG).show();
                return;  //è solo il sender che può confermare che la richiesta è satta effettivamente conclusa
            }

        if (request instanceof RequestTradeModel) {
            RequestTradeModel r = (RequestTradeModel) request;
            if (Utils.USER_ID.equals(r.getSender())) {
                    /*
                    se l'utente attuale è un sender controllo che il sender non abbia già fatto la richiesta
                    se l'ha fatta allora mando messaggio di errore (e anche non faccio fare display dell'item della selezione dell'azione, non se sa mai)
                    l'utente può comunque vedere la richiesta perché deve poter accedere alla chat fino alla fine
                     */
                if (r.isSenderConfirm()) {
                    Toast.makeText(getContext(), "Hai già confermato questa richiesta!", Toast.LENGTH_LONG).show();
                    return;
                }
            } else if (r.isReceiverConfirm()) {
                Toast.makeText(getContext(), "Hai già confermato questa richiesta!", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            if (!(request instanceof RequestShareModel) && Utils.USER_ID.equals(request.getReceiver())) { //SOLO CHI RICEVE IL LIBRO DEL REGALO PUÒ CONTROLLARE
                Toast.makeText(getContext(), "Solo il mittente può confermare che la richiesta è effettivamente conclusa!", Toast.LENGTH_LONG).show();
                return; //solo chi presta il libro può confermarela richiesta conclusa
            }
        }

        MaterialAlertDialogBuilder builderConfirm = new MaterialAlertDialogBuilder(getContext());
        builderConfirm.setTitle("Conferma");
        builderConfirm.setMessage(Html.fromHtml("Sei sicuro di voler segnare la richiesta di <br><b>" + request.getTitle() + "</b> come conlusa?", Html.FROM_HTML_MODE_LEGACY));
        builderConfirm.setPositiveButton("SI", (dialogInterface, i) -> {
            Date now = Timestamp.now().toDate();
            //richiesta di prestito, conttrolla che sia stata raggiunta la data di fine prestito
            if (request instanceof RequestShareModel && now.compareTo(((RequestShareModel) request).getDate()) < 0)
                Toast.makeText(getContext(), "Attenzione, il prestito non ha ancora superato la data prestabilita!", Toast.LENGTH_LONG).show();
            else {
                //popup recensioni
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
                View view2 = View.inflate(getContext(), R.layout.popup_feedback, null);

                TextView text = view2.findViewById(R.id.text);
                String strTxt = "Dai una recensione da una a cinque stelle a " + request.getOtherUser().getFirstName() + " " + request.getOtherUser().getLastName() +
                        ".\nValuta correttamente, ricorda che il tuo voto influirà sulla sua reputazione.";
                text.setText(strTxt);
                builder.setView(view2);

                AlertDialog starDialog = builder.create();
                starDialog.show();

                RatingBar mRatingBar = view2.findViewById(R.id.rating);
                Button confirmFeedback = view2.findViewById(R.id.feedback_button);

                mRatingBar.setOnRatingBarChangeListener((ratingBar, rating, isUser) -> {
                });

                confirmFeedback.setOnClickListener(click -> {

                    if (mRatingBar.getRating() <= 0) return;

                    String otherUserID = "";
                    if (request.getSender().equals(Utils.USER_ID)) {
                        otherUserID = request.getReceiver();
                    } else {
                        otherUserID = request.getSender();
                    }

                    sendFeedbackToUser(otherUserID, mRatingBar.getRating());
                    starDialog.dismiss();
                    Toast.makeText(getContext(), "Feedback inviato correttamente!", Toast.LENGTH_LONG).show();


                    if (request instanceof RequestTradeModel) {
                        Task<Void> task;
                        if (Utils.USER_ID.equals(request.getSender())) {
                            ((RequestTradeModel) request).setSenderConfirm(true);
                            task = db.collection("requests").document(request.getRequestId()).update("senderConfirm", true);
                        } else {
                            ((RequestTradeModel) request).setReceiverConfirm(true);
                            task = db.collection("requests").document(request.getRequestId()).update("receiverConfirm", true);
                        }
                        task.addOnSuccessListener((unused)->{
                            db.collection("requests").document(request.getRequestId()).get().addOnSuccessListener(documentSnapshot -> {
                                if ((boolean) documentSnapshot.get("senderConfirm") && (boolean) documentSnapshot.get("receiverConfirm")) {
                                    closeRequest();
                                }
                            });
                        });
                    }else {

                        closeRequest();//chiude la richiesta in base al tipo
                    }
                });
            }
            dialogInterface.dismiss();
        }).setNegativeButton("NO", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        }).show();
    }

    /**
     * incrementa il punteggio di red flag dell'utente
     *
     * @param id: id dell'utente di riferimento
     * @param feedback: valore di incremento del feedback dell'utente
     */
    private void sendFeedbackToUser(String id, float feedback) {
        db.collection("users").document(id).update("karma.points", FieldValue.increment(feedback), "karma.numbers", FieldValue.increment(1));
    }

    /**
     * segna la richiesta come conclusa (status = concluded) sulla base del tipo
     * abilita il libro nella richiesta di prestito
     * elimina i libri nelle richieste di scambio e regalo
     */
    private void closeRequest() {
        if (!(request instanceof RequestShareModel)) {
            //la richiesta è segnata con status CONCLUDED
            db.collection("requests").document(request.getRequestId()).update("status", "concluded");

            if (request instanceof RequestTradeModel) {
                //cancella i libri scambiati
                if (request.getReceiver().equals(Utils.USER_ID)) {
                    Utils.deleteUserBook(db, Utils.USER_ID, request.getRequestedBook());
                    Utils.deleteUserBook(db, request.getSender(), ((RequestTradeModel) request).getRequestTradeBook());
                } else {
                    Utils.deleteUserBook(db, Utils.USER_ID, ((RequestTradeModel) request).getRequestTradeBook());
                    Utils.deleteUserBook(db, request.getReceiver(), request.getRequestedBook());
                }
                Toast.makeText(getContext(), "Scambio concluso, libro eliminato dalla libreria!", Toast.LENGTH_LONG).show();
            } else {
                //cancella il libro regalato
                Utils.deleteUserBook(db, request.getReceiver(), request.getRequestedBook());
                Toast.makeText(getContext(), "Regalo concluso, libro eliminato dalla libreria!", Toast.LENGTH_LONG).show();
            }

            //riadattano gli item presenti sullo schermo

            Bundle args = new Bundle();
            args.putInt("position", position);
            args.putString("type", "removed");

            //devo usate la navigation per poter passare l'indice della posizione dell'elemento della recycle view
            //per poter notificare l'adapter del cambiamento!
            //TODO rimuovere lo slider fastidioso che fa
            Navigation.findNavController(getView()).navigate(R.id.action_chat_fragment_to_request_page_nav, args);

        } else {
            //richiesta di prestito
            //cambia lo stato del libro
            Utils.changeBookStatus(db, Utils.USER_ID, request.getRequestedBook(), true);
            //segna la richiesta come conlusa
            db.collection("requests").document(request.getRequestId()).update("status", "concluded");

            Toast.makeText(getContext(), "Prestito concluso, il libro è nuovamente disponibile nella libreria!", Toast.LENGTH_LONG).show();

            Bundle args = new Bundle();
            args.putInt("position", position);
            args.putString("type", "removed");

            //devo usate la navigation per poter passare l'indice della posizione dell'elemento della recycle view
            //per poter notificare l'adapter del cambiamento!
            //TODO rimuovere lo slider fastidioso che fa
            Navigation.findNavController(getView()).navigate(R.id.action_chat_fragment_to_request_page_nav, args);

        }
    }

    /**
     * visualizza le informazioni relative alla richiesta selezionata
     */
    public void createNewContactDialog(Flag flag) {

        View view;
        PopupInbox dialogBuilder;
        if(request instanceof RequestTradeModel) {
            view = View.inflate(getContext(), R.layout.popup_inbox_trade_acc, null);
            dialogBuilder = new PopupInboxTradeAcc(getContext(), view);
            ((PopupInboxTradeAcc) dialogBuilder).setUpInformationTrade((RequestTradeModel) request);
        }else {
            //crea il popup tramite la classe PopupInbox hce fornisce i metodi per settarne i valori
            view = View.inflate(getContext(), R.layout.popup_inbox, null);
            dialogBuilder = new PopupInbox(getContext(), view);
        }
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
        realTimedb.removeEventListener(eventListener);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    /**
     * in realtime prende i messaggi dal database e ne permette la visualizzazione sulla chat
     */
    protected void setUpChatRoom() {
        realTimedb.addValueEventListener(eventListener);
    }

    private ValueEventListener createEvent() {
        return new ValueEventListener() {
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
                        if (dataSnapshot.hasChild("status")) {
                            if (dataSnapshot.child("receiver").getValue(String.class).equals(Utils.USER_ID)
                                    && dataSnapshot.child("status").getValue(String.class).equals("sent")) {
                                realTimedb.child(dataSnapshot.getKey()).child("status").setValue("read");
                            }
                        }

                        msg.setStatus(dataSnapshot.child("status").getValue(String.class));
                        msg.setMessage(dataSnapshot.child("message").getValue(String.class));
                        msg.setSender(dataSnapshot.child("sender").getValue(String.class));
                        msg.setReceiver(dataSnapshot.child("receiver").getValue(String.class));

                        if(dataSnapshot.hasChild("messageSentAt"))
                            msg.setMessageSentAt(dataSnapshot.child("messageSentAt").getValue(long.class));

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
                Log.e("DB ERROR", error.getMessage());
            }
        };
    }
}

package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.lelloman.identicon.view.ClassicIdenticonView;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.Requests.RequestTradeModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.UserFlag;
import com.zerobudget.bookito.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;

public class Inbox_RecycleViewAdapter extends RecyclerView.Adapter<Inbox_RecycleViewAdapter.ViewHolder> {

    protected final Context context;
    protected ArrayList<RequestModel> requests;

    protected Button confirmButton;
    protected Button refuseButton;
    protected TextView titlePopup;
    protected TextView owner;
    protected TextView ownerLocation;
    protected TextView returnDate;
    protected ImageView thumbnail;

    // private AlertDialog.Builder dialogBuilder;
    // private AlertDialog dialog;

    protected FirebaseFirestore db;
    protected FirebaseAuth auth;
    private StorageReference storageRef;


    public Inbox_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests) {
        this.context = ctx;
        this.requests = requests;

        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

    }

    @NonNull
    @Override
    public Inbox_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.recycleview_requests, parent, false);

        return new Inbox_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //TODO GET MORE INFORMATION ABOUT THE REQUESTER (HIS NAME INSTEAD OF HIS ID)
        UserModel senderModel = requests.get(holder.getAdapterPosition()).getOtherUser();
        String idSender = requests.get(holder.getAdapterPosition()).getSender();

        if (senderModel != null) {
            String other_usr = requests.get(holder.getAdapterPosition()).getOtherUser().getFirst_name() + " " + requests.get(position).getOtherUser().getLast_name();
            holder.user_name.setText(other_usr);
        } else
            holder.user_name.setText("undefined");
        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(holder.book_image);
        holder.title.setText(requests.get(holder.getAdapterPosition()).getTitle());
        Log.d("AOAOOAOAOA", requests.get(holder.getAdapterPosition()).getOtherUser().getTelephone());

        if(requests.get(holder.getAdapterPosition()).getOtherUser().isHasPicture()){
            holder.user_gravatar.setVisibility(View.GONE);
            storageRef.child("profile_pics/").listAll().addOnSuccessListener(listResult -> {
                for (StorageReference item : listResult.getItems()) {
                    // All the items under listRef.
                    if (!item.getName().equals(Utils.USER_ID) && item.getName().equals(idSender)) {
                        Log.d("item", item.getName());
                        item.getDownloadUrl().addOnSuccessListener(uri -> {
                            // Utils.setUriPic(uri.toString());
                            Log.d("PIC", Utils.URI_PIC);

                            Picasso.get().load(uri).into(holder.usr_pic);
                            holder.usr_pic.setVisibility(View.VISIBLE);
                            //holder.user_gravatar.setVisibility(View.GONE);

                        }).addOnFailureListener(exception -> {
                            int code = ((StorageException) exception).getErrorCode();
                            if (code == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                holder.user_gravatar.setHash(requests.get(holder.getAdapterPosition()).getOtherUser().getTelephone().hashCode());
                                holder.user_gravatar.setVisibility(View.VISIBLE);
                                holder.usr_pic.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            });
        }else{
            holder.user_gravatar.setHash(requests.get(holder.getAdapterPosition()).getOtherUser().getTelephone().hashCode());
            holder.user_gravatar.setVisibility(View.VISIBLE);
            holder.usr_pic.setVisibility(View.GONE);
        }


        holder.request_selected.setOnClickListener(view -> {
            if (senderModel != null && holder.getAdapterPosition() != -1) {
                HashMap<String, Object> karma = senderModel.getKarma(); //HashMap<String, Long>
                Long points = (Long) karma.get("points");
                Long feedback_numbers = (Long) karma.get("numbers");
                Flag flag = UserFlag.getFlagFromUser(points, feedback_numbers);

                createNewContactDialog(position, holder, flag);

            }
        });


    }

    //TODO DALLE RIGHE 113-127 C'È MOLTA RIPETIZIONE DI CODICE, MEGLIO FARE UN METOOD A POSTA DA RICBHIAMARE
    protected void loadPopupViewMembers(View view) {
        confirmButton = view.findViewById(R.id.acceptButton);
        refuseButton = view.findViewById(R.id.refuseButton);
        titlePopup = view.findViewById(R.id.title_popup);
        owner = view.findViewById(R.id.user);
        ownerLocation = view.findViewById(R.id.user_location);
        returnDate = view.findViewById(R.id.return_date);
        thumbnail = view.findViewById(R.id.imageView);
    }

    public void createNewContactDialog(int position, ViewHolder holder, Flag flag) {
        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(context);

        View view = View.inflate(context, R.layout.popup, null);

        dialogBuilder.setView(view);
        AlertDialog dialog = dialogBuilder.create();

        loadPopupViewMembers(view);
        String requestTypeStr = "Richiesta " + requests.get(holder.getAdapterPosition()).getType();
        titlePopup.setText(requestTypeStr);
        String firstAndLastNameStr = requests.get(holder.getAdapterPosition()).getOtherUser().getFirst_name() + " " + requests.get(holder.getAdapterPosition()).getOtherUser().getLast_name();
        owner.setText(firstAndLastNameStr);
        ownerLocation.setText(requests.get(holder.getAdapterPosition()).getOtherUser().getNeighborhood());

        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(thumbnail);


        //TODO: sistemare la data del prestito
        if (requests.get(holder.getAdapterPosition()).getType().equals("Prestito"))
            returnDate.setVisibility(View.VISIBLE);


        if (requests.get(holder.getAdapterPosition()) instanceof RequestTradeModel) {
            confirmButton.setText("Libreria Utente");
        }

        confirmButton.setOnClickListener(view1 -> {
            Log.d("Pos", "" + position);
            if (holder.getAdapterPosition() != -1) {
                if (requests.get(holder.getAdapterPosition()) instanceof RequestTradeModel) {
//                Navigation.findNavController(holder.itemView).navigate(R.layout.fragment_add);
                    //todo creare fragment per spostarci nella libreria dell'altro utente (OSLO LIBRI SCAMBIABII)
                }
                acceptRequest(requests.get(holder.getAdapterPosition()));
                requests.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());

                // notifyItemRangeChanged(holder.getAdapterPosition(), requests.size());
            }
            dialog.dismiss();
        });

        refuseButton.setOnClickListener(view1 -> {
            if (holder.getAdapterPosition() != -1) {
                deleteRequest(requests.get(holder.getAdapterPosition()));
                requests.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                // notifyItemRangeChanged(holder.getAdapterPosition(), requests.size());
            }
            dialog.dismiss();
        });
        dialog.show();

    }

    protected void deleteRequest(RequestModel r) {
        db.collection("requests").document(r.getrequestId()).delete();
    }

    protected void acceptRequest(RequestModel r) {
        db.collection("requests").document(r.getrequestId()).update("status", "accepted");
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected final TextView title;
        protected final ConstraintLayout request_selected;
        protected final TextView user_name;
        protected final ImageView book_image;
        protected final ClassicIdenticonView user_gravatar;
        protected final ImageView usr_pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.requestTitle);
            user_name = itemView.findViewById(R.id.requester_name);
            book_image = itemView.findViewById(R.id.book_image_request);
            request_selected = itemView.findViewById(R.id.request);
            user_gravatar = itemView.findViewById(R.id.user_gravatar);
            usr_pic = itemView.findViewById(R.id.profile_pic);
        }
    }
}

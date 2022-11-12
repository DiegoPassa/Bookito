package com.zerobudget.bookito.ui.inbox;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.ui.Requests.RequestModel;
import com.zerobudget.bookito.ui.Requests.RequestTradeModel;
import com.zerobudget.bookito.ui.users.UserModel;

import java.util.ArrayList;
import java.util.HashMap;

public class Inbox_RecycleViewAdapter extends RecyclerView.Adapter<Inbox_RecycleViewAdapter.ViewHolder>{
    //sss
    private final Context context;
    private ArrayList<RequestModel> requests;

    private final Long MIN_FEEDBACKS_FLAG = 8l;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button closeButton;

    FirebaseFirestore db;
    FirebaseAuth auth;

    public Inbox_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests) {
        this.context = ctx;
        this.requests = requests;

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
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
        UserModel senderModel = requests.get(position).getOtherUser();
        if (senderModel != null)
            holder.user_name.setText(requests.get(position).getOtherUser().getFirst_name());
        else holder.user_name.setText("undefined");
        Picasso.get().load(requests.get(position).getThumbnail()).into(holder.book_image);
        holder.title.setText(requests.get(position).getTitle());

        holder.request_selected.setOnClickListener(view -> {
            if (senderModel != null) {
                HashMap<String, Object> karma = senderModel.getKarma(); //HashMap<String, Long>
                Long points = (Long) karma.get("points");
                Long feedback_numbers = (Long) karma.get("numbers");
                Flag flag = getFlagFromUser(points, feedback_numbers);

                createNewContactDialog(position, holder, flag);

            }

//            RequestModel r2 = requests.get(0);
//            r2.setTitle("CIAONEEE");
//            acceptRequest(requests.get(0));
//            try {
//                requests.set(0, r2);
//                notifyItemChanged(0);
//                Log.d("ENTROOO", "SIIIIII");
//            }catch(Exception e) {
//
//            }
        });


    }

    private Flag getFlagFromUser(Long points, Long feedbacks) {
        /*
        TODO fare in modo che se un utente ha tanti punti e pochi feedbak, o viceversa
        allora ritorna una NORMAL FLAG

        TODO se un utente ha tanti feedback e pochi punti allora ritorna una RED FLAG

        TODO se un utente ha tanti feedback e tanti punti allora è una GREEN FLAG
        TODO se un utente è "bilanciato" ritorna una normal flag
         */
        if (feedbacks > MIN_FEEDBACKS_FLAG) {
            //per ora facciamo che "tanti feedback" equivalgono a 8
            Long total_points = points / feedbacks;

            if (total_points < 0.5) return Flag.RED_FLAG;
            else if (total_points >= 0.8) return Flag.GREEN_FLAG;
            return Flag.NORMAL_FLAG;

        }
        return Flag.NORMAL_FLAG;

    }

    public void createNewContactDialog(int position, ViewHolder holder, Flag flag) {
        dialogBuilder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.popup, null);

        Button confirmButton = view.findViewById(R.id.acceptButton);
        Button refuseButton = view.findViewById(R.id.refuseButton);
        TextView titlePopup = view.findViewById(R.id.title_popup);
        TextView owner = view.findViewById(R.id.user);
        TextView ownerLocation = view.findViewById(R.id.user_location);
        TextView returnDate = view.findViewById(R.id.return_date);
        ImageView thumbnail = view.findViewById(R.id.imageView);


        String requestTypeStr = "Richiesta "+requests.get(holder.getAdapterPosition()).getType();
        titlePopup.setText(requestTypeStr);
        String firstAndLastNameStr = requests.get(holder.getAdapterPosition()).getOtherUser().getFirst_name()+" "+requests.get(holder.getAdapterPosition()).getOtherUser().getLast_name();
        owner.setText(firstAndLastNameStr);
        ownerLocation.setText(requests.get(holder.getAdapterPosition()).getOtherUser().getNeighborhood());

        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(thumbnail);

        //TODO: sistemare la data del prestito
        if(requests.get(holder.getAdapterPosition()).getType().equals("Prestito"))
            returnDate.setVisibility(View.VISIBLE);


        if (requests.get(holder.getAdapterPosition()) instanceof RequestTradeModel) {
            confirmButton.setText("Libreria Utente");
        }

        confirmButton.setOnClickListener(view1 -> {
            Log.d("Pos", ""+position);
            if (holder.getAdapterPosition() != -1) {
                if (requests.get(holder.getAdapterPosition()) instanceof RequestTradeModel) {
//                Navigation.findNavController(holder.itemView).navigate(R.layout.fragment_add);
                    //todo creare fragment per spostarci nella libreria dell'altro utente (OSLO LIBRI SCAMBIABII)
                }
                acceptRequest(requests.get(holder.getAdapterPosition()));
                requests.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());

                // notifyItemRangeChanged(holder.getAdapterPosition(), requests.size());

                dialog.hide();
            }
        });

        refuseButton.setOnClickListener(view1 -> {
            if (holder.getAdapterPosition() != -1) {
                refuseRequest(requests.get(holder.getAdapterPosition()));
                requests.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
                // notifyItemRangeChanged(holder.getAdapterPosition(), requests.size());
                dialog.hide();
            }
        });

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();

    }

    private void refuseRequest(RequestModel r) {
//        db.collection("requests").document(r.getrequestId()).delete();
    }

    private void acceptRequest(RequestModel r) {
//        db.collection("requests").document(r.getrequestId()).update("title", "SOOOKA");
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private final TextView title;
        private final ConstraintLayout request_selected;
        private final TextView user_name;
        private final ImageView book_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.requestTitle);
            user_name = itemView.findViewById(R.id.requester_name);
            book_image= itemView.findViewById(R.id.book_image_request);
            request_selected = itemView.findViewById(R.id.request);
        }
    }
}

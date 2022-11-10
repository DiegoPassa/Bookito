package com.zerobudget.bookito.ui.inbox;

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
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.ui.Requests.RequestModel;
import com.zerobudget.bookito.ui.users.UserModel;

import java.util.ArrayList;
import java.util.HashMap;

public class Inbox_RecycleViewAdapter extends RecyclerView.Adapter<Inbox_RecycleViewAdapter.ViewHolder>{

    private final Context context;
    private ArrayList<RequestModel> requests;
    private final Long MIN_FEEDBACKS_FLAG = 8l;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private Button confirmButton;
    private Button refuseButton;
    private Button closeButton;



    public Inbox_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests) {
        this.context = ctx;
        this.requests = requests;
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
        UserModel senderModel = requests.get(position).getSenderModel();
        if (senderModel != null)
            holder.user_name.setText(requests.get(position).getSenderModel().getFirst_name());
        else holder.user_name.setText("undefined");
        Picasso.get().load(requests.get(position).getThumbnail()).into(holder.book_image);
        holder.title.setText(requests.get(position).getTitle());

        holder.request_selected.setOnClickListener(view -> {
            if (senderModel != null) {
                createNewContactDialog();
//                HashMap<String, Object> karma = senderModel.getKarma(); //HashMap<String, Long>
//                Long points = (Long) karma.get("points");
//                Long feedback_numbers = (Long) karma.get("numbers");
//                Flag flag = getFlagFromUser(points, feedback_numbers);
//
//                switch (flag) {
//                    case GREEN_FLAG: Log.d("AAAAA", "GREEEN FLAG"); break;
//                    case RED_FLAG: Log.d("AAAAAA", "RED_FLAG"); break;
//                    case NORMAL_FLAG: Log.d("AAAAAA", "FLAG NORMALE"); break;
//                    default: Log.d("UNDEFINED", "aaaaa");
//                }

            }
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

    public void createNewContactDialog() {
//        dialogBuilder = new AlertDialog.Builder(context);
//        final View contactPopupView = View.inflate(context, R.layout.popup, null);
//        closeButton = (Button) contactPopupView.findViewById(R.id.closeButton);
//        confirmButton = (Button) contactPopupView.findViewById(R.id.acceptButton);
//        refuseButton = (Button) contactPopupView.findViewById(R.id.refuseButton);
//
//        dialogBuilder.setView(contactPopupView);
//        dialog = dialogBuilder.create();
//        dialog.show();
//
//
//        closeButton.setOnClickListener(view -> {
//            dialog.hide();
//        });
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Conferma");
//        builder.setMessage("Libro ");
//        builder.setPositiveButton("OK", (dialogInterface, i) -> {
//            dialogInterface.dismiss();
//        }).show();


        dialogBuilder = new AlertDialog.Builder(context);
        View view = View.inflate(context, R.layout.popup, null);

        confirmButton = view.findViewById(R.id.acceptButton);

        confirmButton.setOnClickListener(view1 -> {

        });

        dialogBuilder.setView(view);
        dialog = dialogBuilder.create();
        dialog.show();
        dialog.getWindow().setLayout(1080, 1080);

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

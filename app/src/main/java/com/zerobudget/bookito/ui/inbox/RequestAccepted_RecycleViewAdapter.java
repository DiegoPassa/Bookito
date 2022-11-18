package com.zerobudget.bookito.ui.inbox;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.zerobudget.bookito.Flag;
import com.zerobudget.bookito.R;
import com.zerobudget.bookito.models.Requests.RequestModel;
import com.zerobudget.bookito.models.users.UserModel;
import com.zerobudget.bookito.utils.UserFlag;
import com.zerobudget.bookito.utils.Utils;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class RequestAccepted_RecycleViewAdapter extends Inbox_RecycleViewAdapter{
    private StorageReference storageRef;

    public RequestAccepted_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests) {
        super(ctx, requests);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel otherUser = requests.get(holder.getAdapterPosition()).getOtherUser();

        if (otherUser != null) {
            String nameOtherUser = otherUser.getFirst_name();
            String surnameOtherUser = otherUser.getLast_name();

            if (isCurrentUserReceiver(requests.get(holder.getAdapterPosition()))) {
                holder.user_name.setText(nameOtherUser + " " + surnameOtherUser);
            } else {
                holder.user_name.setText(Html.fromHtml("<b> ( TU ) -> </b>" +nameOtherUser + surnameOtherUser, Html.FROM_HTML_MODE_LEGACY));
            }
        }

        holder.title.setText(requests.get(holder.getAdapterPosition()).getTitle());
        Picasso.get().load(requests.get(holder.getAdapterPosition()).getThumbnail()).into(holder.book_image);

        storageRef.child("profile_pics/").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for (StorageReference prefix : listResult.getPrefixes()) {
                            // Log.d("PRE", prefix.getName());
                            // All the prefixes under listRef.
                            // You may call listAll() recursively on them.
                        }

                        for (StorageReference item : listResult.getItems()) {
                            // All the items under listRef.
                            if (item.getName().equals(requests.get(holder.getAdapterPosition()).getReceiver())
                            || item.getName().equals(requests.get(holder.getAdapterPosition()).getSender())) {
                                Log.d("item", item.getName());
                                item.getDownloadUrl().addOnSuccessListener(uri -> {
                                    // Utils.setUriPic(uri.toString());
                                    Log.d("PIC", Utils.URI_PIC);

                                    Picasso.get().load(uri).into(holder.usr_pic);
                                    holder.usr_pic.setVisibility(View.VISIBLE);
                                    holder.user_gravatar.setVisibility(View.GONE);

                                }).addOnFailureListener(exception -> {
                                    int code = ((StorageException) exception).getErrorCode();
                                    if (code == StorageException.ERROR_OBJECT_NOT_FOUND) {
                                        holder.user_gravatar.setHash(requests.get(holder.getAdapterPosition()).getOtherUser().getTelephone().hashCode());
                                        holder.user_gravatar.setVisibility(View.VISIBLE);
                                        holder.usr_pic.setVisibility(View.GONE);
                                    }
                                });
                            }else{
                                holder.user_gravatar.setHash(requests.get(holder.getAdapterPosition()).getOtherUser().getTelephone().hashCode());
                                holder.user_gravatar.setVisibility(View.VISIBLE);
                                holder.usr_pic.setVisibility(View.GONE);
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                        // Uh-oh, an error occurred!
                    }
                });

        holder.request_selected.setOnClickListener(view1 -> {
            if (otherUser != null && holder.getAdapterPosition() != -1) {
                if (isCurrentUserReceiver(requests.get(holder.getAdapterPosition())))
                    createNewContactDialog(holder.getAdapterPosition(), holder, UserFlag.getFlagFromUser((Long) otherUser.getKarma().get("numbers"), (Long) otherUser.getKarma().get("points")));
                else createNewContactDialog(holder.getAdapterPosition(), holder, null);

            }
        });
    }

    @Override
    public void createNewContactDialog(int position, ViewHolder holder, Flag user) {
        Bundle args = new Bundle();
        String toJson = Utils.getGsonParser().toJson(requests.get(holder.getAdapterPosition()));
        args.putString("otherChatUser", toJson);
    }

    protected boolean isCurrentUserReceiver(RequestModel r) {
        return r.getReceiver().equals(Utils.USER_ID);
    }
}

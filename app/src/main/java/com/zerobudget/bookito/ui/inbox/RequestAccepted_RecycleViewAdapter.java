package com.zerobudget.bookito.ui.inbox;

import android.content.Context;

import com.zerobudget.bookito.models.Requests.RequestModel;

import java.util.ArrayList;

public class RequestAccepted_RecycleViewAdapter extends Inbox_RecycleViewAdapter{
    public RequestAccepted_RecycleViewAdapter(Context ctx, ArrayList<RequestModel> requests) {
        super(ctx, requests);
    }
}

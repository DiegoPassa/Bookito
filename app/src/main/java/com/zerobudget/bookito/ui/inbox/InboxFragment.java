package com.zerobudget.bookito.ui.inbox;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.zerobudget.bookito.models.requests.RequestModel;

import java.util.ArrayList;

public abstract class InboxFragment extends Fragment {
    protected ArrayList<RequestModel> requests = new ArrayList<>();
    protected RecyclerView recyclerView;
}

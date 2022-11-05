package com.zerobudget.bookito.ui.add;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private int score = 0;

    public AddViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public int getScore(){
        return score;
    }

    public void plusScore(){
        ++score;
    }

    public void subScore(){
        --score;
    }

}
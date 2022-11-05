package com.zerobudget.bookito.ui.add;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AddViewModel extends ViewModel {

    private final MutableLiveData<String> mText;
    private MutableLiveData<String> score = new MutableLiveData<>("0");

    public AddViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is notifications fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getScore() {
        return score;
    }

    public void subScore() {
        int tmp = Integer.parseInt(score.getValue());
        tmp--;
        score.setValue(Integer.toString(tmp));
    }

    public void plusScore() {
        int tmp = Integer.parseInt(score.getValue());
        tmp++;
        score.setValue(Integer.toString(tmp));
    }
}
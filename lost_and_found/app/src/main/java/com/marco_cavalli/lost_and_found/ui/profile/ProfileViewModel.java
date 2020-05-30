package com.marco_cavalli.lost_and_found.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class ProfileViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ProfileViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    public LiveData<String> getText() {
        return mText;
    }
}
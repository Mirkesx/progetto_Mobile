package com.marco_cavalli.lost_and_found.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ShowUserProfile extends AppCompatActivity {

    private String uid, user_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data_intent = getIntent();
        Bundle data_bundle = data_intent.getExtras();

        uid = data_bundle.get("uid").toString();
        user_id = data_bundle.get("user_id").toString();
    }
}

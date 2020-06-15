package com.marco_cavalli.lost_and_found.ui.base;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.marco_cavalli.lost_and_found.R;

public class ShowPolicy extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_policy);
        String policy = getText(R.string.policy).toString();
        String[] lines = policy.replace("-","_-").split("_");
        TextView tw = findViewById(R.id.policy_text);
        policy = "";
        for(int i = 0; i < lines.length; i++)
            policy += lines[i] + "\n";
        tw.setText(policy);
    }
}

package com.marco_cavalli.lost_and_found.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.marco_cavalli.lost_and_found.R;

public class CreatePosition extends AppCompatActivity {

    private String object_id;
    private String uid;
    private EditText editDesc;
    private TextView textDate, textLat, textLon, getGPS, updateButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_update_object_position);

        //Retrieving data
        Intent inte = getIntent();
        Bundle data =  inte.getExtras();
        object_id = data.getString("object_id");
        uid = data.getString("uid");

        //Initialize elements
        editDesc = findViewById(R.id.new_position_description);
        textDate = findViewById(R.id.new_position_date);
        textLat = findViewById(R.id.new_poition_lat);
        textLon = findViewById(R.id.new_position_lon);
        getGPS = findViewById(R.id.new_position_latlon_button);
        updateButton = findViewById(R.id.new_position_submit);

        updateButton.setOnClickListener(v -> {
            Intent data_intent = new Intent();
            if(editDesc.getText().toString().length() > 0)
                data_intent.putExtra("description", editDesc.getText().toString());
            else
                data_intent.putExtra("description", "");
            data_intent.putExtra("date",textDate.getText().toString());
            if(textLat.getText().toString().length() > 0)
                data_intent.putExtra("latitude", textLat.getText().toString());
            else
                data_intent.putExtra("latitude", "0");
            if(textLon.getText().toString().length() > 0)
                data_intent.putExtra("longitude", textLat.getText().toString());
            else
                data_intent.putExtra("longitude", "0");
            setResult(Activity.RESULT_OK,data_intent);
            finish();
        });
    }
}

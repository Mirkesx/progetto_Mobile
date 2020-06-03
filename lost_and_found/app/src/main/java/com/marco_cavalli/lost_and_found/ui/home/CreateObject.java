package com.marco_cavalli.lost_and_found.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.marco_cavalli.lost_and_found.R;

public class CreateObject extends AppCompatActivity {

    private EditText name;
    private EditText description;
    private ImageView camera, gallery, image;
    private Button create;
    private Toolbar myToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_create_object);


        //ELEMENTS
        name = findViewById(R.id.home_create_name_edit);
        description = findViewById(R.id.home_create_description_edit);
        image = findViewById(R.id.home_create_image);
        camera = findViewById(R.id.home_create_camera);
        gallery = findViewById(R.id.home_create_gallery);
        create = findViewById(R.id.home_create_button);
        myToolbar = findViewById(R.id.home_create_toolbar);

        //LISTENERS
        create.setOnClickListener(v -> {
            if(name.getText() == null || name.getText().toString().length() > 0) {
                Intent data = new Intent();
                data.putExtra("name",name.getText().toString());
                if(description.getText() != null)
                    data.putExtra("description",description.getText().toString());
                else
                    data.putExtra("description","");
                setResult(Activity.RESULT_OK, data);
                finish();
            }
            else {
                Toast.makeText(this, getString(R.string.home_create_missing_name), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

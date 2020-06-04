package com.marco_cavalli.lost_and_found.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.marco_cavalli.lost_and_found.R;

public class CreateObject extends AppCompatActivity {

    private EditText name;
    private EditText description;
    private ImageView camera, gallery, image;
    private Button create;

    static final int REQUEST_IMAGE_CAPTURE = 1;

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
        setTitle(getString(R.string.home_create_toolbar_title));

        image.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_insert_photo_128,null));

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

        camera.setOnClickListener(v -> {
            if(checkDeviceCompatibility()) {
                dispatchTakePictureIntent();
            }
        });
    }

    public boolean checkDeviceCompatibility() {

        PackageManager pm = this.getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                return true;
            } else {
                // use front camera
                Toast.makeText(
                        this,
                        getString(R.string.camera_no_back_camera),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            Toast.makeText(
                    this,
                    getString(R.string.camera_no_camera),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void dispatchTakePictureIntent() {
        Log.d("CAMERA_TEST","Opening camera");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image.setImageBitmap(imageBitmap);
        }
    }

    
}

package com.marco_cavalli.lost_and_found.ui.found_objects;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.FoundItem;

import java.io.File;
import java.util.Map;

public class ShowInsertion extends AppCompatActivity {
    private TextView textObjectName, textDescription, textDate, textAddress, textLatitude, textLongitude;
    private ImageView imageView;
    private Button button;
    private ImageButton maps;
    private String uid, insertion_id;
    private FoundItem insertion;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.found_lost_show_insertion);

        Intent data_intent = getIntent();
        Bundle data_bundle = data_intent.getExtras();
        uid = data_bundle.get("uid").toString();
        insertion_id = data_bundle.get("insertion_id").toString();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        insertion = null;

        //Initialize elements
        textObjectName = findViewById(R.id.found_lost_show_object_name_edit);
        textDescription = findViewById(R.id.found_lost_show_description_edit);
        textDate = findViewById(R.id.found_lost_show_date_value);
        textAddress = findViewById(R.id.found_lost_address);
        textLatitude = findViewById(R.id.found_lost_show_latitude);
        textLongitude = findViewById(R.id.found_lost_show_longitude);
        imageView = findViewById(R.id.found_lost_show_image);
        maps = findViewById(R.id.found_lost_maps);
        setInsertion();

        maps.setOnClickListener(v -> {
            Uri gmmIntentUri;
            if(insertion != null)
                gmmIntentUri = Uri.parse("geo:0,0?q="+insertion.getLatitude()+","+insertion.getLongitude()+"("+insertion.getObject_name()+")");
            else {
                Toast.makeText(this, getString(R.string.gallery_error), Toast.LENGTH_LONG).show();
                return;
            }

            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            // Make the Intent explicit by setting the Google Maps package
            mapIntent.setPackage("com.google.android.apps.maps");

            // Attempt to start an activity that can handle the Intent
            startActivity(mapIntent);
        });
    }

    private void setInsertion() {
        DatabaseReference myRef = database.getReference();
        ValueEventListener foundItemListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, ?> data = ((Map<String,Object>) dataSnapshot.getValue());
                if(data != null && data.get(insertion_id) != null) {
                    data = (Map<String, ?>) data.get(insertion_id);
                    String user_id, user_name, date, icon, object_name, description, address, timestamp;
                    user_id = user_name = date = icon = object_name = description = address = timestamp = "";
                    Double latitude = 0.0, longitude = 0.0;
                    Boolean setFound = false;

                    if(data.get("user_id") != null)
                        user_id = data.get("user_id").toString();

                    if(data.get("user_name") != null)
                        user_name = data.get("user_name").toString();

                    if(data.get("date") != null)
                        date = data.get("date").toString();

                    if(data.get("icon =") != null)
                        icon = data.get("icon").toString();

                    if(data.get("object_name") != null)
                        object_name = data.get("object_name").toString();

                    if(data.get("description") != null)
                        description = data.get("description").toString();

                    if(data.get("address") != null)
                        address = data.get("address").toString();

                    if(data.get("timestamp") != null)
                        timestamp = data.get("timestamp").toString();

                    if(data.get("latitude") != null)
                        latitude = Double.parseDouble(data.get("latitude").toString());

                    if(data.get("longitude") != null)
                        longitude = Double.parseDouble(data.get("longitude").toString());

                    if(data.get("setFound") != null)
                        setFound = Boolean.parseBoolean(data.get("setFound").toString());

                    insertion = new FoundItem(insertion_id,user_id,user_name,date,icon,object_name,description,address,latitude,longitude,timestamp,setFound);

                    if(uid.equals(user_id)) {
                        button = findViewById(R.id.found_lost_setBoolean);
                    }
                    else {
                        button = findViewById(R.id.found_lost_show_user);
                    }
                    button.setVisibility(View.VISIBLE);

                    setValues();

                    if(icon.length() > 0)
                        setImageView(icon);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("founds").addValueEventListener(foundItemListener);
    }

    private void setValues() {
        textObjectName.setText(insertion.getObject_name());
        textDescription.setText(insertion.getDescription());
        textDate.setText(insertion.getDate());
        textAddress.setText(insertion.getAddress());
        textLatitude.setText(""+insertion.getLatitude());
        textLongitude.setText(""+insertion.getLongitude());
    }

    private void setImageView(String path) {
        File file = new File(getFilesDir()+"/founds_images",path);
        if(file.exists()) {
            imageView.setImageURI(Uri.fromFile(file));
        }
        else {
            StorageReference storageRef = storage.getReference();
            StorageReference islandRef = storageRef.child("founds/"+path);
            File newFile = new File(getFilesDir()+"/founds_images",path);

            islandRef.getFile(newFile).addOnSuccessListener(taskSnapshot -> {
                imageView.setImageURI(Uri.fromFile(newFile));
            }).addOnFailureListener(exception -> {
                exception.printStackTrace();
            });
        }
    }
}

package com.marco_cavalli.lost_and_found.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;
import com.marco_cavalli.lost_and_found.objects.Position;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;

import java.util.HashMap;
import java.util.Map;

public class ShowObject extends AppCompatActivity {

    private static final int RC_SHOW_POSITIONS = 23;
    private static final int RC_ADD_POSITION = 24;
    private String object_id;
    private String uid;
    private String name;
    private TextView nameView, descriptionView, lastpositionView;
    private ImageView imageView;
    private PersonalObject obj;
    private FirebaseDatabase database;
    private ImageButton mapView;
    private Button updatePosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_show_object);

        //Retrieving data
        Intent inte = getIntent();
        Bundle data =  inte.getExtras();
        object_id = data.getString("object_id");
        name = data.getString("name");
        uid = data.getString("uid");

        //Initializing objects
        database = FirebaseDatabase.getInstance();
        nameView = findViewById(R.id.home_show_name_value);
        descriptionView = findViewById(R.id.home_show_description_value);
        lastpositionView = findViewById(R.id.home_show_last_position_description);
        imageView = findViewById(R.id.home_show_image);
        mapView = findViewById(R.id.home_show_last_position_map);
        updatePosition = findViewById(R.id.home_show_object_add);
        findViewById(R.id.home_show_last_position_container).setVisibility(View.GONE);
        setTitle(name);

        mapView.setOnClickListener(v -> {
            Position pos = obj.getLastPosition();
            // Create a Uri from an intent string. Use the result to create an Intent.
            Uri gmmIntentUri;
            if(pos != null)
                gmmIntentUri = Uri.parse("geo:0,0?q="+pos.getLatitude()+","+pos.getLongitude()+"("+obj.getName()+")");
            else
                gmmIntentUri = Uri.parse("geo:0,0?q=-33.8666,151.1957(Google+Sydney)");

            // Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            // Make the Intent explicit by setting the Google Maps package
            mapIntent.setPackage("com.google.android.apps.maps");

            // Attempt to start an activity that can handle the Intent
            startActivity(mapIntent);
        });

        updatePosition.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreatePosition.class);
            intent.putExtra("uid",uid);
            intent.putExtra("object_id",object_id);
            startActivityForResult(intent,RC_ADD_POSITION);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getOBJS();
    }

    private void getOBJS() {
        DatabaseReference myRef = database.getReference();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, ?> data = ((Map<String,Object>) dataSnapshot.getValue());
                if(data != null && data.get(uid) != null) {
                    data = ((Map<String, ?>) data.get(uid));
                    if(data != null && data.get("objs") != null) {
                        data = ((Map<String, ?>) data.get("objs"));
                        if(data != null && data.get(object_id) != null) {
                            data = ((Map<String, ?>) data.get(object_id));
                            Image icon = null;
                            if(data.get("icon") != null)
                                icon = ((Image) data.get("icon"));
                            String name = data.get("name").toString();
                            String description = data.get("description").toString();
                            String object_id = data.get("object_id").toString();
                            Map<String, Position> positions = new HashMap<>();
                            if(data.get("positions") != null) {
                                for(Map.Entry<String, ?> entry : ((Map<String, ?>) data.get("positions")).entrySet()) {
                                    String pos_id = ((Map) entry.getValue()).get("pos_id").toString();
                                    String descr = ((Map) entry.getValue()).get("description").toString();
                                    String date = null;
                                    if(((Map) entry.getValue()).get("date") != null) {
                                        date = ((Map) entry.getValue()).get("date").toString();
                                    }
                                    Double latitude = 0.0;
                                    if(((Map) entry.getValue()).get("latitude") != null) {
                                        latitude = Double.parseDouble(((Map) entry.getValue()).get("latitude").toString());
                                    }
                                    Double longitude = 0.0;
                                    if(((Map) entry.getValue()).get("longitude") != null) {
                                        longitude = Double.parseDouble(((Map) entry.getValue()).get("longitude").toString());
                                    }
                                    Position pos = new Position(pos_id, date, descr, latitude, longitude);
                                    positions.put(pos_id,pos);
                                }
                            }
                            obj = new PersonalObject(icon, name, description, positions, object_id);
                        }
                    }
                }

                setValues();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("users").addListenerForSingleValueEvent(userListener);
    }

    private void setValues() {
        nameView.setText(obj.getName());
        descriptionView.setText(obj.getDescription());
        Position pos = obj.getLastPosition();
        if(pos != null) {
            findViewById(R.id.home_show_last_position_container).setVisibility(View.VISIBLE);
            lastpositionView.setText(pos.getDescription());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_show_option_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SHOW_POSITIONS) {
            getOBJS();
            setValues();
        } else if(requestCode == RC_ADD_POSITION && resultCode == Activity.RESULT_OK) {
            String description = data.getStringExtra("description");
            String date = data.getStringExtra("date");
            String pos_id = uid+date.replace("/","");
            Double latitude = Double.parseDouble(data.getStringExtra("description"));
            Double longitude = Double.parseDouble(data.getStringExtra("description"));
            Position pos = new Position(pos_id,date,description,latitude,longitude);

            obj.getPositions().put(pos_id,pos);

            DatabaseReference myRef = database.getReference();
            myRef.child("users").child(uid).child("objs").child(object_id).setValue(obj);
            setValues();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(getString(R.string.home_all_locations))) {
            Intent intent = new Intent(this, ShowPositions.class);
            intent.putExtra("uid", uid);
            intent.putExtra("object_id", object_id);
            startActivityForResult(intent, RC_SHOW_POSITIONS);
        }
        else if(item.getTitle().equals(getString(R.string.home_edit))) {

        }
        else if(item.getTitle().equals(getString(R.string.home_delete))) {
            Intent data = new Intent();
            data.putExtra("object_id", obj.getObject_id());
            setResult(Activity.RESULT_OK, data);
            finish();
        }
        return true;
    }
}
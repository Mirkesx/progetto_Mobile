package com.marco_cavalli.lost_and_found.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.Position;

import java.util.ArrayList;
import java.util.Map;

public class ShowPositions extends AppCompatActivity {

    private String uid;
    private String object_id;
    private ArrayList<Position> positions;
    private ListView myList;
    private AllPositionsCustomAdapter allPositionsCustomAdapter;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_show_all_positions);

        //Retrieving data
        Intent intent = getIntent();
        Bundle data = intent.getExtras();
        uid = data.getString("uid");
        object_id = data.getString("object_id");

        //Setting elements
        myList = findViewById(R.id.home_all_positions_list);
        database = FirebaseDatabase.getInstance();

        //CUSTOM ADAPTER
        int resID = R.layout.positions_custom_list_item;
        positions = new ArrayList<>();
        allPositionsCustomAdapter = new AllPositionsCustomAdapter(this, resID, positions);
        myList.setAdapter(allPositionsCustomAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getOBJS();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_all_position_option_menu, menu);
        return true;
    }

    

    private void getOBJS() {
        DatabaseReference myRef = database.getReference();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, ?> data = ((Map<String,Object>) dataSnapshot.getValue());
                if(data != null && data.get("positions") != null) {
                    positions.clear();
                    for(Map.Entry<String, ?> entry : ((Map<String, ?>) data.get("positions")).entrySet()) {
                        Double latitude = null, longitude = null;
                        String date = null;
                        String description = null;
                        String pos_id;

                        pos_id = ((Map) entry.getValue()).get("pos_id").toString();

                        if(((Map) entry.getValue()).get("description") != null)
                            description = ((Map) entry.getValue()).get("description").toString();

                        if(((Map) entry.getValue()).get("date") != null)
                            date = ((Map) entry.getValue()).get("date").toString();

                        if(((Map) entry.getValue()).get("latitude") != null)
                            latitude = Double.parseDouble(((Map) entry.getValue()).get("latitude").toString());

                        if(((Map) entry.getValue()).get("longitude") != null)
                            longitude = Double.parseDouble(((Map) entry.getValue()).get("longitude").toString());

                        Position pos = new Position(pos_id, date, description, latitude, longitude);
                        positions.add(pos);
                    }
                    allPositionsCustomAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("users").child(uid).child("objs").child(object_id).addListenerForSingleValueEvent(userListener);
    }
}

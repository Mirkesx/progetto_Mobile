package com.marco_cavalli.lost_and_found.ui.found_objects;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.custom_adapters.FoundCustomAdapter;
import com.marco_cavalli.lost_and_found.objects.FoundItem;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;

import java.util.ArrayList;
import java.util.Map;

public class FoundFragment extends Fragment {

    private String uid;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ArrayList<FoundItem> your_founds, others_founds;
    private ListView insertion_list;
    private FoundCustomAdapter your_ca, others_ca;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_found, container, false);
        setHasOptionsMenu(true);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        uid = ((Dashboard) getActivity()).getUID();

        your_founds = new ArrayList<>();
        others_founds = new ArrayList<>();

        //Getting items reference
        insertion_list = root.findViewById(R.id.found_your_found_list);

        //Custom adapters
        int resID = R.layout.found_lost_custom_item;
        your_ca = new FoundCustomAdapter(getActivity(), resID, your_founds);
        others_ca = new FoundCustomAdapter(getActivity(), resID, others_founds);
        insertion_list.setAdapter(your_ca);


        return root;
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
                if(data != null) {
                    your_founds.clear();
                    others_founds.clear();
                    for(Map.Entry<String, ?> entry : data.entrySet()) {
                        Map<String, ?> foundData = ((Map<String,Object>) entry.getValue());
                        String id, user_id, user_name, date, icon, object_name, description;
                        id = user_id = user_name = date = icon = object_name = description = "";
                        Double latitude, longitude;
                        latitude = longitude = 0.0;
                        Boolean setFound = false;

                        if(foundData.get("id") != null)
                            id = foundData.get("id").toString();
                        if(foundData.get("user_id") != null)
                            user_id = foundData.get("user_id").toString();
                        if(foundData.get("user_name") != null)
                            user_name = foundData.get("user_name").toString();
                        if(foundData.get("date") != null)
                            date = foundData.get("date").toString();
                        if(foundData.get("icon") != null)
                            icon = foundData.get("icon").toString();
                        if(foundData.get("object_name") != null)
                            object_name = foundData.get("object_name").toString();
                        if(foundData.get("description") != null)
                            description = foundData.get("description").toString();
                        if(foundData.get("setFound") != null)
                            setFound = Boolean.parseBoolean(foundData.get("setFound").toString());

                        FoundItem fi = new FoundItem(id,user_id,user_name,date,icon,object_name,description,latitude,longitude,setFound);

                        if(uid.equals(user_id))
                            your_founds.add(fi);
                        else
                            others_founds.add(fi);
                    }
                    others_ca.notifyDataSetChanged();
                    your_ca.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("founds").addValueEventListener(userListener);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.found_lost_option_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(getString(R.string.found_new_found))) {
        }
        else if(item.getTitle().equals(getString(R.string.found_lost_your_list))) {
            insertion_list.setAdapter(your_ca);
            your_ca.notifyDataSetChanged();
        }
        else if(item.getTitle().equals(getString(R.string.found_lost_others_list))) {
            insertion_list.setAdapter(others_ca);
            others_ca.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }
}
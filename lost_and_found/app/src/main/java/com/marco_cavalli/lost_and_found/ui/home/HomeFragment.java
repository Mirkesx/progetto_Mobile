package com.marco_cavalli.lost_and_found.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;
import com.marco_cavalli.lost_and_found.objects.User;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

public class HomeFragment extends Fragment {

    ListView list;
    private Button buttonAdd;
    final static int RC_CREATE = 21;
    final static int RC_SHOW = 22;
    private HomeCustomAdapter homeCustomAdapter;
    private ArrayList<PersonalObject> objects;
    private FirebaseDatabase database;
    private String uid;
    private User user;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance();
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        user = ((Dashboard) getActivity()).getUser();
        uid = ((Dashboard) getActivity()).getUID();

        //ELEMENTS
        list = root.findViewById(R.id.home_objects_list);
        buttonAdd = root.findViewById(R.id.home_new_object);

        //CUSTOM ADAPTER
        int resID = R.layout.home_custom_list_item;
        objects = new ArrayList<>();
        homeCustomAdapter = new HomeCustomAdapter(getActivity(), resID, objects);
        list.setAdapter(homeCustomAdapter);

        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getActivity(), ShowObject.class);
            intent.putExtra("object_id", objects.get(position).getObject_id());
            intent.putExtra("name", objects.get(position).getName());
            intent.putExtra("uid", uid);
            startActivityForResult(intent, RC_SHOW);
        });

        //LISTENER BUTTON
        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateObject.class);
            startActivityForResult(intent, RC_CREATE);
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        getOBJS();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_CREATE && resultCode == Activity.RESULT_OK) {
            user = ((Dashboard) getActivity()).getUser();
            String name = data.getStringExtra("name");
            String description = data.getStringExtra("description");
            String object_id = getObject_id(name);
            PersonalObject obj = new PersonalObject(null, name, description, object_id);

            user.getObjs().put(object_id,obj);
            objects.add(obj);

            DatabaseReference myRef = database.getReference();
            myRef.child("users").child(user.getUserID()).setValue(user);
            homeCustomAdapter.notifyDataSetChanged();
        } else if(requestCode == RC_SHOW && resultCode == Activity.RESULT_OK) {
            String obj_id = data.getStringExtra("object_id");
            DatabaseReference myRef = database.getReference();
            myRef.child("users").child(uid).child("objs").child(obj_id).setValue(null);

            getOBJS();
        }
    }

    private String getObject_id(String name) {
        return name+today();
    }

    private String today() {
        String d, m, y, h, min, s;
        d = ""+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        m = ""+Calendar.getInstance().get(Calendar.MONTH);
        y = ""+Calendar.getInstance().get(Calendar.YEAR);
        h = ""+Calendar.getInstance().get(Calendar.HOUR);
        min = ""+Calendar.getInstance().get(Calendar.MINUTE);
        s = ""+Calendar.getInstance().get(Calendar.SECOND);
        if(d.length() == 1) {
            d = "0"+m;
        }
        if(m.length() == 1) {
            m = "0"+m;
        }
        return y+m+d+h+min+s;
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
                        objects.clear();
                        for(Map.Entry<String, ?> entry : ((Map<String, ?>) data.get("objs")).entrySet()) {
                            String icon = null;
                            String name = ((Map) entry.getValue()).get("name").toString();
                            String description = ((Map) entry.getValue()).get("description").toString();
                            String object_id = ((Map) entry.getValue()).get("object_id").toString();
                            PersonalObject po = new PersonalObject(icon, name, description, object_id);
                            objects.add(po);
                        }
                        homeCustomAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("users").addListenerForSingleValueEvent(userListener);
    }
}
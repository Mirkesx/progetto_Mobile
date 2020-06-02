package com.marco_cavalli.lost_and_found.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;
import com.marco_cavalli.lost_and_found.objects.User;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    ListView list;
    private Button buttonAdd;
    final static int RC_CREATE = 21;
    private User user;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        user = ((Dashboard) getActivity()).getUser();

        //ELEMENTS
        list = root.findViewById(R.id.home_objects_list);
        buttonAdd = root.findViewById(R.id.home_new_object);

        //CUSTOM ADAPTER
        int resID = R.layout.home_custom_list_item;
        ArrayList<PersonalObject> objects = new ArrayList<>();
        CustomAdapter customAdapter = new CustomAdapter(getActivity(), resID, objects);
        list.setAdapter(customAdapter);

        //LISTENER BUTTON
        buttonAdd.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateObject.class);
            startActivityForResult(intent, RC_CREATE);
        });

        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_CREATE && resultCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            String description = data.getStringExtra("description");
            PersonalObject obj = new PersonalObject(null, name, description, ""+user.updateNumberObjects());
        }
    }
}
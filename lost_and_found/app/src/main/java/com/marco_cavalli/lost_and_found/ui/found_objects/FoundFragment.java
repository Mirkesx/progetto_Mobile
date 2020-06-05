package com.marco_cavalli.lost_and_found.ui.found_objects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.custom_adapters.FoundCustomAdapter;
import com.marco_cavalli.lost_and_found.objects.FoundItem;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;

import java.util.ArrayList;

public class FoundFragment extends Fragment {

    private String uid;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ArrayList<FoundItem> your_founds, others_founds;
    private Button new_found;
    private ListView your_list, others_list;
    private FoundCustomAdapter your_ca, others_ca;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_found, container, false);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        uid = ((Dashboard) getActivity()).getUID();

        your_founds = new ArrayList<>();
        others_founds = new ArrayList<>();

        //Getting items reference
        new_found = root.findViewById(R.id.found_new_found);
        your_list = root.findViewById(R.id.found_your_found_list);
        others_list = root.findViewById(R.id.found_other_found_list);

        //Custom adapters
        int resID = R.layout.found_lost_custom_item;
        your_ca = new FoundCustomAdapter(getActivity(), resID, your_founds);
        your_list.setAdapter(your_ca);
        others_ca = new FoundCustomAdapter(getActivity(), resID, others_founds);
        others_list.setAdapter(others_ca);


        return root;
    }
}
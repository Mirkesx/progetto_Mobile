package com.marco_cavalli.lost_and_found.ui.losts_objects;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.comparators.FoundItemComparator;
import com.marco_cavalli.lost_and_found.custom_adapters.LostCustomAdapter;
import com.marco_cavalli.lost_and_found.objects.FoundItem;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;
import com.marco_cavalli.lost_and_found.objects.User;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;
import com.marco_cavalli.lost_and_found.ui.base.NewInsertionObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LostsFragment extends Fragment {

    private String uid;
    private User user;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ArrayList<FoundItem> your_lost, others_losts;
    private ListView insertion_list;
    private LostCustomAdapter your_ca, others_ca;
    private final int RC_NEW_INSERTION = 10;
    private final int RC_SHOW_INSERTION = 11;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_lost, container, false);
        setHasOptionsMenu(true);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        uid = ((Dashboard) getActivity()).getUID();
        setUser(uid);

        your_lost = new ArrayList<>();
        others_losts = new ArrayList<>();

        //Getting items reference
        insertion_list = root.findViewById(R.id.lost_your_lost_list);

        //Custom adapters
        int resID = R.layout.found_lost_custom_item;
        your_ca = new LostCustomAdapter(getActivity(), resID, your_lost);
        others_ca = new LostCustomAdapter(getActivity(), resID, others_losts);
        insertion_list.setAdapter(others_ca);


        //Listeners
        insertion_list.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(getContext(), ShowInsertionLost.class);
            intent.putExtra("uid",uid);
            intent.putExtra("insertion_id", ((FoundItem)insertion_list.getAdapter().getItem(position)).getId() );
            startActivityForResult(intent, RC_SHOW_INSERTION);
        });

        return root;
    }


    // END ON CREATE

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
                    your_lost.clear();
                    others_losts.clear();
                    for(Map.Entry<String, ?> entry : data.entrySet()) {
                        Map<String, ?> foundData = ((Map<String,Object>) entry.getValue());
                        String id, user_id, user_name, date, icon, object_name, description, address, timestamp;
                        id = user_id = user_name = date = icon = object_name = description = address = timestamp = "";
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
                        if(foundData.get("address") != null)
                            description = foundData.get("address").toString();
                        if(foundData.get("latitude") != null)
                            longitude = Double.parseDouble(foundData.get("latitude").toString());
                        if(foundData.get("longitude") != null)
                            longitude = Double.parseDouble(foundData.get("longitude").toString());
                        if(foundData.get("timestamp") != null)
                            timestamp = foundData.get("timestamp").toString();
                        if(foundData.get("setFound") != null)
                            setFound = Boolean.parseBoolean(foundData.get("setFound").toString());

                        FoundItem fi = new FoundItem(id,user_id,user_name,date,icon,object_name,description,address,latitude,longitude,timestamp,setFound);

                        if(uid.equals(user_id))
                            your_lost.add(fi);
                        else
                            others_losts.add(fi);
                    }
                    Collections.sort(your_lost, new FoundItemComparator());
                    Collections.sort(others_losts, new FoundItemComparator());

                    others_ca.notifyDataSetChanged();
                    your_ca.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("losts").addValueEventListener(userListener);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.found_lost_option_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getTitle().equals(getString(R.string.found_new_found))) {
            Intent newActivity = new Intent(getActivity(), NewInsertionObject.class);
            startActivityForResult(newActivity,RC_NEW_INSERTION);
        }
        else if(item.getTitle().equals(getString(R.string.found_lost_your_list))) {
            insertion_list.setAdapter(your_ca);
            your_ca.notifyDataSetChanged();
            ((TextView)getActivity().findViewById(R.id.lost_your_losts)).setText(getString(R.string.found_lost_your_list));
        }
        else if(item.getTitle().equals(getString(R.string.found_lost_others_list))) {
            insertion_list.setAdapter(others_ca);
            others_ca.notifyDataSetChanged();
            ((TextView)getActivity().findViewById(R.id.lost_your_losts)).setText(getString(R.string.found_lost_others_list));
        }
        return super.onOptionsItemSelected(item);
    }

    public void setUser(String uid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> data = ((Map<String,Object>) dataSnapshot.getValue());
                if(data.get(uid) != null) {
                    String userID = ((Map)data.get(uid)).get("userID").toString();
                    String displayName = ((Map) data.get(uid)).get("displayName").toString();
                    String email = ((Map) data.get(uid)).get("email").toString();
                    int gender = Integer.parseInt(((Map) data.get(uid)).get("gender").toString());
                    String city = "", birthday = "";
                    if(((Map) data.get(uid)).get("city") != null)
                        city = ((Map) data.get(uid)).get("city").toString();
                    if(((Map) data.get(uid)).get("birthday") != null)
                        birthday = ((Map) data.get(uid)).get("birthday").toString();
                    Map<String, PersonalObject> objs;
                    if( ((Map)data.get(uid)).get("objs") != null)
                        objs = ((Map<String, PersonalObject>) ((Map)data.get(uid)).get("objs"));
                    else
                        objs = new HashMap<>();
                    user = new User(userID, displayName, email, gender, city, birthday, objs);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("users").addValueEventListener(userListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_NEW_INSERTION) {
            if(resultCode == Activity.RESULT_OK) {
                String id, date, icon, object_name, description, address, timestamp;
                Double latitude, longitude;

                Bundle data_bundle = data.getExtras();
                id = createID()+uid;
                object_name = data_bundle.get("object_name").toString();
                description = data_bundle.get("description").toString();
                date = data_bundle.get("date").toString();
                address = data_bundle.get("address").toString();
                latitude = Double.parseDouble(data_bundle.get("latitude").toString());
                longitude = Double.parseDouble(data_bundle.get("longitude").toString());
                icon = saveImage("lost"+id+"_image.jpg");
                timestamp = createID();
                FoundItem fi = new FoundItem(id,uid,user.getDisplayName(),date,icon,object_name,description,address,latitude,longitude, timestamp);

                DatabaseReference myRef = database.getReference();
                myRef.child("losts").child(id).setValue(fi);
            }
        } else if(requestCode == RC_SHOW_INSERTION) {
            if(resultCode == Activity.RESULT_OK) {
                String insertion_id = data.getStringExtra("insertion_id");
                String icon = data.getStringExtra("icon");
                DatabaseReference myRef = database.getReference();
                myRef.child("losts").child(insertion_id).setValue(null);

                if(icon != null && !icon.equals("")) {
                    deleteFile(icon);
                }

                getOBJS();
            }
        }
    }

    private String createID() {
        String d, m, y, h, min, s;
        d = ""+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        m = ""+(Calendar.getInstance().get(Calendar.MONTH)+1);
        y = ""+Calendar.getInstance().get(Calendar.YEAR);
        h = ""+Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        min = ""+Calendar.getInstance().get(Calendar.MINUTE);
        s = ""+Calendar.getInstance().get(Calendar.SECOND);
        if(d.length() == 1) {
            d = "0"+d;
        }
        if(m.length() == 1) {
            m = "0"+m;
        }
        if(h.length() == 1) {
            h = "0"+h;
        }
        if(min.length() == 1) {
            min = "0"+min;
        }
        if(s.length() == 1) {
            s = "0"+s;
        }
        return y+m+d+h+min+s;
    }

    private String saveImage(String fileName) {
        try {
            File tmpFile=new File(getActivity().getFilesDir()+"/tmp", "tmp.jpg");
            if(!tmpFile.exists()){
                return "";
            }
            Log.d("Image_management",tmpFile.toString());

            File directory = new File(getActivity().getFilesDir(),"losts_images");
            if(!directory.exists()){
                directory.mkdir();
            }

            File finalFile = new File(directory,fileName);
            if(finalFile.exists()) {
                finalFile.delete();
            }

            tmpFile.renameTo(finalFile);
            Log.d("Image_management","Moved "+tmpFile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        uploadFile(fileName);

        return fileName;
    }

    private void uploadFile(String path){
        if(!path.equals("")) {
            Uri file = Uri.fromFile(new File(getActivity().getFilesDir()+"/losts_images",path));
            StorageReference storageRef = storage.getReference();
            StorageReference riversRef = storageRef.child("losts/"+path);
            UploadTask uploadTask = riversRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(exception -> {
                exception.printStackTrace();
                Log.d("UPLOAD_PHOTO","Fail");
            }).addOnSuccessListener(taskSnapshot -> Log.d("UPLOAD_PHOTO","Success"));
        }
    }

    private void deleteFile(String path) {
        File file = new File(getActivity().getFilesDir() + "/losts_images", path);
        if (file.exists()) {
            file.delete();
        }
        StorageReference storageRef = storage.getReference();
        StorageReference fileToDelete = storageRef.child("losts/" + path);

        // Delete the file
        fileToDelete.delete().addOnSuccessListener(aVoid -> {
            Log.d("Deleting_file", "Deleted " + "losts/" + path);
            getOBJS();
        }).addOnFailureListener(exception -> {
            exception.printStackTrace();
        });
    }
}
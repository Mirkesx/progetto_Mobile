package com.marco_cavalli.lost_and_found.ui.found_objects;
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
import com.marco_cavalli.lost_and_found.custom_adapters.FoundCustomAdapter;
import com.marco_cavalli.lost_and_found.objects.FoundItem;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;
import com.marco_cavalli.lost_and_found.objects.User;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FoundFragment extends Fragment {

    private String uid;
    private User user;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private ArrayList<FoundItem> your_founds, others_founds;
    private ListView insertion_list;
    private FoundCustomAdapter your_ca, others_ca;
    private final int RC_NEW_INSERTION = 10;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_found, container, false);
        setHasOptionsMenu(true);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        uid = ((Dashboard) getActivity()).getUID();
        setUser(uid);

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
                            your_founds.add(fi);
                        else
                            others_founds.add(fi);
                    }
                    Collections.sort(your_founds, new FoundItemComparator());
                    Collections.sort(others_founds, new FoundItemComparator());

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
            Intent newActivity = new Intent(getActivity(), NewFoundObject.class);
            startActivityForResult(newActivity,RC_NEW_INSERTION);
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

                id = createID()+uid;
                date = data.getStringExtra("date");
                object_name = data.getStringExtra("object_name");
                description = data.getStringExtra("description");
                address = data.getStringExtra("address");
                latitude = Double.parseDouble(data.getStringExtra("latitude"));
                longitude = Double.parseDouble(data.getStringExtra("longitude"));
                icon = saveImage("found"+id+"_image.jpg");
                timestamp = createID();
                FoundItem fi = new FoundItem(id,uid,user.getDisplayName(),date,icon,object_name,description,address,latitude,longitude, timestamp);

                DatabaseReference myRef = database.getReference();
                myRef.child("founds").child(id).setValue(fi);

                if(icon.length() > 0) {
                    uploadFile(icon);
                }
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

            File directory = new File(getActivity().getFilesDir(),"founds_images");
            if(!directory.exists()){
                directory.mkdir();
            }

            File finalFile = new File(directory,fileName);
            if(finalFile.exists()) {
                finalFile.delete();
            }

            boolean success = tmpFile.renameTo(finalFile);
            Log.d("Image_management","Moved "+tmpFile.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


        return fileName;
    }

    private void uploadFile(String path){
        if(!path.equals("")) {
            Uri file = Uri.fromFile(new File(getActivity().getFilesDir()+"/founds",path));
            StorageReference storageRef = storage.getReference();
            StorageReference riversRef = storageRef.child("founds/"+path);
            UploadTask uploadTask = riversRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(exception -> {
                exception.printStackTrace();
                Log.d("UPLOAD_PHOTO","Fail");
            }).addOnSuccessListener(taskSnapshot -> Log.d("UPLOAD_PHOTO","Success"));
        }
    }
}
package com.marco_cavalli.lost_and_found.ui.home;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.marco_cavalli.lost_and_found.objects.PersonalObject;
import com.marco_cavalli.lost_and_found.objects.User;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;

import java.io.File;
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
    private FirebaseStorage storage;
    private String uid;
    private User user;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
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

        if(requestCode == RC_CREATE) {
            if(resultCode == Activity.RESULT_OK) {
                user = ((Dashboard) getActivity()).getUser();
                String name = data.getStringExtra("name");
                String description = data.getStringExtra("description");
                String object_id = getObject_id(name);
                String icon = saveImage(object_id+"_image.jpg");
                PersonalObject obj = new PersonalObject(icon, name, description, object_id);

                user.getObjs().put(object_id,obj);
                objects.add(obj);

                DatabaseReference myRef = database.getReference();
                myRef.child("users").child(user.getUserID()).setValue(user);


                if(!icon.equals(""))
                    uploadFile(icon);

                homeCustomAdapter.notifyDataSetChanged();
            } else {
                File tmpFile=new File(getActivity().getFilesDir()+"/tmp", "tmp.jpg");
                if(tmpFile.exists()){
                    tmpFile.delete();
                }
            }
        } else if(requestCode == RC_SHOW && resultCode == Activity.RESULT_OK) {
            String obj_id = data.getStringExtra("object_id");
            String icon = data.getStringExtra("icon");
            DatabaseReference myRef = database.getReference();
            myRef.child("users").child(uid).child("objs").child(obj_id).setValue(null);

            if(icon != null && !icon.equals("")) {
                deleteFile(icon);
            }
            if(objects.size() == 1) {
                objects.clear();
                homeCustomAdapter.notifyDataSetChanged();
            }
            getOBJS();
        }
    }

    private void deleteFile(String path) {
        File file = new File(getActivity().getFilesDir()+"/objects_images",path);
        if(file.exists()) {
            file.delete();
        }
        StorageReference storageRef = storage.getReference();
        StorageReference fileToDelete = storageRef.child("users/"+uid+"/objects_images/"+path);

        // Delete the file
        fileToDelete.delete().addOnSuccessListener(aVoid -> {
            Log.d("Deleting_file","Deleted "+ "users/"+uid+"/objects_images/"+path);
            getOBJS();
        }).addOnFailureListener(exception -> {
            exception.printStackTrace();
        });
    }

    private void uploadFile(String path){
            Uri file = Uri.fromFile(new File(getActivity().getFilesDir()+"/objects_images",path));
            StorageReference storageRef = storage.getReference();
            StorageReference riversRef = storageRef.child("users/"+uid+"/objects_images/"+path);
            UploadTask uploadTask = riversRef.putFile(file);

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(exception -> {
                exception.printStackTrace();
                Log.d("UPLOAD_PHOTO","Fail");
            }).addOnSuccessListener(taskSnapshot -> Log.d("UPLOAD_PHOTO","Success"));
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
                            String name = ((Map) entry.getValue()).get("name").toString();
                            String description = ((Map) entry.getValue()).get("description").toString();
                            String object_id = ((Map) entry.getValue()).get("object_id").toString();
                            String icon = "";
                            if(((Map) entry.getValue()).get("icon") != null)
                                icon = ((Map) entry.getValue()).get("object_id").toString();
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

    private String saveImage(String fileName) {
        ContextWrapper cw = new ContextWrapper(getActivity());
        File tmpFile=new File(cw.getFilesDir()+"/tmp", "tmp.jpg");
        if(!tmpFile.exists()){
            return "";
        }
        Log.d("Image_management",tmpFile.toString());

        File directory = new File(cw.getFilesDir(),"objects_images");
        if(!directory.exists()){
            directory.mkdir();
        }

        File finalFile = new File(directory,fileName);
        if(finalFile.exists()) {
            finalFile.delete();
        }

        tmpFile.renameTo(finalFile);
        Log.d("Image_management",tmpFile.toString());


        return fileName;
    }
}
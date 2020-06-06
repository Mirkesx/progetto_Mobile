package com.marco_cavalli.lost_and_found.ui.profile;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;
import com.marco_cavalli.lost_and_found.ui.login.LoginScreen;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private String signInMethod;
    private User user;

    private TextView textViewName, textViewEmail, textViewGender, textViewCity, textViewBirthday;
    private Button edit, logout;
    private ImageView profile;
    private int id_gender;


    private String uid;
    private FirebaseStorage storage;
    private static int RC_EDIT_PROFILE = 10;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        signInMethod = ((Dashboard)getActivity()).getSignInMethod();
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        uid = ((Dashboard) getActivity()).getUID();

        getUser();

        storage = FirebaseStorage.getInstance();

        //TEXT VIEW
        textViewName = root.findViewById(R.id.profile_display_name);
        textViewEmail = root.findViewById(R.id.profile_email);
        textViewGender = root.findViewById(R.id.profile_gender);
        textViewCity = root.findViewById(R.id.profile_city);
        textViewBirthday = root.findViewById(R.id.profile_birthday);

        //BUTTON
        edit = root.findViewById(R.id.profile_edit);
        logout = root.findViewById(R.id.profile_logout);

        //IMAGE VIEW
        profile = root.findViewById(R.id.profile_image);

        //BUTTONS LISTENERS

        edit.setOnClickListener(v -> {
           Intent newActivity = new Intent(getActivity(), ProfileEdit.class);
           newActivity.putExtra("uid",uid);
            startActivityForResult(newActivity,RC_EDIT_PROFILE);
        });

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginScreen.class);
            //intent.putExtra();
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();
            GoogleSignIn.getClient(getActivity(), gso).signOut();

            LoginManager.getInstance().logOut();

            startActivity(intent);
            getActivity().finish();
        });

        setProfilePic();

        return root;
    }

    private void getUser() {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            ValueEventListener userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Map<String,Object> data = ((Map<String,Object>) dataSnapshot.getValue());
                    if(data.get(uid) != null) {
                        String icon = ((Map) data.get(uid)).get("icon").toString();
                        String displayName = ((Map) data.get(uid)).get("displayName").toString();
                        String email = ((Map) data.get(uid)).get("email").toString();
                        int id_gender = Integer.parseInt(((Map) data.get(uid)).get("gender").toString());
                        String city = "", birthday = "";
                        if (((Map) data.get(uid)).get("city") != null)
                            city = ((Map) data.get(uid)).get("city").toString();
                        if (((Map) data.get(uid)).get("birthday") != null)
                            birthday = ((Map) data.get(uid)).get("birthday").toString();
                        Map<String, PersonalObject> objs;
                        if (((Map) data.get(uid)).get("objs") != null)
                            objs = ((Map<String, PersonalObject>) ((Map) data.get(uid)).get("objs"));
                        else
                            objs = new HashMap<>();
                        user = new User(uid, displayName, email, id_gender, city, birthday, objs);
                        setValues(user);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            myRef.child("users").addListenerForSingleValueEvent(userListener);
    }

    private void setValues(User user) {
        //SHOWING TEXTVIEWS
        id_gender = user.getGender();
        textViewName.setText(user.getDisplayName());
        textViewEmail.setText(user.getEmail());
        textViewGender.setText(getIdGender(id_gender));
        textViewCity.setText(user.getCity());
        textViewBirthday.setText(user.getBirthday());
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == RC_EDIT_PROFILE) {
            if(resultCode == Activity.RESULT_OK) {
                id_gender = Integer.parseInt(data.getStringExtra("id_gender"));
                textViewGender.setText(getIdGender(id_gender));
                textViewCity.setText(data.getStringExtra("city"));
                textViewBirthday.setText(data.getStringExtra("birthday"));

                if(data.getStringExtra("icon") != null)
                    deleteFile("user_image.jpg");

                String icon = saveImage("user_image.jpg");

                user.setGender(id_gender);
                user.setCity(textViewCity.getText().toString());
                user.setBirthday(textViewBirthday.getText().toString());
                user.setIcon(icon);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();
                myRef.child("users").child(user.getUserID()).setValue(user);
                if(!icon.equals("")) {
                    uploadFile(icon);
                }
                setProfilePic();
            }
        }
    }

    private int getIdGender(int gender) {
        if(gender == 1)
            return R.string.gender_male;
        if(gender == 2)
            return R.string.gender_female;
        return R.string.gender_not_specified;
    }

    private void setProfilePic() { //check if the file is cached, then it downloads it if it doesn't
        File local = new File(getActivity().getFilesDir()+"/profile", "user_image.jpg");
        if(local.exists()) {
            Bitmap b = null;
            try {
                b = BitmapFactory.decodeStream(new FileInputStream(local));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            profile.setImageBitmap(b);
        } else {
            StorageReference storageRef = storage.getReference();
            StorageReference user_image = storageRef.child("users/"+uid+"/user_image.jpg");
            File newFile = new File(getActivity().getFilesDir()+"/profile","user_image.jpg");

            user_image.getFile(newFile).addOnSuccessListener(taskSnapshot -> {
                Bitmap b = null;
                try {
                    b = BitmapFactory.decodeStream(new FileInputStream(newFile));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                profile.setImageBitmap(b);
            }).addOnFailureListener(exception -> {
                exception.printStackTrace();
            });
        }
    }

    private String saveImage(String fileName) {
        ContextWrapper cw = new ContextWrapper(getActivity());
        File tmpFile=new File(cw.getFilesDir()+"/tmp", "tmp.jpg");
        if(!tmpFile.exists()){
            return "";
        }
        Log.d("Image_management",tmpFile.toString());

        File directory = new File(cw.getFilesDir(),"profile");
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

    private void deleteFile(String path) {
        File file = new File(getActivity().getFilesDir()+"/profile",path);
        if(file.exists()) {
            file.delete();
        }
        StorageReference storageRef = storage.getReference();
        StorageReference fileToDelete = storageRef.child("users/"+uid+"/"+path);

        // Delete the file
        fileToDelete.delete().addOnSuccessListener(aVoid -> {
            Log.d("Deleting_file","Deleted "+ "users/"+uid+"/objects_images/"+path);
        }).addOnFailureListener(exception -> {
            exception.printStackTrace();
        });
    }

    private void uploadFile(String path){
        Uri file = Uri.fromFile(new File(getActivity().getFilesDir()+"/profile",path));
        StorageReference storageRef = storage.getReference();
        StorageReference riversRef = storageRef.child("users/"+uid+"/"+path);
        UploadTask uploadTask = riversRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(exception -> {
            exception.printStackTrace();
            Log.d("UPLOAD_PHOTO","Fail");
        }).addOnSuccessListener(taskSnapshot -> Log.d("UPLOAD_PHOTO","Success"));
    }
}
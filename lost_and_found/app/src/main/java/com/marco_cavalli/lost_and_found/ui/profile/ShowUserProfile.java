package com.marco_cavalli.lost_and_found.ui.profile;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;
import com.marco_cavalli.lost_and_found.objects.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class ShowUserProfile extends AppCompatActivity {

    private String uid, user_id;
    private User user;

    private TextView textViewName, textViewEmail, textViewPhone, textViewGender, textViewCity, textViewBirthday;
    private ImageButton email, call, message;
    private ImageView profile;
    private int id_gender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_profile);

        Intent data_intent = getIntent();
        Bundle data_bundle = data_intent.getExtras();

        uid = data_bundle.get("uid").toString();
        user_id = data_bundle.get("user_id").toString();

        //Elements
        textViewName = findViewById(R.id.profile_display_name);
        textViewEmail = findViewById(R.id.profile_email);
        textViewPhone = findViewById(R.id.profile_phone);
        textViewGender = findViewById(R.id.profile_gender);
        textViewCity = findViewById(R.id.profile_city);
        textViewBirthday = findViewById(R.id.profile_birthday);
        email = findViewById(R.id.profile_send_email);
        call = findViewById(R.id.profile_call);
        message = findViewById(R.id.profile_send_message);
        profile = findViewById(R.id.profile_image);

        getUser();
    }

    private void getUser() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> data = ((Map<String, Object>) dataSnapshot.getValue());
                if (data.get(user_id) != null) {
                    String icon = ((Map) data.get(user_id)).get("icon").toString();
                    String displayName = ((Map) data.get(user_id)).get("displayName").toString();
                    String email = ((Map) data.get(user_id)).get("email").toString();
                    int id_gender = Integer.parseInt(((Map) data.get(user_id)).get("gender").toString());
                    String phone = "", city = "", birthday = "";
                    if (((Map) data.get(user_id)).get("phone") != null)
                        phone += ((Map) data.get(user_id)).get("phone").toString();
                    if (((Map) data.get(user_id)).get("city") != null)
                        city += ((Map) data.get(user_id)).get("city").toString();
                    if (((Map) data.get(user_id)).get("birthday") != null)
                        birthday += ((Map) data.get(user_id)).get("birthday").toString();
                    Map<String, PersonalObject> objs;
                    if (((Map) data.get(user_id)).get("objs") != null)
                        objs = ((Map<String, PersonalObject>) ((Map) data.get(user_id)).get("objs"));
                    else
                        objs = new HashMap<>();
                    user = new User(uid, displayName, email, phone, id_gender, city, birthday, objs, icon);
                    Log.d("User_show_management", user.toString());
                    setValues();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("users").addListenerForSingleValueEvent(userListener);
    }

    private void setValues() {
        //SHOWING TEXTVIEWS
        id_gender = user.getGender();
        textViewName.setText(user.getDisplayName());
        textViewEmail.setText(user.getEmail());
        textViewPhone.setText(user.getPhone());
        textViewGender.setText(getIdGender(id_gender));
        textViewCity.setText(user.getCity());
        textViewBirthday.setText(user.getBirthday());

        if (user.getEmail().length() > 0) {
            email.setVisibility(View.VISIBLE);
            email.setOnClickListener(v -> {
                String mailto = "mailto:"+user.getEmail() +
                        "?cc="+
                        "&subject=" + Uri.encode(getString(R.string.email_found_subject)) +
                        "&body=" + Uri.encode(getString(R.string.email_found_message));

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse(mailto));

                try {
                    startActivity(emailIntent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.mailer_not_found), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (user.getPhone().length() > 0) {
            call.setVisibility(View.VISIBLE);
            call.setOnClickListener(v -> {
                callPhoneNumber();
            });

            //message.setVisibility(View.VISIBLE);
            message.setOnClickListener(v -> {
                Uri uri = Uri.parse("smsto:"+user.getPhone());
                Intent messIntent = new Intent(Intent.ACTION_SENDTO, uri);
                messIntent.putExtra("sms_body", getString(R.string.email_found_message));
                startActivity(messIntent);
            });
        }

        if(user.getIcon().length() > 0) {
            setImage();
        }
    }

    private void setImage() {
        File local = new File(getFilesDir()+"/profile_images", user_id+"_image.jpg");
        try {
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(local));
            profile.setImageBitmap(b);
        } catch (FileNotFoundException ex) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference user_image = storageRef.child("users/"+user_id+"/user_image.jpg");
            File newFile = new File(getFilesDir()+"/profile_images",user_id+"_image.jpg");

            user_image.getFile(newFile).addOnSuccessListener(taskSnapshot -> {
                try {
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(newFile));
                    profile.setImageBitmap(b);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }).addOnFailureListener(exception -> {
                exception.printStackTrace();
            });
        }
    }

    private int getIdGender(int gender) {
        if(gender == 1)
            return R.string.gender_male;
        if(gender == 2)
            return R.string.gender_female;
        return R.string.gender_not_specified;
    }

    // FOR CALLS PERMISSIONS

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if(requestCode == 101)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                callPhoneNumber();
            }
        }
    }

    public void callPhoneNumber()
    {
        try
        {
            if(Build.VERSION.SDK_INT > 22)
            {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ShowUserProfile.this, new String[]{Manifest.permission.CALL_PHONE}, 101);
                    return;
                }

                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + user.getPhone()));
                startActivity(callIntent);

            }
            else {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + user.getPhone()));
                startActivity(callIntent);
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            Log.d("user_call_management", "Don't have permissions");
        }
    }
}

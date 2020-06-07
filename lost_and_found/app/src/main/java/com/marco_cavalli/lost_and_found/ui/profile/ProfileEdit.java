package com.marco_cavalli.lost_and_found.ui.profile;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marco_cavalli.lost_and_found.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;

public class ProfileEdit extends AppCompatActivity {

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_LOAD_IMG = 2;
    private String currentPhotoPath;
    private Uri photoURI;
    private EditText editViewPhone, editViewCity;
    private TextView textViewGender, textViewBirthday;
    private Button update;
    private ImageView profile_edit, gallery, camera;
    private int id_gender;
    private String uid, city, birthday;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_edit);

        Intent data_intent = getIntent();
        Bundle data = data_intent.getExtras();


        uid = data.getString("uid");
        getUser();

        //EDIT TEXT
        editViewPhone = findViewById(R.id.profile_phone_edit);
        editViewCity = findViewById(R.id.profile_city_edit);

        //TEXT VIEW
        textViewGender = findViewById(R.id.profile_gender_edit);
        textViewBirthday = findViewById(R.id.profile_birthday_edit);

        //BUTTON
        update = findViewById(R.id.profile_update);


        //IMAGE VIEW
        profile_edit = findViewById(R.id.profile_image_edit);
        gallery = findViewById(R.id.profile_gallery);
        camera = findViewById(R.id.profile_camera);

        //SETTING VALUES
        textViewGender.setText(getString(getIdGender(id_gender)));
        editViewCity.setText(""+city);
        textViewBirthday.setText(birthday);

        //TEXT LISTENERS
        registerForContextMenu(textViewGender);


        textViewBirthday.setOnClickListener(v -> {
            String FRAG_TAG_DATE_PICKER = getString(R.string.CalendarTag);
            String birthday = textViewBirthday.getText().toString();
            int y, m, d;
            if(birthday.length() > 0) {
                String[] tmp = birthday.split("/");
                d = Integer.parseInt(tmp[0]);
                m = Integer.parseInt(tmp[1])-1;
                y = Integer.parseInt(tmp[2]);
            }
            else {
                d = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                m = Calendar.getInstance().get(Calendar.MONTH);
                y = Calendar.getInstance().get(Calendar.YEAR);
            }

            CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                    .setOnDateSetListener((dialog, year, monthOfYear, dayOfMonth) -> setBirthday(dayOfMonth,monthOfYear,year))
                    .setFirstDayOfWeek(Calendar.SUNDAY)
                    .setPreselectedDate(y, m, d)
                    .setDoneText(getString(R.string.Confirm))
                    .setCancelText(getString(R.string.Cancel))
                    .setThemeLight();
            cdp.show(getSupportFragmentManager(), FRAG_TAG_DATE_PICKER);
        });

        //BUTTON LISTENERS
        camera.setOnClickListener(v -> {
            if(checkDeviceCompatibility()) {
                dispatchTakePictureIntent();
            }
        });

        gallery.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, REQUEST_LOAD_IMG);
        });

        update.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.putExtra("id_gender",""+id_gender);
            intent.putExtra("phone", ""+editViewPhone.getText());
            intent.putExtra("city", ""+editViewCity.getText());
            intent.putExtra("birthday", textViewBirthday.getText());
            File f = new File(getFilesDir()+"/tmp","tmp.jpg");
            if(f.exists())
                intent.putExtra("icon","exists");
            setResult(Activity.RESULT_OK,intent);
            finish();
        });

        checkImage();
    }

    private void setBirthday(int dayOfMonth, int monthOfYear, int year) {
        String d = ""+dayOfMonth;
        if(d.length() == 1) {
            d = "0"+d;
        }
        String m = ""+(monthOfYear+1);
        if(m.length() == 1) {
            m = "0"+m;
        }
        textViewBirthday.setText(d+"/"+m+"/"+year);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_gender_edit_menu, menu);
        menu.setHeaderTitle(getString(R.string.pick_gender));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        String result = item.getTitle().toString();
        if(result.equals(getString(R.string.gender_not_specified)))
            id_gender = 0;
        else if(result.equals(getString(R.string.gender_male)))
            id_gender = 1;
        else if(result.equals(getString(R.string.gender_female)))
            id_gender = 2;
        textViewGender.setText(result);
        return true;
    }

    private int getIdGender(int gender) {
        if(gender == 1)
            return R.string.gender_male;
        if(gender == 2)
            return R.string.gender_female;
        return R.string.gender_not_specified;
    }

    public boolean checkDeviceCompatibility() {

        PackageManager pm = getPackageManager();

        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                return true;
            } else {
                // use front camera
                Toast.makeText(
                        this,
                        getString(R.string.camera_no_back_camera),
                        Toast.LENGTH_SHORT).show();
                return true;
            }
        } else {
            Toast.makeText(
                    this,
                    getString(R.string.camera_no_camera),
                    Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file
        File storageDir = new File(getFilesDir(),"tmp");
        if(!storageDir.exists()){
            storageDir.mkdir();
        }
        File image=new File(storageDir,"tmp.jpg");

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            checkImage();
        }
        if (requestCode == REQUEST_LOAD_IMG) {
            if(resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    profile_edit.setImageBitmap(selectedImage);
                    saveToInternalStorage(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.gallery_error), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.gallery_no_selection),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkImage() {
        try {
            File tmp=new File(this.getFilesDir()+"/tmp", "tmp.jpg");
            if(tmp.exists()) {
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(tmp));
                profile_edit.setImageBitmap(b);
            } else{
                getProfilePic();
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private void getProfilePic() {
        try {
            File local=new File(this.getFilesDir()+"/profile", "user_image.jpg");
            if(local.exists()) {
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(local));
                profile_edit.setImageBitmap(b);
            } else{
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference user_image = storageRef.child("users/"+uid+"/user_image.jpg");
                File newFile = new File(getFilesDir()+"/profile","user_image.jpg");

                user_image.getFile(newFile).addOnSuccessListener(taskSnapshot -> {
                    Bitmap b = null;
                    try {
                        b = BitmapFactory.decodeStream(new FileInputStream(newFile));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    profile_edit.setImageBitmap(b);
                }).addOnFailureListener(exception -> {
                    exception.printStackTrace();
                });
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    private void saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(this);
        // path to /data/data/yourapp/app_data/tmp
        File directory = new File(cw.getFilesDir(),"tmp");
        if(!directory.exists()){
            directory.mkdir();
        }
        // Create imageDir
        File mypath=new File(directory,"tmp.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getUser() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> data = ((Map<String, Object>) dataSnapshot.getValue());
                if (data.get(uid) != null) {
                    id_gender = Integer.parseInt(((Map) data.get(uid)).get("gender").toString());
                    city = "";
                    birthday = "";
                    if (((Map) data.get(uid)).get("city") != null)
                        city = ((Map) data.get(uid)).get("city").toString();
                    if (((Map) data.get(uid)).get("birthday") != null)
                        birthday = ((Map) data.get(uid)).get("birthday").toString();

                    textViewGender.setText(getIdGender(id_gender));
                    editViewCity.setText(city);
                    textViewBirthday.setText(birthday);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("users").addListenerForSingleValueEvent(userListener);
    }
}

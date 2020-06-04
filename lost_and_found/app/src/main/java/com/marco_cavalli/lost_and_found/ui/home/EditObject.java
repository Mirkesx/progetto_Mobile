package com.marco_cavalli.lost_and_found.ui.home;

import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;
import com.marco_cavalli.lost_and_found.objects.Position;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditObject extends AppCompatActivity {

    private EditText name;
    private EditText description;
    private ImageView camera, gallery, image;
    private Button update;
    private String uid, object_id;
    private PersonalObject obj;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    //static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    static final int REQUEST_LOAD_IMG = 2;
    private String currentPhotoPath;
    private Uri photoURI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_create_object);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        Intent intent_data = getIntent();
        Bundle bundle_data = intent_data.getExtras();
        uid = bundle_data.getString("uid");
        object_id = bundle_data.getString("object_id");

        //ELEMENTS
        name = findViewById(R.id.home_create_name_edit);
        description = findViewById(R.id.home_create_description_edit);
        image = findViewById(R.id.home_create_image);
        camera = findViewById(R.id.home_create_camera);
        gallery = findViewById(R.id.home_create_gallery);
        update = findViewById(R.id.home_create_button);
        setTitle(getString(R.string.home_edit_object));
        update.setText(R.string.home_update_object);

        //LISTENERS
        update.setOnClickListener(v -> {
            if(name.getText() == null || name.getText().toString().length() > 0) {
                Intent data = new Intent();
                data.putExtra("name",name.getText().toString());
                if(description.getText() != null)
                    data.putExtra("description",description.getText().toString());
                else
                    data.putExtra("description","");
                data.putExtra("icon", obj.getIcon());
                setResult(Activity.RESULT_OK, data);
                finish();
            }
            else {
                Toast.makeText(this, getString(R.string.home_create_missing_name), Toast.LENGTH_SHORT).show();
            }
        });

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        getOBJS();
    }

    public boolean checkDeviceCompatibility() {

        PackageManager pm = this.getPackageManager();

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            checkImage();
        }
        if (requestCode == REQUEST_LOAD_IMG) {
            if(resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    image.setImageBitmap(selectedImage);
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

    private void saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
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

    private void checkImage() {
        try {
            File f=new File(this.getFilesDir()+"/tmp", "tmp.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            image.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
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
                        data = ((Map<String, ?>) data.get("objs"));
                        if(data != null && data.get(object_id) != null) {
                            data = ((Map<String, ?>) data.get(object_id));
                            String icon = null;
                            if(data.get("icon") != null)
                                icon = data.get("icon").toString();
                            String name = data.get("name").toString();
                            String description = data.get("description").toString();
                            String object_id = data.get("object_id").toString();
                            Map<String, Position> positions = new HashMap<>();
                            if(data.get("positions") != null) {
                                for(Map.Entry<String, ?> entry : ((Map<String, ?>) data.get("positions")).entrySet()) {
                                    String pos_id = ((Map) entry.getValue()).get("pos_id").toString();
                                    String descr = ((Map) entry.getValue()).get("description").toString();
                                    String date = null;
                                    if(((Map) entry.getValue()).get("date") != null) {
                                        date = ((Map) entry.getValue()).get("date").toString();
                                    }
                                    Double latitude = 0.0;
                                    if(((Map) entry.getValue()).get("latitude") != null) {
                                        latitude = Double.parseDouble(((Map) entry.getValue()).get("latitude").toString());
                                    }
                                    Double longitude = 0.0;
                                    if(((Map) entry.getValue()).get("longitude") != null) {
                                        longitude = Double.parseDouble(((Map) entry.getValue()).get("longitude").toString());
                                    }
                                    Position pos = new Position(pos_id, date, descr, latitude, longitude);
                                    positions.put(pos_id,pos);
                                }
                            }
                            obj = new PersonalObject(icon, name, description, positions, object_id);
                        }
                    }
                }
                setValues();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("users").addListenerForSingleValueEvent(userListener);
    }

    private void setValues() {
        File tmp = new File(getFilesDir()+"/tmp","tmp.jpg");
        if(!obj.getIcon().equals("") && !tmp.exists())
            setImageView();
        name.setText(obj.getName());
        description.setText(obj.getDescription());
    }

    private void setImageView() {
        File file = new File(getFilesDir()+"/objects_images",obj.getIcon());
        if(file.exists()) {
            image.setImageURI(Uri.fromFile(file));
        }
        else {
            StorageReference storageRef = storage.getReference();
            StorageReference islandRef = storageRef.child("users/"+uid+"/objects_images/"+obj.getIcon());
            File newFile = new File(getFilesDir()+"/objects_images",obj.getIcon());

            islandRef.getFile(newFile).addOnSuccessListener(taskSnapshot -> {
                image.setImageURI(Uri.fromFile(newFile));
            }).addOnFailureListener(exception -> {
                exception.printStackTrace();
            });
        }
    }
}

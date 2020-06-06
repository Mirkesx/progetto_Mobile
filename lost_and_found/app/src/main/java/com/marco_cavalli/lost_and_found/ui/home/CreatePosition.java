package com.marco_cavalli.lost_and_found.ui.home;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.services.LocationTrack;
import com.shivtechs.maplocationpicker.LocationPickerActivity;
import com.shivtechs.maplocationpicker.MapUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CreatePosition extends AppCompatActivity {

    private String object_id;
    private String uid;
    private EditText editDesc;
    private TextView textAddress, textLat, textLon, updateButton;
    private ImageView getGPS;

    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;
    private Uri photoURI;
    private String currentPhotoPath;
    private final int REQUEST_LOAD_IMG = 1;
    private final int REQUEST_TAKE_PHOTO = 2;
    private final int RC_MAP_BUTTON = 3;
    private ImageView image, gallery, camera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_update_object_position);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions((String[])permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        //Retrieving data
        Intent inte = getIntent();
        Bundle data = inte.getExtras();
        object_id = data.getString("object_id");
        uid = data.getString("uid");

        //Initialize elements
        editDesc = findViewById(R.id.new_position_description);
        textAddress = findViewById(R.id.new_position_address);
        textLat = findViewById(R.id.new_poition_lat);
        textLon = findViewById(R.id.new_position_lon);
        getGPS = findViewById(R.id.new_position_latlon_button);
        updateButton = findViewById(R.id.new_position_submit);
        image = findViewById(R.id.new_position_image);
        camera = findViewById(R.id.new_position_camera);
        gallery = findViewById(R.id.new_position_gallery);

        updateButton.setOnClickListener(v -> {
            if (editDesc.getText().toString().length() == 0) {
                Toast.makeText(this, getString(R.string.home_create_position_missing_description), Toast.LENGTH_LONG).show();
                return;
            }

            Intent data_intent = new Intent();
            data_intent.putExtra("description", editDesc.getText().toString());

            data_intent.putExtra("date", today());
            if (textLat.getText().toString().length() > 0)
                data_intent.putExtra("latitude", textLat.getText().toString());
            else
                data_intent.putExtra("latitude", "0");
            if (textLon.getText().toString().length() > 0)
                data_intent.putExtra("longitude", textLon.getText().toString());
            else
                data_intent.putExtra("longitude", "0");
            if (textAddress.getText().toString().length() > 0)
                data_intent.putExtra("address", textAddress.getText().toString());
            else
                data_intent.putExtra("address", "");
            setResult(Activity.RESULT_OK, data_intent);
            finish();
        });


        getGPS.setOnClickListener(view -> {
            Intent i = new Intent(this, LocationPickerActivity.class);
            i.putExtra(MapUtility.LATITUDE, textLat.getText());
            i.putExtra(MapUtility.LONGITUDE, textLon.getText());
            startActivityForResult(i, RC_MAP_BUTTON);
        });

        if ( getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED  || getApplicationContext().checkSelfPermission(ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            getCoordinates();
        }

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

    public void getCoordinates() {
        permissionsToRequest = findUnAskedPermissions(permissions);
        locationTrack = new LocationTrack(CreatePosition.this);


        if (locationTrack.canGetLocation()) {


            Double longitude = locationTrack.getLongitude();
            Double latitude = locationTrack.getLatitude();

            if(latitude > 90 && longitude > 180) {
                Toast.makeText(getApplicationContext(), getString(R.string.gps_not_available), Toast.LENGTH_LONG).show();
                finish();
            }

            textLat.setText(""+ latitude);
            textLon.setText(""+ longitude);

            try {
                Geocoder geocoder;
                geocoder = new Geocoder(this);
                List<Address> location = geocoder.getFromLocation(latitude, longitude, 1);
                textAddress.setText(getAddress(location.get(0)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            locationTrack.showSettingsAlert();
        }
    }

    private String getAddress(Address locality) {
        String full_address = "";

        String city =  locality.getLocality();
        String subLocality = locality.getSubLocality();
        String throughfare = locality.getThoroughfare();
        String subThroughfare = locality.getSubThoroughfare();
        String state =  locality.getAdminArea();
        String country =  locality.getCountryName();
        String postalCode =  locality.getPostalCode();

        if(throughfare != null)
            full_address += throughfare +", ";
        if(subThroughfare != null)
            full_address += subThroughfare +", ";
        if(postalCode != null)
            full_address += postalCode +", ";
        if(city != null)
            full_address += city +", ";
        if(subLocality != null)
            full_address += subLocality +", ";
        if(state != null)
            full_address += state +", ";
        if(country != null)
            full_address += country +", ";

        full_address = full_address.substring(0,full_address.length()-2);

        return full_address;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private ArrayList findUnAskedPermissions(ArrayList wanted) {
        ArrayList result = new ArrayList();

        for (String perm :((ArrayList<String>) wanted)) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : (ArrayList<String>) permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.gps_cant_be_used), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    getCoordinates();
                }

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(locationTrack != null)
            locationTrack.stopListener();
    }


    private String today() {
        String d, m, y, h, min, s;
        d = ""+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        m = ""+Calendar.getInstance().get(Calendar.MONTH);
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
        return d+"/"+m+"/"+y+" - "+h+":"+min+":"+s;
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
        if (requestCode == RC_MAP_BUTTON) {
            Log.d("GOOGLE_ADDRESS", data.getStringExtra("address"));
            try {
                if (data != null && data.getStringExtra("address") != null) {

                    Geocoder geocoder;
                    geocoder = new Geocoder(this);
                    List<Address> location = geocoder.getFromLocationName(data.getStringExtra("address"),1);


                    String address = getAddress(location.get(0));
                    textLat.setText(""+location.get(0).getLatitude());
                    textLon.setText(""+location.get(0).getLongitude());
                    textAddress.setText(address);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File f=new File(this.getFilesDir()+"/tmp", "tmp.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            image.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}

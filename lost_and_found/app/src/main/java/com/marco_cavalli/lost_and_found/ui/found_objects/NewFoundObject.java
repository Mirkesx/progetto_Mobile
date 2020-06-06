package com.marco_cavalli.lost_and_found.ui.found_objects;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
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

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class NewFoundObject extends AppCompatActivity {

    private EditText textObjectName, textDescription;
    private TextView textDate, textAddress, textLatitude, textLongitude;
    private ImageView viewCamera, viewGallery, viewImage;
    private ImageButton buttonMaps;
    private Button buttonCreate;

    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private final static int RC_MAP_BUTTON = 10;
    LocationTrack locationTrack;

    private final static int REQUEST_LOAD_IMG = 21;
    private final static int REQUEST_TAKE_PHOTO = 22;
    private String currentPhotoPath;
    private Uri photoURI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.found_lost_new_insertion);

        //initialize GPS
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions((String[])permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }

        //Initialize elements
        textObjectName = findViewById(R.id.found_lost_new_object_name_edit);
        textDescription = findViewById(R.id.found_lost_new_description_edit);
        textDate = findViewById(R.id.found_lost_new_date_value);
        textAddress = findViewById(R.id.found_lost_address);
        textLatitude = findViewById(R.id.found_lost_new_latitude);
        textLongitude = findViewById(R.id.found_lost_new_longitude);
        viewCamera = findViewById(R.id.found_lost_new_camera);
        viewGallery = findViewById(R.id.found_lost_new_gallery);
        viewImage = findViewById(R.id.found_lost_new_image);
        buttonMaps = findViewById(R.id.found_lost_maps);
        buttonCreate = findViewById(R.id.found_lost_new_create);
        MapUtility.apiKey = getResources().getString(R.string.google_api_key);

        textDate.setText(today());

        if ( getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED  || getApplicationContext().checkSelfPermission(ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            getCoordinates();
        }

        buttonMaps.setOnClickListener(v -> openMaps());

        viewCamera.setOnClickListener(v -> {
            if(checkDeviceCompatibility()) {
                dispatchTakePictureIntent();
            }
        });

        viewGallery.setOnClickListener(v -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, REQUEST_LOAD_IMG);
        });

        textDate.setOnClickListener(v -> setDate());

        buttonCreate.setOnClickListener(v -> {
            if(textObjectName.length() == 0 || textDescription.length() == 0) {
                Toast.makeText(this, getString(R.string.new_insertion_missing_data), Toast.LENGTH_LONG).show();
                return;
            }

            Intent data = new Intent();
            data.putExtra("object_name", textObjectName.getText());
            data.putExtra("description", textDescription.getText());
            data.putExtra("date", textDate.getText());
            data.putExtra("address", textAddress.getText());
            data.putExtra("latitude", textLatitude.getText());
            data.putExtra("longitude", textLongitude.getText());
            setResult(Activity.RESULT_OK, data);
            finish();
        });
    }

    //METHODS FOR GPS

    private void openMaps() {
        Intent i = new Intent(this, LocationPickerActivity.class);
        i.putExtra(MapUtility.LATITUDE, textLatitude.getText());
        i.putExtra(MapUtility.LONGITUDE, textLongitude.getText());
        startActivityForResult(i, RC_MAP_BUTTON);
    }

    public void getCoordinates() {
        permissionsToRequest = findUnAskedPermissions(permissions);
        locationTrack = new LocationTrack(this);


        if (locationTrack.canGetLocation()) {


            Double longitude = locationTrack.getLongitude();
            Double latitude = locationTrack.getLatitude();

            if(latitude > 90 && longitude > 180) {
                Toast.makeText(getApplicationContext(), getString(R.string.gps_not_available), Toast.LENGTH_LONG).show();
                finish();
            }

            textLatitude.setText(""+ latitude);
            textLongitude.setText(""+longitude);

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

    //METHODS FOR PHOTO PICKING

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
            viewImage.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    //METHODS FOR DATA

    private String today() {
        String d, m, y;
        d = ""+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        m = ""+(Calendar.getInstance().get(Calendar.MONTH)+1);
        y = ""+Calendar.getInstance().get(Calendar.YEAR);
        if(d.length() == 1) {
            d = "0"+d;
        }
        if(m.length() == 1) {
            m = "0"+m;
        }
        return d+"/"+m+"/"+y;
    }

    //ON ACTIVITY RESULT

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_MAP_BUTTON) {
            Log.d("GOOGLE_ADDRESS", data.getStringExtra("address"));
            try {
                if (data != null && data.getStringExtra("address") != null) {

                    Geocoder geocoder;
                    geocoder = new Geocoder(this);
                    List<Address> location = geocoder.getFromLocationName(data.getStringExtra("address"),1);


                    String address = getAddress(location.get(0));
                    textLatitude.setText(""+location.get(0).getLatitude());
                    textLongitude.setText(""+location.get(0).getLongitude());
                    textAddress.setText(address);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            checkImage();
        }
        if (requestCode == REQUEST_LOAD_IMG) {
            if(resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    viewImage.setImageBitmap(selectedImage);
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

    @Override
    protected void onResume() {
        super.onResume();
        checkImage();
    }

    // METHODS DATE



    private void setDate() {
        String FRAG_TAG_DATE_PICKER = getString(R.string.CalendarTag);
        String birthday = textDate.getText().toString();
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
                .setOnDateSetListener((dialog, year, monthOfYear, dayOfMonth) -> setDateFound(dayOfMonth,monthOfYear,year))
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setPreselectedDate(y, m, d)
                .setDoneText(getString(R.string.Confirm))
                .setCancelText(getString(R.string.Cancel))
                .setThemeLight();
        cdp.show(getSupportFragmentManager(), FRAG_TAG_DATE_PICKER);
    }

    private void setDateFound(int dayOfMonth, int monthOfYear, int year) {
        String d = ""+dayOfMonth;
        if(d.length() == 1) {
            d = "0"+d;
        }
        String m = ""+(monthOfYear+1);
        if(m.length() == 1) {
            m = "0"+m;
        }
        textDate.setText(d+"/"+m+"/"+year);
    }
}

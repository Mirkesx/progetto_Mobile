package com.marco_cavalli.lost_and_found.ui.found_objects;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.services.LocationTrack;
import com.shivtechs.maplocationpicker.LocationPickerActivity;
import com.shivtechs.maplocationpicker.MapUtility;

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

        if ( getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED  || getApplicationContext().checkSelfPermission(ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            getCoordinates();
        }

        buttonMaps.setOnClickListener(v -> openMaps());

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
                textAddress.setText(location.get(0).getLocality());
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

    //METHODS FOR PHOTO PICKING

    //METHODS FOR DATA

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



    //ON ACTIVITY RESULT

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_MAP_BUTTON) {
            Log.d("GOOGLE_ADDRESS", data.getStringExtra("address"));
            try {
                if (data != null && data.getStringExtra("address") != null) {
                    String address = data.getStringExtra("address");

                    Geocoder geocoder;
                    geocoder = new Geocoder(this);
                    List<Address> location = geocoder.getFromLocationName(address,1);

                    textLatitude.setText(""+location.get(0).getLatitude());
                    textLongitude.setText(""+location.get(0).getLongitude());
                    textAddress.setText(location.get(0).getLocality());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

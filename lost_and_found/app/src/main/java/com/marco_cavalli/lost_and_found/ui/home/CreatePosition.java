package com.marco_cavalli.lost_and_found.ui.home;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.services.LocationTrack;

import java.util.ArrayList;
import java.util.Calendar;

public class CreatePosition extends AppCompatActivity {

    private String object_id;
    private String uid;
    private EditText editDesc;
    private TextView textLat, textLon, getGPS, updateButton;

    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;

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
        textLat = findViewById(R.id.new_poition_lat);
        textLon = findViewById(R.id.new_position_lon);
        getGPS = findViewById(R.id.new_position_latlon_button);
        updateButton = findViewById(R.id.new_position_submit);

        updateButton.setOnClickListener(v -> {
            Intent data_intent = new Intent();
            if (editDesc.getText().toString().length() > 0)
                data_intent.putExtra("description", editDesc.getText().toString());
            else
                data_intent.putExtra("description", "");
            data_intent.putExtra("date", today());
            if (textLat.getText().toString().length() > 0)
                data_intent.putExtra("latitude", textLat.getText().toString());
            else
                data_intent.putExtra("latitude", "0");
            if (textLon.getText().toString().length() > 0)
                data_intent.putExtra("longitude", textLon.getText().toString());
            else
                data_intent.putExtra("longitude", "0");
            setResult(Activity.RESULT_OK, data_intent);
            finish();
        });


        getGPS.setOnClickListener(view -> getCoordinates());

        if ( getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED  || getApplicationContext().checkSelfPermission(ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            getCoordinates();
        }
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
            textLon.setText(""+longitude);
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


    private String today() {
        String d, m, y, h, min, s;
        d = ""+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        m = ""+Calendar.getInstance().get(Calendar.MONTH);
        y = ""+Calendar.getInstance().get(Calendar.YEAR);
        h = ""+Calendar.getInstance().get(Calendar.HOUR);
        min = ""+Calendar.getInstance().get(Calendar.MINUTE);
        s = ""+Calendar.getInstance().get(Calendar.SECOND);
        if(d.length() == 1) {
            d = "0"+d;
        }
        if(m.length() == 1) {
            m = "0"+m;
        }
        return d+"/"+m+"/"+y+" - "+h+":"+min+":"+s;
    }
}

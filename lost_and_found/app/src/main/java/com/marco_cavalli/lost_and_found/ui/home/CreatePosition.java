package com.marco_cavalli.lost_and_found.ui.home;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;

import android.view.View;
import android.widget.Button;
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
    private TextView textDate, textLat, textLon, getGPS, updateButton;

    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_update_object_position);

        //Retrieving data
        Intent inte = getIntent();
        Bundle data = inte.getExtras();
        object_id = data.getString("object_id");
        uid = data.getString("uid");

        //Initialize elements
        editDesc = findViewById(R.id.new_position_description);
        textDate = findViewById(R.id.new_position_date);
        textLat = findViewById(R.id.new_poition_lat);
        textLon = findViewById(R.id.new_position_lon);
        getGPS = findViewById(R.id.new_position_latlon_button);
        updateButton = findViewById(R.id.new_position_submit);

        setDate(Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.YEAR));

        updateButton.setOnClickListener(v -> {
            Intent data_intent = new Intent();
            if (editDesc.getText().toString().length() > 0)
                data_intent.putExtra("description", editDesc.getText().toString());
            else
                data_intent.putExtra("description", "");
            data_intent.putExtra("date", textDate.getText().toString());
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

        textDate.setOnClickListener(v -> {
            String FRAG_TAG_DATE_PICKER = getString(R.string.CalendarTag);
            String birthday = textDate.getText().toString();
            int y, m, d;
            if (birthday.length() > 0) {
                String[] tmp = birthday.split("/");
                d = Integer.parseInt(tmp[0]);
                m = Integer.parseInt(tmp[1]) - 1;
                y = Integer.parseInt(tmp[2]);
            } else {
                d = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                m = Calendar.getInstance().get(Calendar.MONTH);
                y = Calendar.getInstance().get(Calendar.YEAR);
            }

            CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                    .setOnDateSetListener((dialog, year, monthOfYear, dayOfMonth) -> setDate(dayOfMonth, monthOfYear, year))
                    .setFirstDayOfWeek(Calendar.SUNDAY)
                    .setPreselectedDate(y, m, d)
                    .setDoneText(getString(R.string.Confirm))
                    .setCancelText(getString(R.string.Cancel))
                    .setThemeLight();
            cdp.show(getSupportFragmentManager(), FRAG_TAG_DATE_PICKER);
        });

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions((String[])permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }


        getGPS.setOnClickListener(view -> {

            locationTrack = new LocationTrack(CreatePosition.this);


            if (locationTrack.canGetLocation()) {


                Double longitude = locationTrack.getLongitude();
                Double latitude = locationTrack.getLatitude();

                textLat.setText(""+ Math.floor(latitude*100)/100);
                textLon.setText(""+Math.floor(longitude*100)/100);
            } else {
                locationTrack.showSettingsAlert();
            }

        });
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


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale((String)permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions((String[])permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(CreatePosition.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }


    private void setDate(int dayOfMonth, int monthOfYear, int year) {
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

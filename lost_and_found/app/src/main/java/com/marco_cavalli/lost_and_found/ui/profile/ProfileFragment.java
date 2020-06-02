package com.marco_cavalli.lost_and_found.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;
import com.marco_cavalli.lost_and_found.ui.login.LoginScreen;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.User;

import java.util.Calendar;

public class ProfileFragment extends Fragment {
    private String signInMethod;
    private User user;

    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewGender;
    private TextView textViewCity;
    private TextView textViewBirthday;
    private TextView editViewGender;
    private EditText editViewCity;
    private TextView editViewBirthday;
    private Button edit;
    private Button logout;
    private Button update;
    final int REC_CODE_CALENDAR = 10;
    private int id_gender;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        signInMethod = ((Dashboard)getActivity()).getSignInMethod();
        user = ((Dashboard) getActivity()).getUser();
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        id_gender = user.getGender();


        //TEXT VIEW
        textViewName = root.findViewById(R.id.profile_display_name);
        textViewEmail = root.findViewById(R.id.profile_email);
        textViewGender = root.findViewById(R.id.profile_gender);
        textViewCity = root.findViewById(R.id.profile_city);
        textViewBirthday = root.findViewById(R.id.profile_birthday);

        //EDIT TEXT
        editViewGender = root.findViewById(R.id.profile_gender_edit);
        editViewCity = root.findViewById(R.id.profile_city_edit);
        editViewBirthday = root.findViewById(R.id.profile_birthday_edit);

        //BUTTON
        edit = root.findViewById(R.id.profile_edit);
        logout = root.findViewById(R.id.profile_logout);
        update = root.findViewById(R.id.profile_update);

        //SHOWING TEXTVIEWS
        textViewName.setText(user.getDisplayName());
        textViewEmail.setText(user.getEmail());
        textViewGender.setText(getString(id_gender));
        textViewCity.setText(user.getCity());
        textViewBirthday.setText(user.getBirthday());

        //TEXT LISTENERS
        registerForContextMenu(editViewGender);

        editViewBirthday.setOnClickListener(v -> {
            String FRAG_TAG_DATE_PICKER = getString(R.string.CalendarTag);
            String birthday = editViewBirthday.getText().toString();
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
            cdp.show(getActivity().getSupportFragmentManager(), FRAG_TAG_DATE_PICKER);
        });

        //BUTTONS LISTENERS

        edit.setOnClickListener(v -> {
            editViewGender.setText(getString(user.getGender()));
            editViewCity.setText(user.getCity());
            editViewBirthday.setText(user.getBirthday());


            root.findViewById(R.id.profile_show_layout).setVisibility(View.GONE);
            root.findViewById(R.id.profile_edit_layout).setVisibility(View.VISIBLE);
        });

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginScreen.class);
            //intent.putExtra();
            if(signInMethod.equals("Google")) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
                GoogleSignIn.getClient(getActivity(), gso).signOut();
            }
            if(signInMethod.equals("Facebook")) {
                LoginManager.getInstance().logOut();
            }
            startActivity(intent);
            getActivity().finish();
        });

        update.setOnClickListener(v -> {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();

            user.setGender(id_gender);
            user.setCity(editViewCity.getText().toString());
            user.setBirthday(editViewBirthday.getText().toString());

            myRef.child("users").child(user.getUserID()).setValue(user);

            textViewGender.setText(getString(user.getGender()));
            textViewCity.setText(user.getCity());
            textViewBirthday.setText(user.getBirthday());

            root.findViewById(R.id.profile_show_layout).setVisibility(View.VISIBLE);
            root.findViewById(R.id.profile_edit_layout).setVisibility(View.GONE);
        });

        root.findViewById(R.id.profile_show_layout).setVisibility(View.VISIBLE);
        root.findViewById(R.id.profile_edit_layout).setVisibility(View.GONE);

        return root;
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
        editViewBirthday.setText(d+"/"+m+"/"+year);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.profile_gender_edit_menu, menu);
        menu.setHeaderTitle(getString(R.string.pick_gender));
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        String result = item.getTitle().toString();
        editViewGender.setText(result);
        if(result.equals(getString(R.string.gender_male)))
            id_gender = R.string.gender_male;
        if(result.equals(getString(R.string.gender_female)))
            id_gender = R.string.gender_female;
        if(result.equals(getString(R.string.gender_not_specified)))
            id_gender = R.string.gender_not_specified;
        return true;
    }
}
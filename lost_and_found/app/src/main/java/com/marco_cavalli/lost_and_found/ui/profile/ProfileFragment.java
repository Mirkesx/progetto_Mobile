package com.marco_cavalli.lost_and_found.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.marco_cavalli.lost_and_found.Dashboard;
import com.marco_cavalli.lost_and_found.LoginScreen;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.User;

public class ProfileFragment extends Fragment {

    //private ProfileViewModel profileViewModel;
    private String signInMethod;
    private User user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        signInMethod = ((Dashboard)getActivity()).getSignInMethod();
        user = ((Dashboard) getActivity()).getUser();
        //profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        //TEXT VIEW
        final TextView textViewName = root.findViewById(R.id.profile_display_name);
        final TextView textViewEmail = root.findViewById(R.id.profile_email);
        final TextView textViewGender = root.findViewById(R.id.profile_gender);
        final TextView textViewCity = root.findViewById(R.id.profile_city);
        final TextView textViewBirthday = root.findViewById(R.id.profile_birthday);

        //EDIT TEXT
        final EditText editViewGender = root.findViewById(R.id.profile_gender_edit);
        final EditText editViewCity = root.findViewById(R.id.profile_city_edit);
        final EditText editViewBirthday = root.findViewById(R.id.profile_birthday_edit);

        //BUTTON
        final Button edit = root.findViewById(R.id.profile_edit);
        final Button logout = root.findViewById(R.id.profile_logout);
        final Button update = root.findViewById(R.id.profile_update);

        //SHOWING TEXTVIEWS
        textViewName.setText(user.getDisplayName());
        textViewEmail.setText(user.getEmail());
        textViewGender.setText(user.getGender());
        textViewCity.setText(user.getCity());
        textViewBirthday.setText(user.getBirthday());


        //BUTTONS LISTENERS

        edit.setOnClickListener(v -> {
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

            user.setGender(editViewGender.getText().toString());
            user.setCity(editViewCity.getText().toString());
            user.setBirthday(editViewBirthday.getText().toString());

            myRef.child("users").child(user.getUserID()).setValue(user);

            textViewGender.setText(user.getGender());
            textViewCity.setText(user.getCity());
            textViewBirthday.setText(user.getBirthday());

            root.findViewById(R.id.profile_show_layout).setVisibility(View.VISIBLE);
            root.findViewById(R.id.profile_edit_layout).setVisibility(View.GONE);
        });

        root.findViewById(R.id.profile_show_layout).setVisibility(View.VISIBLE);
        root.findViewById(R.id.profile_edit_layout).setVisibility(View.GONE);

        return root;
    }
}
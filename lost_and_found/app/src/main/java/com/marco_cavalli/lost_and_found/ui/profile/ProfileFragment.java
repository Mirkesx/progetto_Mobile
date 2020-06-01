package com.marco_cavalli.lost_and_found.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marco_cavalli.lost_and_found.Dashboard;
import com.marco_cavalli.lost_and_found.LoginScreen;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static android.content.ContentValues.TAG;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private String signInMethod;
    private User user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        signInMethod = ((Dashboard)getActivity()).getSignInMethod();
        user = ((Dashboard) getActivity()).getUser();
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textViewName = root.findViewById(R.id.profile_display_name);
        final TextView textViewEmail = root.findViewById(R.id.profile_email);
        final Button button = root.findViewById(R.id.logout);

        textViewName.setText(user.getDisplayName());
        textViewEmail.setText(user.getEmail());
        button.setOnClickListener(v -> {
            Log.d("LoginMarco","Auth " + FirebaseAuth.getInstance().getUid());
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
        return root;
    }
}
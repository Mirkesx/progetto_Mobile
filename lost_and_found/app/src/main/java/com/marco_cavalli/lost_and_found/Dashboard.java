package com.marco_cavalli.lost_and_found;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marco_cavalli.lost_and_found.objects.User;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Map;

public class Dashboard extends AppCompatActivity {

    private String signInMethod;
    private String uid;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        signInMethod = bundle.getString("signInMethod");
        uid = bundle.getString("uid");
        setUser(uid);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    public String getSignInMethod() {
        return signInMethod;
    }

    public User getUser() {
        return user;
    }

    public void setUser(String uid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(Map.Entry<String, Object> entry : ((Map<String,Object>) dataSnapshot.getValue()).entrySet()) {
                    if(entry.getKey().equals(uid)) {
                        String userID = ((Map) entry.getValue()).get("userID").toString();
                        String displayName = ((Map) entry.getValue()).get("displayName").toString();
                        String email = ((Map) entry.getValue()).get("email").toString();
                        String gender = ((Map) entry.getValue()).get("gender").toString();
                        String city = ((Map) entry.getValue()).get("city").toString();
                        String birthday = ((Map) entry.getValue()).get("birthday").toString();
                        user = new User(userID, displayName, email, gender, city, birthday);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("users").addListenerForSingleValueEvent(userListener);
    }
}
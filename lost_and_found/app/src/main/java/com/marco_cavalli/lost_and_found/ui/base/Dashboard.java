package com.marco_cavalli.lost_and_found.ui.base;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.PersonalObject;
import com.marco_cavalli.lost_and_found.objects.User;
import com.marco_cavalli.lost_and_found.ui.login.LoginScreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.HashMap;
import java.util.Map;

public class Dashboard extends AppCompatActivity {

    private String signInMethod;
    private String uid;
    private User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkUserLogged();
        //Bundle bundle = getIntent().getExtras();
        //signInMethod = bundle.getString("signInMethod");
        uid = FirebaseAuth.getInstance().getUid();
        setUser(uid);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_found, R.id.navigation_notifications, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    private void checkUserLogged() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginScreen.class);
            startActivity(intent);
            finish();
        }
    }

    public String getSignInMethod() {
        return signInMethod;
    }

    public User getUser() {
        return user;
    }

    public String getUID() {
        return uid;
    }

    public void setUser(String uid) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();
        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String,Object> data = ((Map<String,Object>) dataSnapshot.getValue());
                if(data.get(uid) != null) {
                    String userID = ((Map)data.get(uid)).get("userID").toString();
                    String displayName = ((Map) data.get(uid)).get("displayName").toString();
                    String email = ((Map) data.get(uid)).get("email").toString();
                    int gender = Integer.parseInt(((Map) data.get(uid)).get("gender").toString());
                    String city = "", birthday = "";
                    if(((Map) data.get(uid)).get("city") != null)
                        city = ((Map) data.get(uid)).get("city").toString();
                    if(((Map) data.get(uid)).get("birthday") != null)
                        birthday = ((Map) data.get(uid)).get("birthday").toString();
                    Map<String, PersonalObject> objs;
                    if( ((Map)data.get(uid)).get("objs") != null)
                        objs = ((Map<String, PersonalObject>) ((Map)data.get(uid)).get("objs"));
                    else
                        objs = new HashMap<>();
                    user = new User(userID, displayName, email, gender, city, birthday, objs);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        myRef.child("users").addValueEventListener(userListener);
    }
}
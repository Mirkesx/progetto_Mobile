package com.marco_cavalli.lost_and_found.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marco_cavalli.lost_and_found.BuildConfig;
import com.marco_cavalli.lost_and_found.R;
import com.marco_cavalli.lost_and_found.objects.User;
import com.marco_cavalli.lost_and_found.ui.base.Dashboard;

import java.util.Collections;
import java.util.Map;

public class LoginScreen extends BaseActivity {

    private static final int RC_SIGN_IN_GOOGLE = 64204;
    private static final int RC_SIGN_IN_FIREBASE = 64205;
    private static final int RC_SIGN_IN_FACEBOOK = 64206;

    private static final String TAG_GOOGLE = "GoogleActivity";
    private static final String TAG_FACEBOOK = "FacebookActivity";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private String signInMethod;
    private CallbackManager callbackManager;
    private String displayName;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Button listeners
        findViewById(R.id.sign_in_button_google).setOnClickListener(view -> GoogleIntent());
        findViewById(R.id.sign_in_button_firebase).setOnClickListener(view -> FirebaseIntent());

        signInMethod = "";

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        GoogleInitialization();
        FacebookInitialization();
    }

    private void GoogleInitialization() { // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    private void FacebookInitialization() { // Configure Facebook Sign In
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.sign_in_button_fb);
        loginButton.setPermissions("email","public_profile");


        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                    Log.w(TAG_FACEBOOK, "Facebook sign in succedeed");
                    handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.w(TAG_FACEBOOK, "Google sign in cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                Log.w(TAG_FACEBOOK, "Google sign in failed", exception);
            }
        });
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        checkAlreadySigned();
    }

    private void checkAlreadySigned() {
        //Check Facebook Auth already existing
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if(isLoggedIn) {
            Log.d(TAG_FACEBOOK, "Got cached sign-in");
            signInMethod = "Facebook";
            loadDashboard();
        }

        //Check Google Auth already existing
        GoogleSignInAccount lastSignedInAccount= GoogleSignIn.getLastSignedInAccount(this);
        if(lastSignedInAccount!=null){
            // user has already logged in, you can check user's email, name etc from lastSignedInAccount
            Log.d(TAG_GOOGLE, "Got cached sign-in");
            signInMethod = "Google";
            loadDashboard();
        }

        //Check FireBase Auth
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
            loadDashboard();
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try { // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                signInMethod = "Google";
                loadDashboard();
            } catch (ApiException e) { // Google Sign In failed, update UI appropriately
                Log.w(TAG_GOOGLE, "Google sign in failed", e);
            }
        } else if (requestCode == RC_SIGN_IN_FIREBASE && resultCode == RESULT_OK) { // Sign in succeeded
            signInMethod = "Firebase";
            loadDashboard();
        } else if (requestCode == RC_SIGN_IN_FACEBOOK && (AccessToken.getCurrentAccessToken() == null) )  {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            Log.w(TAG_FACEBOOK, "Facebook sign in");
            signInMethod = "Facebook";
            loadDashboard();
        } else { // Sign in failed
            Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
        }
    }
    // [END onactivityresult]

    // [START INTENT Auth]

    private void GoogleIntent() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
    }

    private void FirebaseIntent() {
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setLogo(R.mipmap.ic_launcher)
                .build();

        startActivityForResult(intent, RC_SIGN_IN_FIREBASE);
    }
    // [END INTENT Auth]


    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG_GOOGLE, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressBar();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) { // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG_GOOGLE, "signInWithCredential:success");
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.getException());
                        Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                    }
                    hideProgressBar();
                });
    }
    // [END auth_with_google]

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG_FACEBOOK, "signInWithCredential:success");
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG_FACEBOOK, "signInWithCredential:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadDashboard() {
        //Wait for the mAuth to get the Current User
        mAuth.addAuthStateListener(firebaseAuth -> {
            if(mAuth.getUid() != null) {//Check if the change is because of a sign-in event
                Intent intent = new Intent(this, Dashboard.class);
                intent.putExtra("signInMethod",signInMethod);
                intent.putExtra("uid",mAuth.getUid());
                setUserInfo();

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference();

                ValueEventListener userListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String,Object> data = ((Map<String,Object>) dataSnapshot.getValue());
                        if(data == null || data.get(mAuth.getUid()) == null) {
                            User user = new User(mAuth.getUid(), displayName, email);

                            myRef.child("users").child(mAuth.getUid()).setValue(user);
                        }
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };
                myRef.child("users").addListenerForSingleValueEvent(userListener);
            }
        });
    }

    private void setUserInfo() {
        email = mAuth.getCurrentUser().getEmail();

        switch(signInMethod) {
            case "Facebook":
                Profile profile = Profile.getCurrentProfile();
                displayName = profile.getName();
                break;
            default:
                displayName = mAuth.getCurrentUser().getDisplayName();
        }
    }
}
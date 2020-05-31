package com.marco_cavalli.lost_and_found;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.marco_cavalli.lost_and_found.objects.User;

import java.util.Collections;

public class LoginScreen extends BaseActivity {

    private static final int RC_SIGN_IN_GOOGLE = 64204;
    private static final int RC_SIGN_IN_FIREBASE = 64205;
    private static final int RC_SIGN_IN_FACEBOOK = 64206;

    private static final String TAG_GOOGLE = "GoogleActivity";
    private static final String TAG_FACEBOOK = "FacebookActivity";

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    //private TextView mStatusTextView;
    //private TextView mDetailTextView;
    private String signInMethod;
    private CallbackManager callbackManager;
    private String displayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Views
        //mStatusTextView = findViewById(R.id.status);
        //mDetailTextView = findViewById(R.id.detail);

        // Button listeners
        findViewById(R.id.sign_in_button_google).setOnClickListener(view -> GoogleIntent());
        findViewById(R.id.sign_in_button_firebase).setOnClickListener(view -> FirebaseIntent());
        //findViewById(R.id.sign_out_button).setOnClickListener(view -> signOut());
        //findViewById(R.id.disconnect_button).setOnClickListener(view -> revokeAccess());

        signInMethod = "";

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        GoogleInitialization();
        FacebookInitialization();
    }

    private void GoogleInitialization() {
        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount lastSignedInAccount= GoogleSignIn.getLastSignedInAccount(this);
        if(lastSignedInAccount!=null){
            // user has already logged in, you can check user's email, name etc from lastSignedInAccount
            Log.d(TAG_GOOGLE, "Got cached sign-in");
            signInMethod = "Google";
            loadDashboard();
        }
    }


    private void FacebookInitialization() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if(isLoggedIn) {
            //LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
            Log.d(TAG_FACEBOOK, "Got cached sign-in");
            signInMethod = "Facebook";
            loadDashboard();
            //updateUI(mAuth.getCurrentUser());
        }
        // Configure Facebook Sign In
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.sign_in_button_fb);
        loginButton.setPermissions("email","public_profile");

        // Callback registration
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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
            loadDashboard();
        //updateUI(currentUser);
    }

    /*private void signOut() {
        mAuth.signOut();

        switch(signInMethod) {
            case "Google":
                mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> //updateUI(null));
                break;
            case "Facebook":
                LoginManager.getInstance().logOut();
                break;
            default:
        }

        //updateUI(null);
    }*/

    /*private void revokeAccess() {
        mAuth.signOut();

        switch(signInMethod) {
            case "Google":
                mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, task -> updateUI(null));
                break;
            case "Facebook":
                FirebaseAuth.getInstance().signOut();
                break;
            default:
                //updateUI(null);
        }
    }*/

    /*private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            int layout_string;
            switch(signInMethod) {
                case "Google":
                    layout_string = R.string.google_status_fmt;
                    break;
                case "Facebook":
                    layout_string = R.string.facebook_status_fmt;
                    break;
                default:
                    layout_string = R.string.firebase_status_fmt;
            }
            mStatusTextView.setText(getString(layout_string, user.getEmail()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.sign_in_buttons).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.sign_in_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }*/
    // [END on_start_check_user]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
                FirebaseUser user = mAuth.getCurrentUser();
                signInMethod = "Google";
                loadDashboard();
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG_GOOGLE, "Google sign in failed", e);
                //updateUI(null);
            }
        } else if (requestCode == RC_SIGN_IN_FIREBASE && resultCode == RESULT_OK) {
            // Sign in succeeded
            mAuth.getCurrentUser();
            //updateUI(mAuth.getCurrentUser());
            signInMethod = "Firebase";
            loadDashboard();
        } else if (requestCode == RC_SIGN_IN_FACEBOOK && (AccessToken.getCurrentAccessToken() == null) )  {
            //Toast.makeText(this, requestCode+ " " +resultCode, Toast.LENGTH_SHORT).show();
            callbackManager.onActivityResult(requestCode, resultCode, data);
            Log.w(TAG_FACEBOOK, "Facebook sign in");
            signInMethod = "Facebook";
            loadDashboard();
        } else {
            // Sign in failed
            Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
            //updateUI(null);
        }
    }
    // [END onactivityresult]

    // [START INTENT Auth]

    private void GoogleIntent() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG_GOOGLE, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressBar();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG_GOOGLE, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        //updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.getException());
                        Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        //updateUI(null);
                    }

                    // [START_EXCLUDE]
                    hideProgressBar();
                    // [END_EXCLUDE]
                });
    }
    // [END auth_with_google]

    private void FirebaseIntent() {
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.EmailBuilder().build()))
                .setLogo(R.mipmap.ic_launcher)
                .build();

        startActivityForResult(intent, RC_SIGN_IN_FIREBASE);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG_FACEBOOK, "handleFacebookAccessToken:" + token.getToken());

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG_FACEBOOK, "signInWithCredential:success");
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            signInMethod = "Facebook";
                            displayName = "Facebook";
                            loadDashboard();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG_FACEBOOK, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    private void loadDashboard() {
        Log.d("LoginMarco","Auth " + mAuth.getUid());
        Intent intent = new Intent(this, Dashboard.class);
        intent.putExtra("signInMethod",signInMethod);
        startActivity(intent);
        finish();
    }

    private void handleRegistration() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users/"+mAuth.getUid());

        User user = new User("", mAuth.getCurrentUser().getEmail());;

        myRef.setValue(user);
    }
}
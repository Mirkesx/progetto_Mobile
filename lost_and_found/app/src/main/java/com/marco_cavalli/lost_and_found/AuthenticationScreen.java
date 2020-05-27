package com.marco_cavalli.lost_and_found;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Collections;

public class AuthenticationScreen extends BaseActivity {

    private static final int RC_SIGN_IN_GOOGLE = 9001;
    private static final int RC_SIGN_IN_FIREBASE = 9002;

    private static final String TAG_GOOGLE = "GoogleActivity";


    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView mStatusTextView;
    private TextView mDetailTextView;
    private String signInMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Views
        mStatusTextView = findViewById(R.id.status);
        mDetailTextView = findViewById(R.id.detail);

        // Button listeners
        findViewById(R.id.sign_in_button_google).setOnClickListener(view -> GoogleIntent());
        findViewById(R.id.sign_in_button_firebase).setOnClickListener(view -> FirebaseIntent());
        findViewById(R.id.sign_out_button).setOnClickListener(view -> signOut());
        findViewById(R.id.disconnect_button).setOnClickListener(view -> revokeAccess());

        GoogleInitialization();

        signInMethod = "";

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
    }

    private void GoogleInitialization() {
        // [START config_signin]
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // [END config_signin]

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signOut() {
        mAuth.signOut();

        switch(signInMethod) {
            case "Google":
                mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> updateUI(null));
                break;
            default:
                updateUI(null);
        }
    }

    private void revokeAccess() {
        mAuth.signOut();

        switch(signInMethod) {
            case "Google":
                mGoogleSignInClient.revokeAccess().addOnCompleteListener(this, task -> updateUI(null));
                break;
            default:
                updateUI(null);
        }
    }

    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            //mStatusTextView.setText(getString(R.string.google_status_fmt, user.getEmail()));
            mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));

            findViewById(R.id.sign_in_buttons).setVisibility(View.GONE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.VISIBLE);
        } else {
            mStatusTextView.setText(R.string.signed_out);
            mDetailTextView.setText(null);

            findViewById(R.id.sign_in_buttons).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_and_disconnect).setVisibility(View.GONE);
        }
    }
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
                signInMethod = "Google";
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG_GOOGLE, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        } else if (requestCode == RC_SIGN_IN_FIREBASE) {
            if (resultCode == RESULT_OK) {
                // Sign in succeeded
                updateUI(mAuth.getCurrentUser());
                signInMethod = "Firebase";
            } else {
                // Sign in failed
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show();
                updateUI(null);
            }
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
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG_GOOGLE, "signInWithCredential:failure", task.getException());
                        Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        updateUI(null);
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
}
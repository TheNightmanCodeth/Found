package com.diragi.found;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private final Firebase ref = new Firebase("https://foundout.firebaseio.com");
    private final String TAG = "LoginActivity";
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Firebase.setAndroidContext(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Login");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        Button login = (Button) findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText email = (EditText)findViewById(R.id.emailBox);
                EditText password = (EditText)findViewById(R.id.passwordBox);
                login(email.getText().toString(), password.getText().toString());
            }
        });
    }

    public void login(String email, String password) {
        final String emailR = email;
        final String passwd = password;
        ref.authWithPassword(email, password, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.i(TAG, "AUTH_SUCCESS: User ID: " +authData.getUid() + ", Provider: " +authData.getProvider());
                // Go to MainFeed
                startActivity(new Intent(LoginActivity.this, MainFeed.class));
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.wtf(TAG, "AUTH_ERROR: " + firebaseError.getMessage());
            }
        });
    }

    public void login(final String e, String p, String na, String u, String user) {
        final String name  = na;
        final String uid   = u;
        final String usern = user;
        ref.authWithPassword(e, p, new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                Log.i(TAG, "AUTH_SUCCESS: User ID: " +authData.getUid() + ", Provider: " +authData.getProvider());
                addUserToDb(uid, name, usern, e);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                Log.wtf(TAG, "AUTH_ERROR: " + firebaseError.getMessage());
            }
        });
    }

    private void addUserToDb(String uid, String name, String username, String email) {
        // Add user entry to DB
        Firebase userRef = ref.child("users").child(uid);
        // User info
        Map<String, String> userMap = new HashMap<String, String>();
        userMap.put("fullName", name);
        userMap.put("userName", username);
        userMap.put("userEmail", email);
        userRef.setValue(userMap);
    }
}

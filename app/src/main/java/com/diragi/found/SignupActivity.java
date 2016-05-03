package com.diragi.found;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private final Firebase ref = new Firebase("https://foundout.firebaseio.com");
    private final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Firebase.setAndroidContext(this);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Signup");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        Button button = (Button)findViewById(R.id.signupButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get string values from editTexts
                final EditText name = (EditText)findViewById(R.id.nameBox);
                final EditText username = (EditText)findViewById(R.id.userNameBox);
                final EditText email = (EditText)findViewById(R.id.emailBox);
                final EditText password = (EditText)findViewById(R.id.passwordBox);

                // Register user
                ref.createUser(email.getText().toString(), password.getText().toString(), new Firebase.ValueResultHandler<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> stringObjectMap) {
                        Log.i(TAG, "AUTH_SUCCESS: UID: " +stringObjectMap.get("uid"));
                        //Log user in
                        LoginActivity l = new LoginActivity();
                        l.login(email.getText().toString(), password.getText().toString(), name.getText().toString(), stringObjectMap.get("uid").toString(), username.getText().toString());
                        // Go to main Feed
                        Intent goHome = new Intent(SignupActivity.this, MainFeed.class);
                        startActivity(goHome);
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        Log.wtf(TAG, "AUTH_ERROR: " +firebaseError.getMessage());
                    }
                });
            }
        });
    }
}

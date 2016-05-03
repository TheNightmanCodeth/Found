package com.diragi.found;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.diragi.found.Models.TextPost;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.client.utilities.Base64;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.firebase.ui.auth.core.FirebaseLoginDialog;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainFeed extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private final String TAG = "MainFeed";
    Firebase ref;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;
    private String uid;

    RecyclerView feed;

    boolean votedUp, votedDown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Enable persistence
        // TODO: uncomment
        // Firebase.getDefaultConfig().setPersistenceEnabled(true);

        Firebase.setAndroidContext(this);
        ref = new Firebase("https://foundout.firebaseio.com");

        listenForAuthChange();

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Feed");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View coordinator = findViewById(R.id.widgetCoordinator);

        coordinator.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View view) {

            }

            @Override
            public void onViewDetachedFromWindow(View view) {
                fab.setTranslationY(0);
            }
        });

        feed = (RecyclerView)findViewById(R.id.feed);
        feed.setHasFixedSize(false);
        feed.setLayoutManager(new LinearLayoutManager(this));

        final AuthData auth = ref.getAuth();
        if (auth != null) {
            // User is authorized
            uid = auth.getUid();
            //TODO: Make the ImageView in drawer a CircleImageView and populate
            //Set username and email in navdrawer
            ref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final String username = (String) dataSnapshot.child("userName").getValue();
                    String email    = (String) dataSnapshot.child("userEmail").getValue();

                    prefs = MainFeed.this.getSharedPreferences("com.diragi.found", MODE_PRIVATE);
                    edit = prefs.edit();

                    edit.putString("currentUsername", username);
                    edit.putString("currentEmail", email);
                    edit.putString("currentUid", uid);

                    edit.apply();

                    TextView userNameDrawerText  = (TextView)findViewById(R.id.userNameDrawerText);
                    TextView userEmailDrawerText = (TextView)findViewById(R.id.userEmailDrawerText);
                    CircleImageView userIconDrawer = (CircleImageView)findViewById(R.id.navUserIcon);
                    Picasso.with(userIconDrawer.getContext()).load((String)dataSnapshot.child("userIcon").getValue()).into(userIconDrawer);
                    userNameDrawerText.setText(username);
                    userEmailDrawerText.setText(email);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.wtf(TAG, firebaseError.getMessage());
                }
            });
            // New post
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Init builder and layout
                    AlertDialog.Builder newPostAlertBuilder = new AlertDialog.Builder(MainFeed.this);
                    LayoutInflater layoutInflater = MainFeed.this.getLayoutInflater();
                    View dialogLayout = layoutInflater.inflate(R.layout.new_textpost_dialog, null);
                    newPostAlertBuilder.setView(dialogLayout);

                    // Assign EditText and submit button
                    final EditText postText = (EditText)dialogLayout.findViewById(R.id.postEditText);
                    Button imageButto = (Button)dialogLayout.findViewById(R.id.imageButton);
                    Button submitButt = (Button)dialogLayout.findViewById(R.id.submitButton);

                    final AlertDialog textPostAlert = newPostAlertBuilder.create();
                    final Map<String, String> postMap = new HashMap<String, String>();

                    imageButto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder build = new AlertDialog.Builder(MainFeed.this);
                            LayoutInflater layoutInflater = MainFeed.this.getLayoutInflater();
                            View dialogLayout = layoutInflater.inflate(R.layout.new_textpost_dialog, null);
                            build.setView(dialogLayout);

                            final EditText postText = (EditText)dialogLayout.findViewById(R.id.postEditText);
                            Button submitButt = (Button)dialogLayout.findViewById(R.id.submitButton);

                            final AlertDialog urldialog = build.create();

                            submitButt.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    postMap.put("imgLink", postText.getText().toString());
                                    urldialog.dismiss();
                                }
                            });
                            urldialog.show();
                        }
                    });

                    submitButt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Get post string from edittext
                            String post = postText.getText().toString();

                            // Send post to firebase
                            Firebase postRef = ref.child("posts").child(auth.getUid());
                            Firebase newPostRef = postRef.push();

                            postMap.put("author", prefs.getString("currentUsername", "NaN"));
                            postMap.put("content", post);
                            postMap.put("score", "0");
                            postMap.put("time", getTimestamp());
                            postMap.put("postID", newPostRef.getKey());
                            newPostRef.setValue(postMap);
                            textPostAlert.dismiss();
                        }
                    });
                    textPostAlert.show();
                }
            });
            populateList();
        } else {
            reAuth(MainFeed.this);
        }
    }

    private void populateList() {
        if (ref.getAuth() != null) {
            FirebaseRecyclerAdapter<TextPost, PostViewHolder> adapter = new FirebaseRecyclerAdapter<TextPost, PostViewHolder>(TextPost.class, R.layout.list_item, PostViewHolder.class, ref.child("posts").child(uid)) {
                @Override
                protected void populateViewHolder(final PostViewHolder postViewHolder, final TextPost textPost, int i) {
                    postViewHolder.content.setText(textPost.getContent());
                    postViewHolder.author.setText(textPost.getAuthor());
                    postViewHolder.score.setText(textPost.getScore());
                    postViewHolder.time.setText(textPost.getTime());
                    postViewHolder.img.setImageDrawable(null);

                    if (textPost.getImgLink() != null && !textPost.getImgLink().equals("")) {
                        Log.i(TAG, "Setting image");
                        Picasso.with(postViewHolder.img.getContext()).load(textPost.getImgLink()).into(postViewHolder.img);
                    }

                    final String postID = textPost.getPostID();
                    final int score = Integer.parseInt(textPost.getScore());

                    postViewHolder.up.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            postViewHolder.up.setEnabled(false);
                            postViewHolder.down.setEnabled(true);
                            // Vote up
                            Firebase voteRef = ref.child("posts").child(uid).child(postID).child("score");
                            voteRef.setValue(score + 1);
                        }
                    });

                    postViewHolder.down.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            postViewHolder.down.setEnabled(false);
                            postViewHolder.up.setEnabled(true);
                            // Post reference
                            Firebase voteRef = ref.child("posts").child(uid).child(postID).child("score");
                            voteRef.setValue(score - 1);
                        }
                    });
                }
            };
            feed.setAdapter(adapter);
        } else {
            reAuth(MainFeed.this);
        }
    }

    public String getTimestamp() {
        SimpleDateFormat timestampFormat = new SimpleDateFormat("dd/MM/yyyy-hh:mm:ss", Locale.US);
        return timestampFormat.format(new Date());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        AuthData auth = ref.getAuth();
        if (auth != null) {
            // User is authenticated
        } else {
            // User is not authenticated
            reAuth(MainFeed.this);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_settings:

                break;
            case R.id.nav_acct:
                Intent goToUserPage = new Intent(MainFeed.this, UserDetailActivity.class);
                goToUserPage.putExtra("uid", uid);
                startActivity(goToUserPage);
                break;
            case R.id.nav_logout:
                ref.unauth();
                startActivity(new Intent(MainFeed.this, LoginActivity.class));
                break;
            case R.id.nav_discover:
                startActivity(new Intent(MainFeed.this, DiscoverActivity.class));
                break;
            case R.id.nav_friends:
                startActivity(new Intent(MainFeed.this, FriendsActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView content;
        TextView author;
        TextView score;
        TextView time;

        ImageButton up;
        ImageButton down;

        ImageView img;

        public PostViewHolder(View v) {
            super(v);
            content = (TextView)v.findViewById(R.id.text1);
            author  = (TextView)v.findViewById(R.id.text2);
            score   = (TextView)v.findViewById(R.id.scoreCount);
            time    = (TextView)v.findViewById(R.id.text3);

            up = (ImageButton)v.findViewById(R.id.up);
            down = (ImageButton)v.findViewById(R.id.down);

            img = (ImageView)v.findViewById(R.id.image);
        }


    }

    public void reAuth(final Context c) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle("Not logged in!")
                .setMessage("Would you like to login now?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Go to LoginActivity
                        Intent goToLogin = new Intent(c, LoginActivity.class);
                        startActivity(goToLogin);
                    }
                })
                .setNeutralButton("Register", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Go to SignupActivity
                        Intent goToSignup = new Intent(c, SignupActivity.class);
                        startActivity(goToSignup);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                });
        AlertDialog unauthorizedAlertDialog = builder.create();
        unauthorizedAlertDialog.show();
    }

    private void listenForAuthChange() {
        ref.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    // Do nothing user is logged in
                } else {
                    // Log user in

                }
            }
        });
    }

    public Bitmap bitmapFromUrl(String url) throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
        connection.setRequestProperty("User-agent", "Mozilla/4.0");
        connection.connect();
        InputStream input = connection.getInputStream();

        return BitmapFactory.decodeStream(input);
    }
}

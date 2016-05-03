package com.diragi.found;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeTransform;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diragi.found.Models.TextPost;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserDetailActivity extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    final String TAG = "UserDetailActivity";

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean isTitleVis = false;
    private boolean isContainerVis = false;

    private TextView title;
    private LinearLayout titleContainer;

    private Firebase ref;
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;

    private RecyclerView feed;

    private String uid;
    private String user;
    private String email;
    private String name;
    private String icon;

    private MainFeed m = new MainFeed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setSharedElementExitTransition(new ChangeTransform());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        uid = getIntent().getExtras().getString("uid");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarUD);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        ref = new Firebase("https://foundout.firebaseio.com");

        Firebase.setAndroidContext(this);

        prefs = this.getSharedPreferences("com.diragi.found", MODE_PRIVATE);
        edit = prefs.edit();

        final CircleImageView userIcon = (CircleImageView) findViewById(R.id.userImage);
        final MainFeed m = new MainFeed();
        final TextView firstTitle = (TextView)findViewById(R.id.firstTitle);
        final TextView subTitle   = (TextView)findViewById(R.id.subText);
        final CollapsingToolbarLayout c = (CollapsingToolbarLayout)findViewById(R.id.main_collapsing);
        final Button follow = (Button)findViewById(R.id.followButton);
        title = (TextView)findViewById(R.id.main_textview_title);
        titleContainer = (LinearLayout)findViewById(R.id.main_linearlayout_title);

        ref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = (String) dataSnapshot.child("userName").getValue();
                email = (String) dataSnapshot.child("userEmail").getValue();
                name = (String) dataSnapshot.child("fullName").getValue();
                icon = (String) dataSnapshot.child("userIcon").getValue();
                //TODO: Check if current user is already following this one, or if this is the current user, and make the follow button red and unfollow
                firstTitle.setText(name);
                title.setText(user);
                subTitle.setText(email);
                setIcon(userIcon, icon);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add this users' uid to logged in users' following
                AuthData a = ref.getAuth();

                Map<String, String> following = new HashMap<String, String>();

                // Send post to firebase
                Firebase postRef = ref.child("users").child(a.getUid()).child("following");
                Firebase newPostRef = postRef.push();

                following.put("userName", user);
                following.put("fullName", name);
                following.put("userEmail", email);
                following.put("followID", newPostRef.getKey());
                following.put("userIcon", icon);
                newPostRef.setValue(following);
            }
        });
        // TODO: Actual custom user backgrounds

        AppBarLayout a = (AppBarLayout)findViewById(R.id.app_bar);

        a.addOnOffsetChangedListener(this);

        startAlphaAnimation(title, 0, View.INVISIBLE);

        // User feed
        feed = (RecyclerView)findViewById(R.id.feed);
        feed.setHasFixedSize(true);
        feed.setLayoutManager(new LinearLayoutManager(this));

        populateList();
    }

    private void setIcon(final CircleImageView i, final String link) {
        Log.i(TAG, "Setting image");
        Picasso.with(i.getContext()).load(link).into(i);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percent = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percent);
        handleToolbarVis(percent);
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
            m.reAuth(UserDetailActivity.this);
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
            m.reAuth(UserDetailActivity.this);
        }
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



    private void handleToolbarVis(float percent) {
        if (percent >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
            if (!isTitleVis) {
                startAlphaAnimation(title, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                isTitleVis = true;
            }
        } else {
            if (isTitleVis) {
                startAlphaAnimation(title, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                isTitleVis = false;
            }
        }
    }

    public static void startAlphaAnimation(View v, long dur, int vis) {
        AlphaAnimation a = (vis == View.VISIBLE) ? new AlphaAnimation(0f, 1f) : new AlphaAnimation(1f, 0f);

        a.setDuration(dur);
        a.setFillAfter(true);
        v.startAnimation(a);
    }

    private void handleAlphaOnTitle(float percent) {
        if (percent >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if (isContainerVis) {
                startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                isContainerVis = false;
            }
        } else {
            if (!isContainerVis) {
                startAlphaAnimation(titleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                isContainerVis = true;
            }
        }
    }
}

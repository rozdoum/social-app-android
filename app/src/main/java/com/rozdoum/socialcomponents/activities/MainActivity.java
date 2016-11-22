package com.rozdoum.socialcomponents.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.adapters.PostsAdapter;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnDataChangedListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.GoogleApiHelper;
import com.rozdoum.socialcomponents.utils.LogUtil;

import java.util.List;

public class MainActivity extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private ListView postsListView;
    private PostsAdapter postsAdapter;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user == null) {
            startLoginActivity();
        } else {
            initContentView();
        }
    }

    private void startLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void initContentView() {
        if (postsListView == null) {
            FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.addNewPostFab);

            if (floatingActionButton != null) {
                floatingActionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openCreatePostActivity();
                    }
                });
            }

            postsListView = (ListView) findViewById(R.id.postsListView);
            postsAdapter = new PostsAdapter(this);
            postsListView.setAdapter(postsAdapter);

            OnDataChangedListener<Post> onPostsDataChangedListener = new OnDataChangedListener<Post>() {
                @Override
                public void onListChanged(List<Post> list) {
                    postsAdapter.setList(list);
                }
            };

            PostManager.getInstance(getApplicationContext()).getPosts(onPostsDataChangedListener);

            postsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Post post = (Post) postsAdapter.getItem(position);

                    Intent intent = new Intent(MainActivity.this, PostDetailsActivity.class);
                    intent.putExtra(PostDetailsActivity.POST_EXTRA_KEY, post);
                    startActivity(intent);
                }
            });
        }
    }

    private void openCreatePostActivity() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivity(intent);
    }

    private void signOut() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                String providerId = profile.getProviderId();
                logoutByProvider(providerId);
            }
            logoutFirebase();
        }
    }

    private void logoutByProvider(String providerId) {
        switch (providerId) {
            case GoogleAuthProvider.PROVIDER_ID :
                logoutGoogle();
                break;

            case FacebookAuthProvider.PROVIDER_ID :
                logoutFacebook();
                break;
        }
    }

    private void logoutFirebase() {
        mAuth.signOut();
        startLoginActivity();
    }

    private void logoutFacebook() {
        LoginManager.getInstance().logOut();
    }

    private void logoutGoogle() {
        final GoogleApiClient mGoogleApiClient = GoogleApiHelper.createGoogleApiClient(this);
        mGoogleApiClient.connect();
        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                if (mGoogleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                LogUtil.logDebug(TAG, "User Logged out from Google");
                            } else {
                                LogUtil.logDebug(TAG, "Error Logged out from Google");
                            }
                        }
                    });
                }
            }

            @Override
            public void onConnectionSuspended(int i) {
                LogUtil.logDebug(TAG, "Google API Client Connection Suspended");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.signOut:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtil.logDebug(TAG, "onConnectionFailed:" + connectionResult);
    }
}

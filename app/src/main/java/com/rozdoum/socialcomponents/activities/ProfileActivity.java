package com.rozdoum.socialcomponents.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.FacebookSdk;
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
import com.rozdoum.socialcomponents.adapters.PostsByUserAdapter;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.GoogleApiHelper;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.utils.PreferencesUtil;

public class ProfileActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = ProfileActivity.class.getSimpleName();
    public static final int OPEN_PROFILE_REQUEST = 22;
    public static final String USER_ID_EXTRA_KEY = "ProfileActivity.USER_ID_EXTRA_KEY";

    // UI references.
    private TextView nameEditText;
    private ImageView imageView;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView postsCounterTextView;
    private TextView postsLabelTextView;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private String currentUserId;
    private String userID;

    private PostsByUserAdapter postsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        userID = getIntent().getStringExtra(USER_ID_EXTRA_KEY);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
        }

        // Set up the login form.
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imageView = (ImageView) findViewById(R.id.imageView);
        nameEditText = (TextView) findViewById(R.id.nameEditText);
        postsCounterTextView = (TextView) findViewById(R.id.postsCounterTextView);
        postsLabelTextView = (TextView) findViewById(R.id.postsLabelTextView);

        loadPostsList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setResult(RESULT_OK);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadProfile();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CreatePostActivity.CREATE_NEW_POST_REQUEST) {
                postsAdapter.loadPosts();
                showSnackBar(R.string.message_post_was_created);
            }

            if (requestCode == PostDetailsActivity.UPDATE_COUNTERS_REQUEST) {
                postsAdapter.updateSelectedPost();
            }
        }
    }

    private void loadPostsList() {
        if (recyclerView == null) {

            recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            postsAdapter = new PostsByUserAdapter(this, userID);
            postsAdapter.setOnItemClickListener(new PostsByUserAdapter.CallBack() {
                @Override
                public void onItemClick(Post post) {
                    Intent intent = new Intent(ProfileActivity.this, PostDetailsActivity.class);
                    intent.putExtra(PostDetailsActivity.POST_EXTRA_KEY, post);
                    startActivityForResult(intent, PostDetailsActivity.UPDATE_COUNTERS_REQUEST);
                }

                @Override
                public void onPostsLoaded(int postsCount) {
                    postsCounterTextView.setText(getResources().getQuantityString(R.plurals.posts_counter_format, postsCount, postsCount));

                    if (postsCount > 0) {
                        postsLabelTextView.setVisibility(View.VISIBLE);
                    }
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(postsAdapter);
            recyclerView.setNestedScrollingEnabled(false);
            postsAdapter.loadPosts();
        }
    }

    private void loadProfile() {
        showProgress();
        ProfileManager.getInstance(this).getProfileSingleValue(userID, createOnProfileChangedListener());
    }

    private OnObjectChangedListener<Profile> createOnProfileChangedListener() {
        return new OnObjectChangedListener<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                fillUIFields(obj);
            }
        };
    }

    private void fillUIFields(Profile profile) {
        if (profile != null) {
            nameEditText.setText(profile.getUsername());

            if (profile.getPhotoUrl() != null) {
                ImageUtil.getInstance(this).getFullImage(profile.getPhotoUrl(), imageView, R.drawable.ic_stub);
            }
        }
        hideProgress();
    }

    private void startMainActivity() {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startEditProfileActivity() {
        Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
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

        ImageUtil.getInstance(this).clearCache();
    }

    private void logoutByProvider(String providerId) {
        switch (providerId) {
            case GoogleAuthProvider.PROVIDER_ID:
                logoutGoogle();
                break;

            case FacebookAuthProvider.PROVIDER_ID:
                logoutFacebook();
                break;
        }
    }

    private void logoutFirebase() {
        mAuth.signOut();
        PreferencesUtil.setProfileCreated(this, false);
        startMainActivity();
    }

    private void logoutFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().logOut();
    }

    private void logoutGoogle() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiHelper.createGoogleApiClient(this);
        }

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LogUtil.logDebug(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void openCreatePostActivity() {
        Intent intent = new Intent(this, CreatePostActivity.class);
        startActivityForResult(intent, CreatePostActivity.CREATE_NEW_POST_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userID.equals(currentUserId)) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.profile_menu, menu);
            return true;
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.editProfile:
                imageView.setImageResource(R.drawable.ic_stub);
                startEditProfileActivity();
                return true;
            case R.id.signOut:
                signOut();
                return true;
            case R.id.createPost:
                if (hasInternetConnection()) {
                    openCreatePostActivity();
                } else {
                    showSnackBar(R.string.internet_connection_failed);
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

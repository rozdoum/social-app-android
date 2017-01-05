package com.rozdoum.socialcomponents.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.enums.ProfileStatus;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.model.Profile;

/**
 * Created by alexey on 05.12.16.
 */

public class BaseActivity extends AppCompatActivity {

    public ProgressDialog progressDialog;
    public ActionBar actionBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();

    }

    public void doAuthorization(ProfileStatus status) {
        if (status.equals(ProfileStatus.NOT_AUTHORIZED)) {
            startLoginActivity();
        } else if (status.equals(ProfileStatus.NO_PROFILE)) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Profile profile = ProfileManager.getInstance(this).buildProfile(user);
            startCreateProfileActivity(profile);
        }
    }

    private void startCreateProfileActivity(Profile profile) {
        Intent intent = new Intent(this, CreateProfileActivity.class);
        intent.putExtra(CreateProfileActivity.PROFILE_EXTRA_KEY, profile);
        startActivity(intent);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void showProgress() {
        showProgress(R.string.loading);
    }

    public void showProgress(int message) {
        hideProgress();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(message));
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void showSnackBar(int messageId) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                messageId, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void showSnackBar(View view, int messageId) {
        Snackbar snackbar = Snackbar.make(view, messageId, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    public void showWarningDialog(int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.button_ok, null);
        builder.show();
    }

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public boolean checkInternetConnection() {
        boolean hasInternetConnection = hasInternetConnection();
        if (!hasInternetConnection) {
            showWarningDialog(R.string.internet_connection_failed);
        }

        return hasInternetConnection;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}

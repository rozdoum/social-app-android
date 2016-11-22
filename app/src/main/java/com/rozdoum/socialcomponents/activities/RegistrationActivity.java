package com.rozdoum.socialcomponents.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.utils.ValidationUtil;

public class RegistrationActivity extends AppCompatActivity {

    // UI references.
    private EditText emailEditText;
    private EditText nameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        // Set up the login form.
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        nameEditText = (AutoCompleteTextView) findViewById(R.id.nameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegistration();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });
    }

    private void attemptRegistration() {

        // Reset errors.
        emailEditText.setError(null);
        nameEditText.setError(null);
        passwordEditText.setError(null);
        confirmPasswordEditText.setError(null);

        // Store values at the time of the login attempt.
        String email = emailEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_field_required));
            if (!cancel) {
                focusView = emailEditText;
                cancel = true;
            }
        } else if (!ValidationUtil.isEmailValid(email)) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            if (!cancel) {
                focusView = emailEditText;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError(getString(R.string.error_field_required));
            focusView = nameEditText;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_field_required));
            if (!cancel) {
                focusView = passwordEditText;
                cancel = true;
            }
        } else if (!ValidationUtil.isPasswordValid(password)) {
            passwordEditText.setError(getString(R.string.error_invalid_password));
            if (!cancel) {
                focusView = passwordEditText;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.error_field_required));
            if (!cancel) {
                focusView = confirmPasswordEditText;
                cancel = true;
            }
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordEditText.setError(getString(R.string.error_confirm_password));
            if (!cancel) {
                focusView = confirmPasswordEditText;
                cancel = true;
            }
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgressDialog();

            // TODO: 21.11.16 do registration
        }
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


}


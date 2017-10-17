package com.example.eric.cueanews;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static com.example.eric.cueanews.LoginActivity.LOGIN_NAME;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
    private static final String TAG = "RegisterActivity";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mFirstNameView;
    private AutoCompleteTextView mLastNameView;
    private AutoCompleteTextView mUsernameView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private TextView mLoginAlert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupActionBar();

        //hide inputs on window create
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Set up the login form.
        mFirstNameView = (AutoCompleteTextView) findViewById(R.id.first_name);
        mLastNameView = (AutoCompleteTextView) findViewById(R.id.last_name);
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mConfirmPasswordView = (EditText) findViewById(R.id.confirm_password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        //setup login alert
        mLoginAlert = (TextView) findViewById(R.id.login_alert);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        mAuthTask = null;

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        //Reset alert
        mLoginAlert.setText("");

        // Store values at the time of the login attempt.
        String first_name = mFirstNameView.getText().toString();
        String last_name = mLastNameView.getText().toString();
        String username = mUsernameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirm_password = mConfirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if(!doPasswordsMatch(password, confirm_password)) {
            mConfirmPasswordView.setError(getString(R.string.error_password_match));
            focusView = mConfirmPasswordView;
            cancel = true;
        }

        // Check for a valid name.
        if (TextUtils.isEmpty(first_name)) {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        } else if (!isNameValid(first_name)) {
            mFirstNameView.setError(getString(R.string.error_invalid_name));
            focusView = mFirstNameView;
            cancel = true;
        }

        // Check for a valid name.
        if (TextUtils.isEmpty(last_name)) {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        } else if (!isNameValid(last_name)) {
            mLastNameView.setError(getString(R.string.error_invalid_name));
            focusView = mLastNameView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserRegisterTask(first_name, last_name, username, email, password);
            mAuthTask.postSignUpData();
        }
    }

    private boolean isNameValid(String name) {
        //TODO: Replace this with your own logic
        return name.length() > 2 && name.length() <= 30;
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length() >= 6;
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean doPasswordsMatch(String password, String confirmPassword) {
        //TODO: Replace this with your own logic
        return ((password.length() == confirmPassword.length()) && (password == confirmPassword));
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 6 && password.length() <= 20;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginAlert.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                    mLoginAlert.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginAlert.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask implements AuthRegisterJsonData.OnSignUpDataAvailable {
        private static final String TAG = "UserRegisterTask";

        private final String mFirstName;
        private final String mLastName;
        private final String mUsername;
        private final String mEmail;
        private final String mPassword;

        public UserRegisterTask(String firstName, String lastName, String username, String email, String password) {
            mFirstName = firstName;
            mLastName = lastName;
            mUsername = username;
            mEmail = email;
            mPassword = password;
        }

        public void postSignUpData() {
            Log.d(TAG, "postSignUpData: starts");

            JSONObject user = new JSONObject();

            try {

                user.put("first_name", mFirstName);
                user.put("last_name", mLastName);
                user.put("username", mUsername);
                user.put("email", mEmail);
                user.put("password", mPassword);

            } catch (JSONException jsone) {
                Log.d(TAG, "postSignUpData: Error creating Json data " + jsone.getMessage());
            }

            if (user.length() > 0) {
                Log.d(TAG, "postSignUpData: user sign up details json parsed");
                AuthRegisterJsonData userSignUp = new AuthRegisterJsonData(this, user.toString());
                userSignUp.execute((String[]) null);
            }

            Log.d(TAG, "postSignUpData: ends");
        }

        @Override
        public void onSignUpDataAvailable(String data, DownloadStatus status, int responseCode) {
            Log.d(TAG, "onSignUpDataAvailable: received data " + data);

            showProgress(false);

            String avatar = null;
            String name = null;
            String first_name = null;
            String last_name = null;
            String email = null;
            String username = null;
            String password = null;

            if (data != null) {
                if (status == DownloadStatus.OK) {
                    try {
                        JSONObject jsonData = new JSONObject(data);

                        first_name = jsonData.has("first_name") ? jsonData.getString("first_name").replace("[", "").replace("]", "") : "";
                        last_name = jsonData.has("last_name") ? jsonData.getString("last_name").replace("[", "").replace("]", "") : "";
                        email = jsonData.has("email") ? jsonData.getString("email").replace("[", "").replace("]", "") : "";

                        name = jsonData.has("name") ? jsonData.getString("name") : "";
                        avatar = jsonData.has("avatar") ? jsonData.getString("avatar") : "";
                        username = jsonData.has("username") ? jsonData.getString("username").replace("[", "").replace("]", "") : "";
                        password = jsonData.has("password") ? jsonData.getString("password").replace("[", "").replace("]", "") : "";

                    } catch (JSONException jsone) {
                        jsone.printStackTrace();

                        Log.e(TAG, "onSignUpDataAvailable: Error processing Json data " + jsone.getMessage());
                    }
                }
            }

            Log.d(TAG, "onSignUpDataAvailable: Sign up returned name " + name + " with avatar " + avatar + " with response code " + responseCode);

            if (((responseCode / 100) == 2) &&
                    (name != null) && (avatar != null)) {
                Log.d(TAG, "onSignUpDataAvailable: Sign up successful.");
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.putExtra(LOGIN_NAME, mAuthTask.mEmail);
                startActivity(intent);

                mAuthTask = null;
                mLoginAlert.setText("");
                finish();
            } else if (((first_name != null) && (first_name != mFirstName)) ||
                    ((last_name != null) && (last_name != mLastName)) ||
                    ((username != null) && (username != mUsername)) ||
                    ((email != null) && (email != mEmail)) ||
                    ((password != null) && (password != mPassword))) {
                String errors;
                first_name = first_name != null ? first_name.toString() + "\n" : "";
                last_name = last_name != null ? last_name.toString() + "\n" : "";
                username = username != null ? username.toString() + "\n" : null;
                email = email != null ? email.toString() : null;
                password = password != null ? "\n" + password.toString() : null;

                errors = new StringBuilder()
                        .append(first_name)
                        .append(last_name)
                        .append(username)
                        .append(email)
                        .append(password)
                        .toString();

                mPasswordView.setText("");
                mLoginAlert.setText(errors);

                Log.d(TAG, "onSignUpDataAvailable: Sign up failed with errors.");
            } else {
                mLoginAlert.setText("Sign up failed. Try again!");
                Log.d(TAG, "onSignUpDataAvailable: Sign up failed without data. Try again!");
            }

            Log.d(TAG, "onSignUpDataAvailable: ends");
        }
    }
}


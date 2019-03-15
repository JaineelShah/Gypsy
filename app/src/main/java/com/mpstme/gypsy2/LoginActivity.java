package com.mpstme.gypsy2;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.ason.Ason;
import com.afollestad.bridge.Bridge;
import com.afollestad.bridge.BridgeException;
import com.afollestad.bridge.Form;
import com.afollestad.bridge.Request;
import com.afollestad.bridge.Response;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    public static final String PREFERENCES = "MyPreferences";
    private static final int REQUEST_SIGNUP = 0;
    public String login_endpoint = Keys.SERVER + "/mobile/login";

    @BindView(R.id.input_email) EditText _emailText;
    @BindView(R.id.input_password) EditText _passwordText;
    @BindView(R.id.btn_login) Button _loginButton;
    @BindView(R.id.link_signup) TextView _signupLink;

    SharedPreferences sharedPreferences;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        
        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    login();
                } catch (BridgeException e) {
                    e.printStackTrace();
                }
            }
        });

        sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                finish();
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });
    }

    public void login() throws BridgeException {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        Boolean login_success = false;
        // TODO: Implement your own authentication logic here.

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        Form form = new Form()
                .add("username", String.valueOf(email))
                .add("password", String.valueOf(password));
        Request request = Bridge
                .post(login_endpoint)
                .body(form)
                .request();

        Log.d("LOGIN_TAG", request.toString()); // read this
        Response response = request.response();
        Ason ason = new Ason(String.valueOf(response.asAsonObject()));
        Log.d("LOGIN_TEST_DATA", ason.getString("login"));

        String login_entity = ason.getString("login");
        Boolean login_status = false;
        login_status = login_entity != null && login_entity.contentEquals("success");
        SharedPreferences.Editor editor = getSharedPreferences(Keys.PREFERENCES, MODE_PRIVATE).edit();
        if(login_status){
            login_success = true;
            Log.d("PREFERENCES", "Setting preferences");
            String user_email = ason.getString("email");
            String user_full_name = ason.getString("fullname");

            editor.putString("email", user_email);
            editor.putString("full_name", user_full_name);
            editor.putBoolean("logged_status", true);
            editor.apply();

            Log.d("PREFERENCES", "Set preferences");
        }

        final Boolean finalLogin_success = login_success;
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        if (finalLogin_success){
                            onLoginSuccess();

                        } else {
                            onLoginFailed();
                        }
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("logged_status", true);
        editor.commit();
        Toast.makeText(getBaseContext(), "Login successful", Toast.LENGTH_LONG).show();
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }
}

package com.bitproject.driverapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private Button mSignup;
    private EditText mEmail, mPassword, mConfirmPassword;
    private TextView linkLogin;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        //get firebase authentication and start the map activity
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null){
                    Intent intent = new Intent(SignupActivity.this, PhoneLoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        };

        mEmail = (EditText) findViewById(R.id.txtEmail);
        mPassword = (EditText) findViewById(R.id.txtPassword);
        mConfirmPassword = (EditText) findViewById(R.id.txtConfirmPassword);
        mSignup = (Button) findViewById(R.id.btnSignup);
        linkLogin = (TextView) findViewById(R.id.link_signup);

        loadingBar =  new ProgressDialog(this, R.style.MyAlertDialogStyle);

        //signup
        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean val = validate();
                if (!validate()) {
                    onSignupFailed();
                    return;
                }

                mSignup.setEnabled(false);

                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                //final String confirmPassword = mConfirmPassword.getText().toString();

                loadingBar.setTitle("Creating New Account");
                loadingBar.setMessage("Please wait while we are creating new account for you...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()){
                            String errorMessage = task.getException().toString();
                            Toast.makeText(SignupActivity.this, "Sign up Error!  " + errorMessage, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }else {
                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id);
                            current_user_db.setValue(true);
                            Toast.makeText(SignupActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }
        });


        linkLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Sign up Failed!", Toast.LENGTH_SHORT).show();

        mSignup.setEnabled(true);
    }

    private boolean validate() {
        boolean valid = true;

        final  String email = mEmail.getText().toString();
        final  String password = mPassword.getText().toString();
        final  String confirmPassword = mConfirmPassword.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmail.setError("Enter a valid email address.");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        if (!isValidPassword(mPassword.getText().toString())) {
            mPassword.setError("Password should contain uppercase, lowercase, number and symbol with a minimum of 6 characters.");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        if (confirmPassword.isEmpty() || !(confirmPassword.equals(password))) {
            mConfirmPassword.setError("Password don't match.");
            valid = false;
        } else {
            mConfirmPassword.setError(null);
        }

        return valid;
    }

    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{6,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
}

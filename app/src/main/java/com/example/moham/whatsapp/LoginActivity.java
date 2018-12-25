package com.example.moham.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    FirebaseUser mCurrentUser;
    MaterialButton mLoginButton, mLoginUsingPhoneButton;

    TextInputLayout mEmailTextInputLayout, mPasswordTextInputLayout;
    TextInputEditText mEmailTextInputEditText, mPasswordTextInputEditText;

    TextView mForgetPasswordTextView, mCreateNewAccountTextView;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        mCreateNewAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToRegisterActivity();
            }
        });

        mLoginUsingPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(intent);
            }
        });
    }


    void init() {
        mLoginButton = findViewById(R.id.login_button);
        mEmailTextInputEditText = findViewById(R.id.email_edit_text_login);
        mPasswordTextInputEditText = findViewById(R.id.password_edit_text_login);
        mEmailTextInputLayout = findViewById(R.id.textInputLayout_email_login);
        mPasswordTextInputLayout = findViewById(R.id.password_text_input_login);
        mForgetPasswordTextView = findViewById(R.id.forget_password_tv);
        mCreateNewAccountTextView = findViewById(R.id.create_new_account_tv);
        mLoginUsingPhoneButton = findViewById(R.id.login_using_phone_button);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mCurrentUser != null) {

            navigateToMainActivity();
        }
    }


    void login() {

        String email = mEmailTextInputEditText.getText().toString().trim();
        String password = mPasswordTextInputEditText.getText().toString().trim();
        if (!isPasswordValid(password)) {
            mPasswordTextInputLayout.setError(getString(R.string.error_password));
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(LoginActivity.this, "Sucess failed.",
                                        Toast.LENGTH_SHORT).show();

                                Log.d("", "signInWithEmail:success");
                                navigateToMainActivity();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("", "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void navigateToMainActivity() {
        Intent loginIntent = new Intent(LoginActivity.this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    private void navigateToRegisterActivity() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    private boolean isPasswordValid(@Nullable String text) {
        return text != null && text.length() >= 8;
    }
}

package com.example.moham.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    MaterialButton mSignUpButton;

    TextInputLayout mEmailTextInputLayout, mPasswordTextInputLayout;
    TextInputEditText mEmailTextInputEditText, mPasswordTextInputEditText;

    TextView mAlreadyHaveAnAccountTextView;
    private FirebaseAuth mAuth;
    ProgressBar mProgressBar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        init();
    }


    void init() {
        mSignUpButton = findViewById(R.id.sign_up_button);
        mEmailTextInputEditText = findViewById(R.id.email_edit_text_register);
        mPasswordTextInputEditText = findViewById(R.id.password_edit_text_register);
        mEmailTextInputLayout = findViewById(R.id.textInputLayout_email_register);
        mPasswordTextInputLayout = findViewById(R.id.password_text_input_register);
        mAlreadyHaveAnAccountTextView = findViewById(R.id.already_have_an_account_tv);
        mProgressBar = findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAlreadyHaveAnAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToLoginActivity();
            }
        });


        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

    }

    void createNewAccount() {

        String email = mEmailTextInputEditText.getText().toString();
        String password = mPasswordTextInputEditText.getText().toString();


        if (!isPasswordValid(password)) {
            mPasswordTextInputLayout.setError(getString(R.string.error_password));
        } else {
            mPasswordTextInputLayout.setError(null);
            mProgressBar.setVisibility(View.VISIBLE);
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        String currentUserId = mAuth.getCurrentUser().getUid();
                        mDatabase.child("Users").child(currentUserId).setValue("");

                        mDatabase.child("Users").child(currentUserId).child("device_token")
                                .setValue(deviceToken);

                        mProgressBar.setVisibility(View.GONE);
                        navigateToMainActivity();



                        Toast.makeText(RegisterActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                    } else {

                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }


    private boolean isPasswordValid(@Nullable String text) {
        return text != null && text.length() >= 8;
    }

    private void navigateToLoginActivity() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }


    private void navigateToMainActivity() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}


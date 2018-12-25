package com.example.moham.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private MaterialButton mSendVerficationButton, mVerifyButton;
    private EditText mPhoneNumberEditText, mVerficationCodeEditText;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerficationId;
    private PhoneAuthProvider.ForceResendingToken mForceResendingToken;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        init();

        mSendVerficationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber =
                        mPhoneNumberEditText.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)){

                    Toast.makeText(PhoneLoginActivity.this, "please enter phone", Toast.LENGTH_SHORT).show();
                }else {

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            mCallbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Toast.makeText(
                        PhoneLoginActivity.this,
                        "please enter correct phone number with your country code",
                        Toast.LENGTH_SHORT)
                        .show();

                mSendVerficationButton.setVisibility(View.VISIBLE);
                mPhoneNumberEditText.setVisibility(View.VISIBLE);

                mVerficationCodeEditText.setVisibility(View.INVISIBLE);
                mVerifyButton.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // Save verification ID and resending token so we can use them later
                mVerficationId = verificationId;
                mForceResendingToken = token;
                Toast.makeText(PhoneLoginActivity.this, "code has been sent", Toast.LENGTH_SHORT).show();

                mSendVerficationButton.setVisibility(View.INVISIBLE);
                mPhoneNumberEditText.setVisibility(View.INVISIBLE);

                mVerficationCodeEditText.setVisibility(View.VISIBLE);
                mVerifyButton.setVisibility(View.VISIBLE);


            }
        };


        mVerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendVerficationButton.setVisibility(View.INVISIBLE);
                mPhoneNumberEditText.setVisibility(View.INVISIBLE);

                String verificationCode = mVerficationCodeEditText.getText().toString();

                if (TextUtils.isEmpty(verificationCode)){

                    Toast.makeText(PhoneLoginActivity.this, "please write verification code first", Toast.LENGTH_SHORT).show();

                }else {

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerficationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);

                }

            }
        });

    }
    private void init() {
        mSendVerficationButton = findViewById(R.id.send_verification_code_buttom);
        mVerifyButton = findViewById(R.id.verify_button);
        mPhoneNumberEditText = findViewById(R.id.phone_number_ed);
        mVerficationCodeEditText = findViewById(R.id.verification_code_ed);
        mAuth = FirebaseAuth.getInstance();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                         navigateToMainActivity();
                        } else {

                            }
                        }

                });
    }
    private void navigateToMainActivity() {
        Intent loginIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}

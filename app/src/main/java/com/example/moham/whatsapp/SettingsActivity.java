package com.example.moham.whatsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView mUserImagecircleImageView;
    private MaterialButton mUpdateButton;
    private TextInputEditText mSetUserNameEditText, mUserStatusEditText;
    private String mCurrentUserID, mDownloadImageUrl;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private static final int GALLARYPICK = 1;
    private StorageReference mUserProfileImageRef;
    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateSettings();

            }
        });


        mUserImagecircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageIntent =
                        new Intent();
                imageIntent.setType("image/*");
                imageIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(imageIntent, GALLARYPICK);

            }
        });
        retieveUserRefrence();
    }

    private void init() {
        mUserImagecircleImageView = findViewById(R.id.set_profile_image_civ);
        mUpdateButton = findViewById(R.id.update_setting_button);
        mSetUserNameEditText = findViewById(R.id.user_name_settings_ed);
        mUserStatusEditText = findViewById(R.id.update_status_ed);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");

    }

    void updateSettings() {
        final String setName = mSetUserNameEditText.getText().toString();
        final String setStatus = mUserStatusEditText.getText().toString();


        if (TextUtils.isEmpty(setName)) {
            Toast.makeText(SettingsActivity.this, "please write username", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(setStatus)) {

            Toast.makeText(SettingsActivity.this, "please write your status", Toast.LENGTH_SHORT).show();
        } else {

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", mCurrentUserID);
            profileMap.put("name", setName);
            profileMap.put("status", setStatus);
            mDatabase.child("Users").child(mCurrentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                navigateToMainActivity();
                                Toast.makeText(SettingsActivity.this, "isSuccessful", Toast.LENGTH_SHORT).show();
                            } else {

                                Toast.makeText(SettingsActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLARYPICK && resultCode == RESULT_OK && data != null) {

            imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                final StorageReference filePath = mUserProfileImageRef.child(imageUri.getLastPathSegment() + "currentDate" + ".jpg");

                final UploadTask uploadTask = filePath.putFile(imageUri);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String photoLink = uri.toString();

                                mDatabase.child("Users").child(mCurrentUserID).child("image")
                                        .setValue(photoLink)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {

                                                    Toast.makeText(SettingsActivity.this, "image Saved", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        });

                        Toast.makeText(SettingsActivity.this, "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();

                        Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                mDownloadImageUrl = filePath.getDownloadUrl().toString();
                                return filePath.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(SettingsActivity.this, "Product Save in Database", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }
                });
            }
        }
    }


    void retieveUserRefrence() {

        mDatabase.child("Users").child(mCurrentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")) {

                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveSatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                            mSetUserNameEditText.setText(retrieveUserName);
                            mUserStatusEditText.setText(retrieveSatus);
                            Picasso.get().load(retrieveProfileImage).into(mUserImagecircleImageView);

                        } else if (dataSnapshot.exists() && dataSnapshot.hasChild("name")) {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveSatus = dataSnapshot.child("status").getValue().toString();

                            mSetUserNameEditText.setText(retrieveUserName);
                            mUserStatusEditText.setText(retrieveSatus);

                        } else {

                            Toast.makeText(SettingsActivity.this, "please set and update your profile info", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

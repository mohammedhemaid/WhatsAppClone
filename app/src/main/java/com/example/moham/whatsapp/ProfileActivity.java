package com.example.moham.whatsapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    private String receiverUserID, mCurrentUserId, mCurrentState;

    private TextView mUserName, mUserStatus;
    private CircleImageView profileImage;
    private MaterialButton mSendMessageButton, mCancelMessageButton;
    private DatabaseReference mUserRef, mChatRequestRef, mContactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        Toast.makeText(this, "" + receiverUserID, Toast.LENGTH_SHORT).show();

        init();

        retrieveUserInfo();
    }

    private void init() {

        mUserName = findViewById(R.id.profile_username_tv);
        mUserStatus = findViewById(R.id.profile_status_tv);
        profileImage = findViewById(R.id.profile_image_civ);
        mSendMessageButton = findViewById(R.id.profile_send_message_btn);
        mCancelMessageButton = findViewById(R.id.profile_cancel_message_btn);

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mCurrentState = "new";
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        mContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

    }

    private void retrieveUserInfo() {

        mUserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChild("image")) {

                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String username = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();


                    Picasso.get().load(userImage).placeholder(R.drawable.ic_person_black_24dp).into(profileImage);
                    mUserName.setText(username);
                    mUserStatus.setText(userStatus);

                    manageChatRequest();


                } else {

                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStauts = dataSnapshot.child("status").getValue().toString();

                    mUserName.setText(userName);
                    mUserStatus.setText(userStauts);

                    manageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void manageChatRequest() {


        mChatRequestRef.child(mCurrentUserId).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(receiverUserID)) {
                            String requesttype = dataSnapshot.child(receiverUserID)
                                    .child("request_type")
                                    .getValue()
                                    .toString();


                            if (requesttype.equals("sent")) {

                                mCurrentState = "request_sent";
                                mSendMessageButton.setText("Cancel Chat Request");
                            } else if (requesttype.equals("received")) {

                                mCurrentState = "request_received";
                                mSendMessageButton.setText("Accept Request");
                                mCancelMessageButton.setVisibility(View.VISIBLE);
                                mCancelMessageButton.setEnabled(true);

                                mCancelMessageButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        CancelChatRequest();
                                    }
                                });
                            }
                        } else {
                            mContactsRef.child(mCurrentUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            if (dataSnapshot.hasChild(receiverUserID)){

                                                mCurrentState = "friends";
                                                mSendMessageButton.setText("Remove This Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


        if (!mCurrentUserId.equals(receiverUserID)) {

            mSendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mSendMessageButton.setEnabled(false);

                    if (mCurrentState.equals("new")) {
                        sendChatRequest();
                    }
                    if (mCurrentState.equals("request_sent")) {

                        CancelChatRequest();
                    }
                    if (mCurrentState.equals("request_received")) {

                        AcceptChatRequest();
                    } if (mCurrentState.equals("friends")) {
                        removeSpecificContacts();

                    }

                }
            });
        } else {

            mSendMessageButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContacts() {
        mContactsRef.child(mCurrentUserId).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            mContactsRef    .child(receiverUserID).child(mCurrentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                mSendMessageButton.setEnabled(true);
                                                mCurrentState = "new";
                                                mSendMessageButton.setText("Send Message");

                                                mCancelMessageButton.setVisibility(View.INVISIBLE);
                                                mCancelMessageButton.setEnabled(false);

                                            }
                                        }
                                    });


                        }
                    }
                });
    }

    private void AcceptChatRequest() {

        mContactsRef.child(mCurrentUserId).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            mContactsRef.child(receiverUserID).child(mCurrentUserId)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                mChatRequestRef.child(mCurrentUserId)
                                                        .child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()) {

                                                                    mChatRequestRef.child(receiverUserID)
                                                                            .child(mCurrentUserId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {


                                                                                    mSendMessageButton.setEnabled(true);
                                                                                    mCurrentState = "friends";
                                                                                    mSendMessageButton.setText("Remove this Contact");
                                                                                    mCancelMessageButton.setVisibility(View.INVISIBLE);
                                                                                    mCancelMessageButton.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });

                                            }
                                        }
                                    });
                        }
                    }
                });

    }

    private void CancelChatRequest() {
        mChatRequestRef.child(mCurrentUserId).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {

                            mChatRequestRef.child(receiverUserID).child(mCurrentUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                mSendMessageButton.setEnabled(true);
                                                mCurrentState = "new";
                                                mSendMessageButton.setText("Send Message");

                                                mCancelMessageButton.setVisibility(View.INVISIBLE);
                                                mCancelMessageButton.setEnabled(false);

                                            }
                                        }
                                    });


                        }
                    }
                });
    }

    private void sendChatRequest() {
        mChatRequestRef.child(mCurrentUserId).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            mChatRequestRef.child(receiverUserID).child(mCurrentUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                mSendMessageButton.setEnabled(true);
                                                mCurrentState = "request_sent";
                                                mSendMessageButton.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}

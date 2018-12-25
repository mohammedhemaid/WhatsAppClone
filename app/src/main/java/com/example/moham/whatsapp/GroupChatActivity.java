package com.example.moham.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ImageButton mSendMessageImageButton;
    private EditText mSendMessagetEditText;
    private ScrollView mScrollView;
    private TextView mDisplayMessageTextView;
    private Intent mIntent;
    private String mGroupName, mCurrentUserId, mCurrentUserName, mCurrentDate, mCurrentTime;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef, mGroupNameRef, mGroupMessageKeyRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        init();
        setUpToolbar();
        getIntentActivity();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mGroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(mGroupName);
        getUserInfo();
        mSendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageInfoInDatabase();
                mSendMessagetEditText.setText("");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()) {

                    DisplayMesxsage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void DisplayMesxsage(DataSnapshot dataSnapshot) {

        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {

            String chatDate = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot) iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot) iterator.next()).getValue();
            mDisplayMessageTextView.append(chatName + ":\n" + chatMessage + "\n" + chatDate + "   " + chatTime + "\n\n\n");
        }
    }



    private void init() {
        mSendMessagetEditText = findViewById(R.id.send_message_edit_text);
        mSendMessageImageButton = findViewById(R.id.send_message_ib);
        mDisplayMessageTextView = findViewById(R.id.group_chat_text_display_tv);
        mScrollView = findViewById(R.id.my_scroll_view);

    }


    private void setUpToolbar() {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Group Name");
    }

    private void getIntentActivity() {
        mIntent = getIntent();
        mGroupName = mIntent.getExtras().get("GroupName").toString();
        mToolbar.setTitle(mGroupName);
    }


    private void getUserInfo() {
        mUserRef.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    mCurrentUserName = dataSnapshot.child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void saveMessageInfoInDatabase() {
        String message = mSendMessagetEditText.getText().toString();
        String messageKey = mGroupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(GroupChatActivity.this, "please write a message first...", Toast.LENGTH_SHORT).show();
        } else {

            Calendar calfordate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat =
                    new SimpleDateFormat("dd,MM,yyyy");
            mCurrentDate = currentDateFormat.format(calfordate.getTime());

            Calendar calfortime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat =
                    new SimpleDateFormat("hh:mm a");
            mCurrentTime = currentTimeFormat.format(calfortime.getTime());

            HashMap<String, Object> groupMessageKey =
                    new HashMap<>();
            mGroupNameRef.updateChildren(groupMessageKey);


            mGroupMessageKeyRef = mGroupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap =
                    new HashMap<>();

            messageInfoMap.put("name", mCurrentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", mCurrentDate);
            messageInfoMap.put("time", mCurrentTime);

            mGroupMessageKeyRef.updateChildren(messageInfoMap);
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}

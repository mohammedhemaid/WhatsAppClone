package com.example.moham.whatsapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moham.whatsapp.Adapters.MessageAdapter;
import com.example.moham.whatsapp.Model.Messages;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mMessageReceiverID, mMessageReciverName, mMessageReciverImage, mMessageSenderID;
    private TextView mUserNameTextView, mUserLastSeenTextView;
    private CircleImageView mChatUserImageCircleImageView;
    private Toolbar mChatToolbarl;
    private ImageButton mSendMessageImageButton;
    private EditText mMessageEditText;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private final List<Messages> messagesList = new ArrayList<>();
    private MessageAdapter mMessageAdapter;
    private RecyclerView mMessageListRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mMessageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        mMessageReciverName = getIntent().getExtras().get("visit_user_name").toString();
        mMessageReciverImage = getIntent().getExtras().get("visit_user_image").toString();

        setUpToolbar();
        init();

        mUserNameTextView.setText(mMessageReciverName);
        Picasso.get().load(mMessageReciverImage).placeholder(R.drawable.pic).into(mChatUserImageCircleImageView);

        mSendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        DisplayUserLastSeen();
    }

    private void init() {
        mUserNameTextView = findViewById(R.id.chat_user_name_tv);
        mUserLastSeenTextView = findViewById(R.id.chat_user_last_seen_tv);
        mChatUserImageCircleImageView = findViewById(R.id.chat_image_civ);
        mSendMessageImageButton = findViewById(R.id.send_chat_message_ib);
        mMessageEditText = findViewById(R.id.send_chat_message_edit_text);
        mAuth = FirebaseAuth.getInstance();
        mMessageSenderID = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mMessageAdapter = new MessageAdapter(ChatActivity.this,messagesList);
        mMessageListRecyclerView = findViewById(R.id.private_list_messages_rv);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mMessageListRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageListRecyclerView.setAdapter(mMessageAdapter);

    }

    private void setUpToolbar() {
         mChatToolbarl = findViewById(R.id.chat_bar_layout);
        setSupportActionBar(mChatToolbarl);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater mInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View actionBarView = mInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);
    }

    void DisplayUserLastSeen(){

            mDatabase.child("Users").child(mMessageSenderID)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("userState").hasChild("state")) {

                                String State = dataSnapshot.child("userState").child("state").getValue().toString();
                                String time = dataSnapshot.child("userState").child("time").getValue().toString();
                                String date = dataSnapshot.child("userState").child("date").getValue().toString();

                                if (State.equals("Online")){
                                    mUserLastSeenTextView.setText("online");

                                }else if (State.equals("offline")){

                                    mUserLastSeenTextView.setText("last seen: "+date+" "+time);

                                }
                            } else {

                                mUserLastSeenTextView.setText("offline");

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
    }

    @Override
    protected void onStart() {
        super.onStart();

        mDatabase.child("Messages").child(mMessageSenderID).child(mMessageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        mMessageAdapter.notifyDataSetChanged();

                        mMessageListRecyclerView.smoothScrollToPosition(mMessageListRecyclerView.getAdapter().getItemCount());
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

    private void sendMessage() {
        String messageText = mMessageEditText.getText().toString();
        if (!TextUtils.isEmpty(messageText)) {

            String messageSenderRef = "Messages/" + mMessageSenderID + "/" + mMessageReceiverID;
            String messageReceiverRef = "Messages/" + mMessageReceiverID + "/" + mMessageSenderID;

            DatabaseReference mUserMessageKeyRef = mDatabase.child("Messages")
                    .child(mMessageSenderID).child(mMessageReceiverID).push();

            String messagePushID = mUserMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", mMessageSenderID);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            mDatabase.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()) {

                        //Toast.makeText(ChatActivity.this, "Message Sent ", Toast.LENGTH_SHORT).show();

                    } else {

                        Toast.makeText(ChatActivity.this, "Message error ", Toast.LENGTH_SHORT).show();
                    }
                    mMessageEditText.setText("");
                }
            });

        }


    }


}

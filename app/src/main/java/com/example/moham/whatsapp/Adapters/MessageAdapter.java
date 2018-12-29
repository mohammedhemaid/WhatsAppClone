package com.example.moham.whatsapp.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.moham.whatsapp.Model.Messages;
import com.example.moham.whatsapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mUserMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private final LayoutInflater mInflater;


    public MessageAdapter(Context context, List<Messages> messages) {
        mInflater = LayoutInflater.from(context);
        mUserMessageList = messages;
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

//        View view = LayoutInflater.from(viewGroup.getContext())
//                .inflate(R.layout.custom_message_layout, viewGroup, false);

        View view = mInflater.inflate(R.layout.custom_message_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder messageViewHolder, int position) {


        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = mUserMessageList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("image")) {

                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.pic).into(messageViewHolder.mReceiverImageCircleImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (fromMessageType.equals("text")) {

            messageViewHolder.mReceiverMessageTextView.setVisibility(View.INVISIBLE);
            messageViewHolder.mReceiverImageCircleImageView.setVisibility(View.INVISIBLE);

            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.mSenderMessageTextView.setVisibility(View.VISIBLE);
                messageViewHolder.mSenderMessageTextView.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.mSenderMessageTextView.setTextColor(Color.BLACK);
                messageViewHolder.mSenderMessageTextView.setText(messages.getMessage());
            } else {
                messageViewHolder.mSenderMessageTextView.setVisibility(View.INVISIBLE);
                messageViewHolder.mReceiverImageCircleImageView.setVisibility(View.VISIBLE);
                messageViewHolder.mReceiverMessageTextView.setVisibility(View.VISIBLE);

                messageViewHolder.mReceiverMessageTextView.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.mReceiverMessageTextView.setTextColor(Color.BLACK);
                messageViewHolder.mReceiverMessageTextView.setText(messages.getMessage());
            }
        }

    }


    @Override
    public int getItemCount() {
        return mUserMessageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView mSenderMessageTextView, mReceiverMessageTextView;
        CircleImageView mReceiverImageCircleImageView;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            mSenderMessageTextView = itemView.findViewById(R.id.sender_message_text);
            mReceiverMessageTextView = itemView.findViewById(R.id.receiver_message_text);
            mReceiverImageCircleImageView = itemView.findViewById(R.id.message_user_image_civ);
        }
    }


}

package com.example.moham.whatsapp.Fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moham.whatsapp.Model.Contacts;
import com.example.moham.whatsapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {


    private View mRequestFragmentView;
    private RecyclerView mRequestFragmentRecycleView;
    private DatabaseReference mChatRequestRef, mUserRef, mContactRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRequestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mRequestFragmentRecycleView = mRequestFragmentView.findViewById(R.id.request_list_rv);
        mRequestFragmentRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        mChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        return mRequestFragmentView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(mChatRequestRef.child(mCurrentUserID), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, RequestsViewHolder> adaper =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contacts model) {

                        holder.itemView.findViewById(R.id.accept_request_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.cancel_request_button).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();
                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {

                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals("received")) {

                                        mUserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild("image")) {

                                                    String requestuserImage = dataSnapshot.child("image").getValue().toString();


                                                    Picasso.get().load(requestuserImage).placeholder(R.drawable.ic_person_black_24dp).into(holder.profileImage);


                                                }
                                                final String requestusername = dataSnapshot.child("name").getValue().toString();
                                                String requestuserStatus = dataSnapshot.child("status").getValue().toString();

                                                holder.userName.setText(requestusername);
                                                holder.userStatus.setText(requestuserStatus);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence option[] = new CharSequence[]{

                                                                "Accept", "Cancel"

                                                        };
                                                        // implement progress bar

                                                        AlertDialog.Builder builder =
                                                                new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestusername + "chatRequest");
                                                        builder.setItems(option, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                if (which == 0) {
                                                                    mContactRef.child(mCurrentUserID).child(list_user_id).child("Contacts")
                                                                            .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                            if (task.isSuccessful()) {
                                                                                mContactRef.child(list_user_id).child(mCurrentUserID).child("Contacts")
                                                                                        .setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        if (task.isSuccessful()) {

                                                                                            mChatRequestRef.child(mCurrentUserID).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                mChatRequestRef.child(list_user_id).child(mCurrentUserID)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if (task.isSuccessful()) {
                                                                                                                                    Toast.makeText(getContext(), "Saved", Toast.LENGTH_SHORT).show();
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
                                                                        }
                                                                    });
                                                                }
                                                                if (which == 1) {
                                                                    mChatRequestRef.child(mCurrentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        mChatRequestRef.child(list_user_id).child(mCurrentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                                        builder.show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }


                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext())
                                .inflate(R.layout.user_item_row, viewGroup, false);

                        RequestsViewHolder holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };

        mRequestFragmentRecycleView.setAdapter(adaper);
        adaper.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {


        TextView userName, userStatus;
        CircleImageView profileImage;
        MaterialButton mAcceptButton, mCancelButton;


        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);


            userName = itemView.findViewById(R.id.user_name_chat_tv);
            userStatus = itemView.findViewById(R.id.user_status_tv);
            profileImage = itemView.findViewById(R.id.chat_proflie_civ);
            mAcceptButton = itemView.findViewById(R.id.accept_request_button);
            mCancelButton = itemView.findViewById(R.id.cancel_request_button);
        }
    }
}

package com.example.moham.whatsapp.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.moham.whatsapp.Model.Contacts;
import com.example.moham.whatsapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ContactsFragment extends Fragment {


    private View mContactView;
    private RecyclerView mMyContactRecycleView;
    private DatabaseReference mContactRef, mUserRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContactView = inflater.inflate(R.layout.fragment_contacts, container, false);
        mMyContactRecycleView = mContactView.findViewById(R.id.contact_list_rv);
        mMyContactRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();
        mContactRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(mCurrentUserId);
        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        return mContactView;
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(mContactRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, ContactViewHolder> mAdapter
                = new FirebaseRecyclerAdapter<Contacts, ContactViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int position, @NonNull Contacts model) {


                String userId = getRef(position).getKey();

                    mUserRef.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("image")) {

                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String username = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();

                                Picasso.get().load(userImage).placeholder(R.drawable.ic_person_black_24dp).into(holder.profileImage);
                                holder.mUserName.setText(username);
                                holder.mUserStatus.setText(userStatus);

                            } else {

                                String username = dataSnapshot.child("name").getValue().toString();
                                String userStatus = dataSnapshot.child("status").getValue().toString();

                                holder.mUserName.setText(username);
                                holder.mUserStatus.setText(userStatus);

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }


            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.user_item_row, viewGroup, false);
                ContactViewHolder viewHolder =
                        new ContactViewHolder(view);


                return viewHolder;
            }
        };
        mMyContactRecycleView.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView mUserName, mUserStatus;
        CircleImageView profileImage;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            mUserName = itemView.findViewById(R.id.user_name_chat_tv);
            mUserStatus = itemView.findViewById(R.id.user_status_tv);
            profileImage = itemView.findViewById(R.id.chat_proflie_civ);

        }
    }
}

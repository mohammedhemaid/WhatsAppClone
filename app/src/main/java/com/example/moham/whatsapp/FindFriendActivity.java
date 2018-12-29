package com.example.moham.whatsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.moham.whatsapp.Model.Contacts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendActivity extends AppCompatActivity {


    Toolbar mToolbar;
    RecyclerView mFindFriendsRecycleView;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);
        initToolbar();
        mFindFriendsRecycleView = findViewById(R.id.find_friends_rv);
        mFindFriendsRecycleView.setLayoutManager(new LinearLayoutManager(this));

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    private void initToolbar() {
        mToolbar = findViewById(R.id.find_friends_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.find_friends);
    }


    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options =

                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(userRef, Contacts.class)
                        .build();


        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.pic).into(holder.profileImage);
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_User_Id
                                        = getRef(position).getKey();

                                Intent profileIntent =
                                        new Intent(FindFriendActivity.this, ProfileActivity.class);
                                profileIntent.putExtra("visit_user_id", visit_User_Id);
                                startActivity(profileIntent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                        View view = LayoutInflater.from(viewGroup.getContext())
                                .inflate(R.layout.user_item_row, viewGroup, false);
                        FindFriendViewHolder viewHolder =
                                new FindFriendViewHolder(view);


                        return viewHolder;
                    }
                };

        mFindFriendsRecycleView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder {


        TextView userName, userStatus;
        CircleImageView profileImage;


        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);


            userName = itemView.findViewById(R.id.user_name_chat_tv);
            userStatus = itemView.findViewById(R.id.user_status_tv);
            profileImage = itemView.findViewById(R.id.chat_proflie_civ);

        }
    }

}



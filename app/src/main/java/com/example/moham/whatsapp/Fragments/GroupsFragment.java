package com.example.moham.whatsapp.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.moham.whatsapp.GroupChatActivity;
import com.example.moham.whatsapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private View mGroupFragmentView;
    ListView mListView;
    ArrayAdapter<String> mAdapter;
    ArrayList<String> listOfGroup = new ArrayList<>();
    DatabaseReference mDatabase;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mGroupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        init();
        retriveDisplayGroups();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String currentGroupName = parent.getItemAtPosition(position).toString();
                Intent mGroupIntent = new Intent(getContext(), GroupChatActivity.class);
                mGroupIntent.putExtra("GroupName", currentGroupName);
                startActivity(mGroupIntent);
            }
        });
        return mGroupFragmentView;
    }


    void init() {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Groups");
        mListView = mGroupFragmentView.findViewById(R.id.list_view_groups);
        mAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, listOfGroup);
        mListView.setAdapter(mAdapter);

    }

    private void retriveDisplayGroups() {
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext()) {
                    set.add(((DataSnapshot) iterator.next()).getKey());
                }

                listOfGroup.clear();
                listOfGroup.addAll(set);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


}

package com.example.moham.whatsapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.moham.whatsapp.Adapters.CategoryAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    CategoryAdapter mcategoryAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    String mCurrentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initToolbar();
        SetupViewPager();

    }

    private void init() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    private void SetupViewPager() {

        //Setup ViewPager
        mViewPager = findViewById(R.id.main_page_view_pager);
        mcategoryAdapter = new CategoryAdapter(this, getSupportFragmentManager());
        mViewPager.setAdapter(mcategoryAdapter);
        //setup tab layout
        mTabLayout = findViewById(R.id.main_tabs_tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    private void initToolbar() {
        mToolbar = findViewById(R.id.main_activity_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.whatsapp);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser mCurrentUser = mAuth.getCurrentUser();


        if (mCurrentUser == null) {

            sendUserToLoginActivity();
        } else {
            updateUserStatus("Online");
            VerfiyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser mCurrentUser = mAuth.getCurrentUser();

        if (mCurrentUser != null) {

            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser mCurrentUser = mAuth.getCurrentUser();

        if (mCurrentUser != null) {

            updateUserStatus("offline");
        }
    }

    private void VerfiyUserExistance() {

        String currentUserId = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("name").exists()) {
                   // Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                } else {

                    sendUserToSettingsActivity();
                }
            }


            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.find_friends:
                navigateToFindFriendsActivity();
                break;
            case R.id.setting:
                sendUserToSettingsActivity();
                break;
            case R.id.CreateGroup:
                requestNewGroup();
                break;
            case R.id.logout:
                updateUserStatus("offline");
                mAuth.signOut();
                sendUserToLoginActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToFindFriendsActivity() {
        Intent findFriendIntent = new Intent(MainActivity.this, FindFriendActivity.class);
        startActivity(findFriendIntent);
    }

    private void requestNewGroup() {
        AlertDialog.Builder mAlertDialog = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog_MainActivity);
        mAlertDialog.setTitle(R.string.enter_group_name);

        final EditText groupNameField =
                new EditText(MainActivity.this);
        groupNameField.setHint("e.g Pubg ");
        mAlertDialog.setView(groupNameField);

        InputFilter[] editFilters = groupNameField.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.LengthFilter(15);
        groupNameField.setFilters(newFilters);


        mAlertDialog.setPositiveButton("create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {

                    Toast.makeText(MainActivity.this, "please write a group name", Toast.LENGTH_SHORT).show();
                } else {

                    createNewGroup(groupName);
                }
            }
        });
        mAlertDialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mAlertDialog.show();
    }

    private void createNewGroup(final String groupName) {
        mDatabase.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, groupName + " group is Created Successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    void updateUserStatus(String state) {

        String saveCurrentTime, saveCurrentDate;

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM dd yyyy");

        saveCurrentDate = dateFormat.format(calendar.getTime());

        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");

        saveCurrentTime = timeFormat.format(calendar.getTime());


        HashMap<String, Object> onlineStateMap = new HashMap<>();

        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        mCurrentUserID = mAuth.getCurrentUser().getUid();

        mDatabase.child("Users").child(mCurrentUserID).child("userState")
                .updateChildren(onlineStateMap);

    }
}

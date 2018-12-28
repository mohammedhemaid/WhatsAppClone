package com.example.moham.whatsapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class ChatActivity extends AppCompatActivity {

    private String mMessageReciverID , mMessageReciverName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mMessageReciverID = getIntent().getExtras().get("visit_user_id").toString();
        mMessageReciverName = getIntent().getExtras().get("visit_user_name").toString();

        Toast.makeText(this, mMessageReciverID, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, mMessageReciverName, Toast.LENGTH_SHORT).show();
    }
}

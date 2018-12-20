package com.example.moham.whatsapp;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.moham.whatsapp.Adapters.CategoryAdapter;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    CategoryAdapter mcategoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();
        SetupViewPager();
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


}

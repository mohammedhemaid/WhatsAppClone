package com.example.moham.whatsapp.Adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.moham.whatsapp.Fragments.ChatFragment;
import com.example.moham.whatsapp.Fragments.ContactsFragment;
import com.example.moham.whatsapp.Fragments.GroupsFragment;
import com.example.moham.whatsapp.R;

public class CategoryAdapter extends FragmentPagerAdapter {
    Context mContext;

    public CategoryAdapter(Context mContext, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.mContext = mContext;


    }

    @Override
    public Fragment getItem(int i) {

        switch (i) {
            case 0:
                return new ChatFragment();
            case 1:
                return new GroupsFragment();
            case 2:
                return new ContactsFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {

            case 0:
                return mContext.getString(R.string.chats);
            case 1:
                return mContext.getString(R.string.groups);
            case 2:
                return mContext.getString(R.string.contacts);
            default:
                return null;
        }

    }
}

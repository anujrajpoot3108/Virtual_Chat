package com.example.virtualchat;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdaptor extends FragmentPagerAdapter {
    public TabsAccessorAdaptor(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                chatFragment mychatFragment=new chatFragment();
                return mychatFragment;
            case 1:
                GroupsFragment mygroupFragment=new GroupsFragment();
                return mygroupFragment;
            case 2:
                contactsFragment mycontactsFragment=new contactsFragment();
                return mycontactsFragment;
            case 3:
                requestFragment myrequestfragment=new requestFragment();
                return myrequestfragment;
             default:
                 return null;

    } }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "chats";
            case 1:
                return "groups";
            case 2:
                return "contacts";
            case 3:
                return "Requests";
            default:
                return null;
        }
    }}
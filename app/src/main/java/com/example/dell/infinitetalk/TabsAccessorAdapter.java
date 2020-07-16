package com.example.dell.infinitetalk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter {
    public TabsAccessorAdapter(@NonNull FragmentManager fm) {

        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

      switch (position){
          case 0:
              return new CHATS();
          case 1:
              return new POSTS();
          case 2:
              return new CONTACTS();

      }
      return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0: return "CHATS";
            case 1: return "POSTS";
            case 2: return "CONTACTS";
        }
        return null;
    }
}

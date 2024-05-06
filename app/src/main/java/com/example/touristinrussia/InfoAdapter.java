package com.example.touristinrussia;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class InfoAdapter extends FragmentStateAdapter {
    public InfoAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return(InfoFragment.newInstance(position));
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}

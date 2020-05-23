package xyz.joumboes.wechat.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import xyz.joumboes.wechat.fragment.ContactFragment;
import xyz.joumboes.wechat.fragment.SessionFragment;
import xyz.joumboes.wechat.fragment.SettingFragment;

public class MyFragmentAdapter extends FragmentPagerAdapter {
    private List<Fragment> fragmentList = new ArrayList<>();

    public MyFragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
        this.fragmentList.add(new SessionFragment());
        this.fragmentList.add(new ContactFragment());
        this.fragmentList.add(new SettingFragment());
    }

    public MyFragmentAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.fragmentList.add(new SessionFragment());
        this.fragmentList.add(new ContactFragment());
        this.fragmentList.add(new SettingFragment());
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
